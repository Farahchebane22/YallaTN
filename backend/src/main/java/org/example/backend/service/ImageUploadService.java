package org.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ImageUploadService {

    private static final String IMGBB_ENDPOINT = "https://api.imgbb.com/1/upload";
    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    @Value("${app.imgbb.api-key:${imgbb.api.key:}}")
    private String apiKey;

    @Autowired
    private ImgBbService imgBbService;

    @Value("${app.upload.max-image-bytes:5242880}")
    private long maxImageBytes;

    public ImageUploadService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "api.error.image.required");
        }
        if (file.getSize() > maxImageBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "api.error.image.too_large");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "api.error.image.type_invalid");
        }

        try {
            return imgBbService.uploadImage(file);
        } catch (Exception ex) {
            log.error("ImgBB upload failed: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "api.error.image.upload_failed");
        }
    }
}
