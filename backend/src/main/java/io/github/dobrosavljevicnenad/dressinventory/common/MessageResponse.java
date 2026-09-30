package io.github.dobrosavljevicnenad.dressinventory.common;

/**
 * Simple acknowledgement body, same shape as the legacy API: {"message": "..."}.
 */
public record MessageResponse(String message) {
}
