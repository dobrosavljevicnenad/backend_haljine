package io.github.dobrosavljevicnenad.dressinventory.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    long deleteByPaymentScreenshotUrl(String paymentScreenshotUrl);

    boolean existsByPaymentScreenshotUrl(String paymentScreenshotUrl);
}
