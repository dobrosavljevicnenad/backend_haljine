package io.github.dobrosavljevicnenad.dressinventory.common;

/**
 * The request is valid but conflicts with the current state of a resource (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
