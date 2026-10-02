package io.github.dobrosavljevicnenad.dressinventory.dress;

import io.github.dobrosavljevicnenad.dressinventory.common.NotFoundException;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.DressRequest;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.DressUpdateRequest;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.StockItemRequest;
import io.github.dobrosavljevicnenad.dressinventory.media.ImagesReleasedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class DressService {

    private final DressRepository dresses;
    private final StockItemRepository stock;
    private final ApplicationEventPublisher events;

    public DressService(DressRepository dresses, StockItemRepository stock, ApplicationEventPublisher events) {
        this.dresses = dresses;
        this.stock = stock;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<Dress> findAll() {
        return dresses.findAllByOrderByCreatedAtDesc();
    }

    public Dress create(DressRequest request) {
        Dress dress = new Dress(request.name().trim(), blankToNull(request.imageUrl()), request.price());
        dress.replaceStock(normalize(request.stock()));
        return dresses.save(dress);
    }

    public Dress update(UUID id, DressUpdateRequest request) {
        Dress dress = dresses.findWithStockById(id).orElseThrow(() -> NotFoundException.dress(id));
        String previousImage = dress.getImageUrl();
        if (request.name() != null) {
            dress.setName(request.name().trim());
        }
        if (request.imageUrl() != null) {
            dress.setImageUrl(blankToNull(request.imageUrl()));
        }
        if (request.price() != null) {
            dress.setPrice(request.price());
        }
        if (request.stock() != null) {
            dress.replaceStock(normalize(request.stock()));
        }
        if (previousImage != null && !previousImage.equals(dress.getImageUrl())) {
            events.publishEvent(ImagesReleasedEvent.of(previousImage));
        }
        return dress;
    }

    public Dress delete(UUID id) {
        Dress dress = dresses.findById(id).orElseThrow(() -> NotFoundException.dress(id));
        dresses.delete(dress);
        events.publishEvent(ImagesReleasedEvent.of(dress.getImageUrl()));
        return dress;
    }

    public void addQuantity(UUID id, String color, int quantity) {
        requireExists(id);
        stock.addQuantity(id, color.trim(), quantity);
    }

    public void removeColor(UUID id, String color) {
        requireExists(id);
        stock.deleteColor(id, color.trim());
    }

    private void requireExists(UUID id) {
        if (!dresses.existsById(id)) {
            throw NotFoundException.dress(id);
        }
    }

    /**
     * Ignores rows without a color (the admin form always sends one empty row)
     * and merges duplicate colors by summing their quantities.
     */
    static Map<String, Integer> normalize(List<StockItemRequest> items) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        if (items == null) {
            return quantities;
        }
        for (StockItemRequest item : items) {
            if (item == null || item.color() == null || item.color().isBlank()) {
                continue;
            }
            quantities.merge(item.color().trim(), Math.max(0, item.quantity()), Integer::sum);
        }
        return quantities;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
