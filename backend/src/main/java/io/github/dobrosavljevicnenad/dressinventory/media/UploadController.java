package io.github.dobrosavljevicnenad.dressinventory.media;

import io.github.dobrosavljevicnenad.dressinventory.common.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
class UploadController {

    private final ImageStorage storage;

    UploadController(ImageStorage storage) {
        this.storage = storage;
    }

    /** Multipart field name "slika" is kept from the legacy API. */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UploadResponse upload(@RequestParam(name = "slika", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Fajl nije prosleđen");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Dozvoljen je samo upload slika");
        }
        return new UploadResponse(storage.upload(file));
    }

    record UploadResponse(String url) {
    }
}
