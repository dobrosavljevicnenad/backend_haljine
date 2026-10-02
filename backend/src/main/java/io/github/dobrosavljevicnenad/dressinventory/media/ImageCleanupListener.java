package io.github.dobrosavljevicnenad.dressinventory.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Deletes images only after the database transaction has committed. If the transaction
 * rolls back, the images stay; storage failures are logged and never fail the request.
 */
@Component
class ImageCleanupListener {

    private static final Logger log = LoggerFactory.getLogger(ImageCleanupListener.class);

    private final ImageStorage storage;

    ImageCleanupListener(ImageStorage storage) {
        this.storage = storage;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void deleteReleasedImages(ImagesReleasedEvent event) {
        for (String imageUrl : event.imageUrls()) {
            try {
                storage.delete(imageUrl);
            } catch (RuntimeException e) {
                log.warn("Could not delete image {} from storage: {}", imageUrl, e.getMessage());
            }
        }
    }
}
