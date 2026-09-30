package io.github.dobrosavljevicnenad.dressinventory.common;

import java.util.UUID;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException dress(UUID id) {
        return new NotFoundException("Haljina nije pronađena (id: " + id + ")");
    }

    public static NotFoundException order(UUID id) {
        return new NotFoundException("Narudžbina nije pronađena (id: " + id + ")");
    }
}
