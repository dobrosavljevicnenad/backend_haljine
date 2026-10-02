package io.github.dobrosavljevicnenad.dressinventory.media;

import java.net.URI;
import java.util.Optional;

/**
 * Extracts the Cloudinary public id ("folder/name") from a delivery URL such as
 * {@code https://res.cloudinary.com/demo/image/upload/v1712345678/haljine/abc123.jpg}.
 */
final class CloudinaryPublicIds {

    private CloudinaryPublicIds() {
    }

    static Optional<String> fromUrl(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        try {
            String path = URI.create(url.trim()).getPath();
            if (path == null) {
                return Optional.empty();
            }
            String[] segments = path.split("/");
            if (segments.length < 2) {
                return Optional.empty();
            }
            String folder = segments[segments.length - 2];
            String fileName = segments[segments.length - 1];
            int extension = fileName.indexOf('.');
            String name = extension >= 0 ? fileName.substring(0, extension) : fileName;
            if (folder.isBlank() || name.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(folder + "/" + name);
        } catch (IllegalArgumentException invalidUrl) {
            return Optional.empty();
        }
    }
}
