package io.github.dobrosavljevicnenad.dressinventory.order;

import io.github.dobrosavljevicnenad.dressinventory.common.ConflictException;

public class InsufficientStockException extends ConflictException {

    public InsufficientStockException(String dressName, String color) {
        super("Nema dovoljno na lageru za " + dressName + " (" + color + ")");
    }
}
