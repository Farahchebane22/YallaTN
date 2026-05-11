package org.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Service
public class SwiftStorageService {

    @Value("${swift.public-url}")
    private String publicUrl;

    @Value("${swift.project-id:a131d52ca82444039f2a42202440851c}")
    private String projectId;

    @Value("${swift.container:cloudaura-uploads}")
    private String containerName;

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + extension;

        // Build the URL based on user's specific request
        // String urlStr = "http://192.168.100.167:8080/v1/AUTH_" + projectId + "/cloudaura-uploads/" + fileName;
        // Using the publicUrl property which should be configured in application.properties
        String urlStr = publicUrl + "/" + fileName;

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", file.getContentType());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(file.getBytes());
        }

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("Failed to upload file to Swift. Response code: " + responseCode);
        }

        return urlStr;
    }
}
