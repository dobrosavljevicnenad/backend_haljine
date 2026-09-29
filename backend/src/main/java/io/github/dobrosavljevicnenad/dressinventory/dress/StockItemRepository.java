package io.github.dobrosavljevicnenad.dressinventory.dress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Stock changes are executed as single atomic SQL statements instead of read-modify-write
 * in Java, so concurrent requests can never lose an update or sell more than is in stock.
 */
public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    /**
     * Decrements the quantity only if enough items are available.
     *
     * @return 1 if the stock was reserved, 0 if the color does not exist or there is not enough stock
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update StockItem s
               set s.quantity = s.quantity - :quantity
             where s.dress.id = :dressId
               and s.color = :color
               and s.quantity >= :quantity
            """)
    int decrementIfAvailable(@Param("dressId") UUID dressId,
                             @Param("color") String color,
                             @Param("quantity") int quantity);

    /** Removes color variants that are sold out (same behavior as the legacy API). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from StockItem s where s.dress.id = :dressId and s.quantity <= 0")
    int deleteDepleted(@Param("dressId") UUID dressId);

    /** Atomic upsert: adds to an existing color or creates the color variant if it does not exist yet. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            insert into stock_item (id, dress_id, color, quantity)
            values (gen_random_uuid(), :dressId, :color, :quantity)
            on conflict (dress_id, color)
            do update set quantity = stock_item.quantity + excluded.quantity
            """, nativeQuery = true)
    int addQuantity(@Param("dressId") UUID dressId,
                    @Param("color") String color,
                    @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from StockItem s where s.dress.id = :dressId and s.color = :color")
    int deleteColor(@Param("dressId") UUID dressId, @Param("color") String color);

    boolean existsByDressIdAndColor(UUID dressId, String color);
}
