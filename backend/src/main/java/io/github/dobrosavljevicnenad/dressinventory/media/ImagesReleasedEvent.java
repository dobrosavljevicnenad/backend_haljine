package io.github.dobrosavljevicnenad.dressinventory.media;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Published when stored images are no longer referenced by any record.
 */
public record ImagesReleasedEvent(List<String> imageUrls) {

    public static ImagesReleasedEvent of(String... imageUrls) {
        return new ImagesReleasedEvent(Arrays.stream(imageUrls).filter(StringUtils::hasText).toList());
    }
}
