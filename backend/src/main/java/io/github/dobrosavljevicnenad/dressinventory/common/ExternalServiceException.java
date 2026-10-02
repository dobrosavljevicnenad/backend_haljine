package io.github.dobrosavljevicnenad.dressinventory.common;

/**
 * A downstream service (e.g. image storage) failed while handling the request (HTTP 502).
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
