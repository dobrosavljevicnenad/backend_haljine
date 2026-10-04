package io.github.dobrosavljevicnenad.dressinventory.dress;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DressTest {

    @Test
    void replaceStockUpdatesExistingVariantsInPlaceAndRemovesMissingOnes() {
        Dress dress = new Dress("Lana", null, new BigDecimal("4500.00"));
        dress.replaceStock(variants("Crvena", 2, "Plava", 1));
        StockItem red = dress.findStock("Crvena").orElseThrow();

        dress.replaceStock(variants("Crvena", 5, "Zelena", 3));

        assertThat(dress.findStock("Crvena")).get().isSameAs(red);
        assertThat(red.getQuantity()).isEqualTo(5);
        assertThat(dress.findStock("Plava")).isEmpty();
        assertThat(dress.findStock("Zelena")).get().extracting(StockItem::getQuantity).isEqualTo(3);
        assertThat(dress.getStock()).hasSize(2);
    }

    private static Map<String, Integer> variants(String color1, int quantity1, String color2, int quantity2) {
        Map<String, Integer> variants = new LinkedHashMap<>();
        variants.put(color1, quantity1);
        variants.put(color2, quantity2);
        return variants;
    }
}
