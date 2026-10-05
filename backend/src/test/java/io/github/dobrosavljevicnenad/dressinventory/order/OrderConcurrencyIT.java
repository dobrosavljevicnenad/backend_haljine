package io.github.dobrosavljevicnenad.dressinventory.order;

import io.github.dobrosavljevicnenad.dressinventory.TestcontainersConfiguration;
import io.github.dobrosavljevicnenad.dressinventory.common.NotFoundException;
import io.github.dobrosavljevicnenad.dressinventory.dress.Dress;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.DressRequest;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.StockItemRequest;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressService;
import io.github.dobrosavljevicnenad.dressinventory.media.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Many customers try to buy the last items at the same moment. With read-modify-write logic
 * some of them would "win" the same item; the conditional UPDATE must sell exactly what is in stock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class OrderConcurrencyIT {

    private static final int ITEMS_IN_STOCK = 5;
    private static final int CONCURRENT_BUYERS = 20;

    @Autowired
    private OrderService orders;

    @Autowired
    private DressService dresses;

    @Autowired
    private JdbcClient jdbc;

    @MockitoBean
    private ImageStorage imageStorage;

    @BeforeEach
    void cleanDatabase() {
        jdbc.sql("truncate table customer_order, stock_item, dress").update();
    }

    @Test
    void concurrentCheckoutsNeverOversell() throws Exception {
        Dress dress = dresses.create(new DressRequest("Lana", null, new BigDecimal("4500.00"),
                List.of(new StockItemRequest("Crvena", ITEMS_IN_STOCK))));

        ExecutorService buyers = Executors.newFixedThreadPool(CONCURRENT_BUYERS);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<Boolean>> attempts = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_BUYERS; i++) {
            attempts.add(buyers.submit(() -> {
                startSignal.await();
                try {
                    orders.placeOrder(dress.getId(), "Crvena", 1, null);
                    return true;
                } catch (InsufficientStockException | NotFoundException soldOut) {
                    return false;
                }
            }));
        }

        startSignal.countDown();
        int successfulOrders = 0;
        for (Future<Boolean> attempt : attempts) {
            if (attempt.get(30, TimeUnit.SECONDS)) {
                successfulOrders++;
            }
        }
        buyers.shutdown();

        assertThat(successfulOrders).isEqualTo(ITEMS_IN_STOCK);
        assertThat(jdbc.sql("select count(*) from customer_order").query(Long.class).single())
                .isEqualTo(ITEMS_IN_STOCK);
        // the sold-out color variant is removed, exactly like in the legacy API
        assertThat(jdbc.sql("select count(*) from stock_item").query(Long.class).single()).isZero();
    }
}
