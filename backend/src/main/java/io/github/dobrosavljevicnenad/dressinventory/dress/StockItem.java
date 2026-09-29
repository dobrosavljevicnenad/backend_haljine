package io.github.dobrosavljevicnenad.dressinventory.dress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Quantity in stock for one color variant of a dress.
 */
@Entity
@Table(name = "stock_item")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dress_id", nullable = false)
    private Dress dress;

    @Column(nullable = false, length = 100)
    private String color;

    @Column(nullable = false)
    private int quantity;

    protected StockItem() {
        // required by JPA
    }

    StockItem(Dress dress, String color, int quantity) {
        this.dress = dress;
        this.color = color;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public Dress getDress() {
        return dress;
    }

    public String getColor() {
        return color;
    }

    public int getQuantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
