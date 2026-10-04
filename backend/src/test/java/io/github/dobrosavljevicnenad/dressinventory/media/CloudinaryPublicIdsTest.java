package io.github.dobrosavljevicnenad.dressinventory.media;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CloudinaryPublicIdsTest {

    @ParameterizedTest
    @CsvSource({
            "https://res.cloudinary.com/demo/image/upload/v1712345678/haljine/abc123.jpg, haljine/abc123",
            "https://res.cloudinary.com/demo/image/upload/haljine/plava-haljina.png, haljine/plava-haljina",
            "https://res.cloudinary.com/demo/image/upload/v1/haljine/no-extension, haljine/no-extension"
    })
    void extractsFolderAndFileName(String url, String expectedPublicId) {
        assertThat(CloudinaryPublicIds.fromUrl(url)).contains(expectedPublicId);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "not a url", "https://example.com/", "https://example.com/image.jpg"})
    void returnsEmptyForUnusableUrls(String url) {
        assertThat(CloudinaryPublicIds.fromUrl(url)).isEmpty();
    }
}
