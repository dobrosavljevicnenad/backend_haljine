package io.github.dobrosavljevicnenad.dressinventory.media;

import org.springframework.web.multipart.MultipartFile;

/**
 * Port for storing product images and payment screenshots. Keeps the rest of the
 * application independent of the concrete provider (Cloudinary today, S3 tomorrow).
 */
public interface ImageStorage {

    /** Stores the image and returns its public URL. */
    String upload(MultipartFile file);

    /** Deletes a previously uploaded image; unknown URLs are ignored. */
    void delete(String imageUrl);
}
