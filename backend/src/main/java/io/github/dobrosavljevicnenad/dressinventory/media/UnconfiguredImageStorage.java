package io.github.dobrosavljevicnenad.dressinventory.media;

import io.github.dobrosavljevicnenad.dressinventory.common.ServiceUnavailableException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Used when no storage provider is configured (e.g. local development without
 * Cloudinary credentials): the API still works, only uploads are rejected with 503.
 */
class UnconfiguredImageStorage implements ImageStorage {

    @Override
    public String upload(MultipartFile file) {
        throw new ServiceUnavailableException("Upload slika nije podešen na serveru");
    }

    @Override
    public void delete(String imageUrl) {
        // nothing was uploaded, so there is nothing to delete
    }
}
