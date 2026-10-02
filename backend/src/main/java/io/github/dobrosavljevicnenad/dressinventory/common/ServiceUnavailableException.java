package io.github.dobrosavljevicnenad.dressinventory.common;

/**
 * A feature is not available in the current deployment, e.g. missing configuration (HTTP 503).
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
