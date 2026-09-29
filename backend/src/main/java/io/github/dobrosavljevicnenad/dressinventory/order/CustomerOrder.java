package io.github.dobrosavljevicnenad.dressinventory.order;

import io.github.dobrosavljevicnenad.dressinventory.dress.Dress;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A placed order line. Stores a snapshot of the dress at the time of ordering,
 * so prices and names in the order history never change retroactively.
 */
@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Plain reference: set to null by the database when the dress is deleted. */
    @Column(name = "dress_id")
    private UUID dressId;

    @Column(name = "dress_name", nullable = false, length = 200)
    private String dressName;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 100)
    private String color;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "payment_screenshot_url", length = 1000)
    private String paymentScreenshotUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CustomerOrder() {
        // required by JPA
    }

    private CustomerOrder(Dress dress, String color, int quantity, String paymentScreenshotUrl) {
        this.dressId = dress.getId();
        this.dressName = dress.getName();
        this.imageUrl = dress.getImageUrl();
        this.unitPrice = dress.getPrice();
        this.color = color;
        this.quantity = quantity;
        this.paymentScreenshotUrl = paymentScreenshotUrl;
    }

    public static CustomerOrder snapshotOf(Dress dress, String color, int quantity, String paymentScreenshotUrl) {
        return new CustomerOrder(dress, color, quantity, paymentScreenshotUrl);
    }

    public UUID getId() {
        return id;
    }

    public UUID getDressId() {
        return dressId;
    }

    public String getDressName() {
        return dressName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getColor() {
        return color;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPaymentScreenshotUrl() {
        return paymentScreenshotUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
