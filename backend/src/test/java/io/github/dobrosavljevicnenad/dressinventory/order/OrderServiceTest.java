package io.github.dobrosavljevicnenad.dressinventory.order;

import io.github.dobrosavljevicnenad.dressinventory.common.NotFoundException;
import io.github.dobrosavljevicnenad.dressinventory.dress.Dress;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressRepository;
import io.github.dobrosavljevicnenad.dressinventory.dress.StockItemRepository;
import io.github.dobrosavljevicnenad.dressinventory.media.ImagesReleasedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private DressRepository dresses;
    @Mock
    private StockItemRepository stock;
    @Mock
    private OrderRepository orders;
    @Mock
    private ApplicationEventPublisher events;

    @InjectMocks
    private OrderService service;

    private final UUID dressId = UUID.randomUUID();
    private Dress dress;

    @BeforeEach
    void setUp() {
        dress = new Dress("Lana", "https://res.cloudinary.com/demo/image/upload/v1/haljine/lana.jpg",
                new BigDecimal("4500.00"));
        ReflectionTestUtils.setField(dress, "id", dressId);
    }

    @Test
    void placesOrderWithSnapshotTakenFromTheCatalog() {
        given(dresses.findById(dressId)).willReturn(Optional.of(dress));
        given(stock.decrementIfAvailable(dressId, "Crvena", 2)).willReturn(1);
        given(orders.save(any(CustomerOrder.class))).willAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder order = service.placeOrder(dressId, " Crvena ", 2, "https://example.com/uplata.png");

        assertThat(order.getDressName()).isEqualTo("Lana");
        assertThat(order.getUnitPrice()).isEqualByComparingTo("4500.00");
        assertThat(order.getColor()).isEqualTo("Crvena");
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getPaymentScreenshotUrl()).isEqualTo("https://example.com/uplata.png");
        verify(stock).deleteDepleted(dressId);
    }

    @Test
    void rejectsOrderWhenStockIsInsufficient() {
        given(dresses.findById(dressId)).willReturn(Optional.of(dress));
        given(stock.decrementIfAvailable(dressId, "Crvena", 5)).willReturn(0);
        given(stock.existsByDressIdAndColor(dressId, "Crvena")).willReturn(true);

        assertThatThrownBy(() -> service.placeOrder(dressId, "Crvena", 5, null))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Nema dovoljno na lageru za Lana (Crvena)");
        verify(orders, never()).save(any());
        verify(stock, never()).deleteDepleted(any());
    }

    @Test
    void rejectsOrderForColorThatDoesNotExist() {
        given(dresses.findById(dressId)).willReturn(Optional.of(dress));
        given(stock.decrementIfAvailable(dressId, "Zuta", 1)).willReturn(0);
        given(stock.existsByDressIdAndColor(dressId, "Zuta")).willReturn(false);

        assertThatThrownBy(() -> service.placeOrder(dressId, "Zuta", 1, null))
                .isInstanceOf(NotFoundException.class);
        verify(orders, never()).save(any());
    }

    @Test
    void rejectsOrderForUnknownDress() {
        given(dresses.findById(dressId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.placeOrder(dressId, "Crvena", 1, null))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(stock, orders);
    }

    @Test
    void deletingAWholeCheckoutReleasesItsScreenshot() {
        given(orders.deleteByPaymentScreenshotUrl("https://example.com/uplata.png")).willReturn(3L);

        assertThat(service.deleteGroup("https://example.com/uplata.png")).isEqualTo(3);
        verify(events).publishEvent(ImagesReleasedEvent.of("https://example.com/uplata.png"));
    }

    @Test
    void keepsScreenshotWhileOtherLinesOfTheCheckoutStillUseIt() {
        UUID orderId = UUID.randomUUID();
        CustomerOrder order = CustomerOrder.snapshotOf(dress, "Crvena", 1, "https://example.com/uplata.png");
        given(orders.findById(orderId)).willReturn(Optional.of(order));
        given(orders.existsByPaymentScreenshotUrl("https://example.com/uplata.png")).willReturn(true);

        service.delete(orderId);

        verify(orders).delete(order);
        verifyNoInteractions(events);
    }
}
