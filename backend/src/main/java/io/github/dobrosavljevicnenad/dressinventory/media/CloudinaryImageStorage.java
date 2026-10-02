package io.github.dobrosavljevicnenad.dressinventory.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.dobrosavljevicnenad.dressinventory.common.ExternalServiceException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

class CloudinaryImageStorage implements ImageStorage {

    private final Cloudinary cloudinary;
    private final String folder;

    CloudinaryImageStorage(Cloudinary cloudinary, String folder) {
        this.cloudinary = cloudinary;
        this.folder = folder;
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader()
                    .upload(file.getBytes(), ObjectUtils.asMap("folder", folder, "resource_type", "image"));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new ExternalServiceException("Cloudinary nije vratio URL slike");
            }
            return secureUrl.toString();
        } catch (IOException e) {
            throw new ExternalServiceException("Upload slike nije uspeo", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        Optional<String> publicId = CloudinaryPublicIds.fromUrl(imageUrl);
        if (publicId.isEmpty()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId.get(), ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new ExternalServiceException("Brisanje slike nije uspelo", e);
        }
    }
}
