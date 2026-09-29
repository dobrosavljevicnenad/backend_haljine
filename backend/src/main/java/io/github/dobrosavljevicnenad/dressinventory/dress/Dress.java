package io.github.dobrosavljevicnenad.dressinventory.dress;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "dress")
public class Dress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Optimistic locking: concurrent edits of the same dress fail instead of silently overwriting each other. */
    @Version
    private long version;

    @OneToMany(mappedBy = "dress", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("color ASC")
    private List<StockItem> stock = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Dress() {
        // required by JPA
    }

    public Dress(String name, String imageUrl, BigDecimal price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    /**
     * Replaces the color variants with the given quantities. Existing rows are updated in place
     * (instead of delete + insert) so the unique (dress, color) constraint is never violated during flush.
     */
    public void replaceStock(Map<String, Integer> quantitiesByColor) {
        stock.removeIf(item -> !quantitiesByColor.containsKey(item.getColor()));
        quantitiesByColor.forEach((color, quantity) -> findStock(color).ifPresentOrElse(
                item -> item.setQuantity(quantity),
                () -> stock.add(new StockItem(this, color, quantity))));
    }

    public Optional<StockItem> findStock(String color) {
        return stock.stream().filter(item -> item.getColor().equals(color)).findFirst();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public long getVersion() {
        return version;
    }

    public List<StockItem> getStock() {
        return Collections.unmodifiableList(stock);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
