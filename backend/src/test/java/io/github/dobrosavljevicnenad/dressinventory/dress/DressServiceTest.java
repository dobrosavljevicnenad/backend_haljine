package io.github.dobrosavljevicnenad.dressinventory.dress;

import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.StockItemRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class DressServiceTest {

    @Test
    void normalizeIgnoresRowsWithoutColorAndMergesDuplicates() {
        var quantities = DressService.normalize(Arrays.asList(
                new StockItemRequest("Crvena", 2),
                new StockItemRequest("  ", 5),
                new StockItemRequest(null, 1),
                null,
                new StockItemRequest("Crvena ", 1),
                new StockItemRequest("Plava", 0)));

        assertThat(quantities).containsExactly(entry("Crvena", 3), entry("Plava", 0));
    }

    @Test
    void normalizeAcceptsMissingStock() {
        assertThat(DressService.normalize(null)).isEmpty();
    }
}
