package io.github.dobrosavljevicnenad.dressinventory.order;

import io.github.dobrosavljevicnenad.dressinventory.common.NotFoundException;
import io.github.dobrosavljevicnenad.dressinventory.dress.Dress;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressRepository;
import io.github.dobrosavljevicnenad.dressinventory.dress.StockItemRepository;
import io.github.dobrosavljevicnenad.dressinventory.media.ImagesReleasedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final DressRepository dresses;
    private final StockItemRepository stock;
    private final OrderRepository orders;
    private final ApplicationEventPublisher events;

    public OrderService(DressRepository dresses, StockItemRepository stock, OrderRepository orders,
                        ApplicationEventPublisher events) {
        this.dresses = dresses;
        this.stock = stock;
        this.orders = orders;
        this.events = events;
    }

    public CustomerOrder placeOrder(UUID dressId, String color, int quantity, String paymentScreenshotUrl) {
        Dress dress = dresses.findById(dressId).orElseThrow(() -> NotFoundException.dress(dressId));
        String normalizedColor = color.trim();
        reserveStock(dress, normalizedColor, quantity);
        return orders.save(CustomerOrder.snapshotOf(dress, normalizedColor, quantity, blankToNull(paymentScreenshotUrl)));
    }

    /**
     * Places all lines in one transaction. If a single line cannot be fulfilled the exception
     * rolls back everything: no stock is reserved and no order is stored.
     */
    public List<CustomerOrder> placeOrders(List<OrderLine> lines, String paymentScreenshotUrl) {
        List<CustomerOrder> placed = new ArrayList<>(lines.size());
        for (OrderLine line : lines) {
            placed.add(placeOrder(line.dressId(), line.color(), line.quantity(), paymentScreenshotUrl));
        }
        return placed;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> findAll() {
        return orders.findAllByOrderByCreatedAtDesc();
    }

    public CustomerOrder delete(UUID id) {
        CustomerOrder order = orders.findById(id).orElseThrow(() -> NotFoundException.order(id));
        orders.delete(order);
        String screenshot = order.getPaymentScreenshotUrl();
        // other lines of the same checkout may still reference the screenshot
        if (screenshot != null && !orders.existsByPaymentScreenshotUrl(screenshot)) {
            events.publishEvent(ImagesReleasedEvent.of(screenshot));
        }
        return order;
    }

    /** Deletes all lines of one checkout, identified by its payment screenshot. */
    public long deleteGroup(String paymentScreenshotUrl) {
        long deleted = orders.deleteByPaymentScreenshotUrl(paymentScreenshotUrl);
        if (deleted > 0) {
            events.publishEvent(ImagesReleasedEvent.of(paymentScreenshotUrl));
        }
        return deleted;
    }

    /**
     * Reserves stock with a single conditional UPDATE. Under concurrent requests the database
     * serializes updates of the same row and re-checks the condition, so stock can never go negative.
     */
    private void reserveStock(Dress dress, String color, int quantity) {
        if (stock.decrementIfAvailable(dress.getId(), color, quantity) == 0) {
            if (!stock.existsByDressIdAndColor(dress.getId(), color)) {
                throw NotFoundException.color(dress.getName(), color);
            }
            throw new InsufficientStockException(dress.getName(), color);
        }
        stock.deleteDepleted(dress.getId());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record OrderLine(UUID dressId, String color, int quantity) {
    }
}
