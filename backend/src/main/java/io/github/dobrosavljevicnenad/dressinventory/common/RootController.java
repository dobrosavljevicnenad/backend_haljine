package io.github.dobrosavljevicnenad.dressinventory.common;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Plain-text landing endpoint, kept for parity with the legacy Node.js service.
 */
@RestController
class RootController {

    @GetMapping(path = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    String root() {
        return "Dress Inventory API is running. All endpoints are available under /api.";
    }
}
