package org.example.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private ImgBbService imgBbService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-bytes:52428800}") // 50MB
    private long maxFileBytes;

    @Value("${server.port:9091}")
    private String serverPort;

    public String storeFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (file.getSize() > maxFileBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File is too large");
        }

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            try {
                return imgBbService.uploadImage(file);
            } catch (Exception e) {
                log.warn("ImgBB upload failed, falling back to local storage", e);
            }
        }

        // Local Storage Logic
        try {
            java.nio.file.Path targetDir = java.nio.file.Paths.get(uploadDir, subDir != null ? subDir : "general").toAbsolutePath().normalize();
            if (!java.nio.file.Files.exists(targetDir)) {
                java.nio.file.Files.createDirectories(targetDir);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path targetPath = targetDir.resolve(fileName);
            file.transferTo(targetPath);

            // Return relative URL that will be served by a controller or ResourceHandler
            return "/uploads/" + (subDir != null ? subDir : "general") + "/" + fileName;
        } catch (IOException e) {
            log.error("Failed to store file locally", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file");
        }
    }
}
