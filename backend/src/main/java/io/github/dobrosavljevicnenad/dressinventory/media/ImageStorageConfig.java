package io.github.dobrosavljevicnenad.dressinventory.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class ImageStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageConfig.class);

    @Bean
    ImageStorage imageStorage(CloudinaryProperties properties) {
        if (!properties.isConfigured()) {
            log.warn("Cloudinary is not configured (CLOUDINARY_* variables missing): image uploads are disabled");
            return new UnconfiguredImageStorage();
        }
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", properties.cloudName(),
                "api_key", properties.apiKey(),
                "api_secret", properties.apiSecret(),
                "secure", true));
        return new CloudinaryImageStorage(cloudinary, properties.folder());
    }
}
