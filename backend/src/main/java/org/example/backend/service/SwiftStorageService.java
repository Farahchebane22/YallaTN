package org.example.backend.service;

import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SwiftStorageService {

    @Value("${swift.auth-url}")
    private String authUrl;

    @Value("${swift.username}")
    private String username;

    @Value("${swift.password}")
    private String password;

    @Value("${swift.project-name}")
    private String projectName;

    @Value("${swift.container}")
    private String containerName;

    @Value("${swift.public-url}")
    private String publicUrl;

    public String uploadFile(MultipartFile file) throws IOException {

        AccountConfig config = new AccountConfig();
        config.setAuthUrl(authUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setTenantName(projectName);

        Account account = new AccountFactory(config).createAccount();

        Container container = account.getContainer(containerName);

        if (!container.exists()) {
            container.create();
        }

        String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();

        StoredObject object = container.getObject(filename);

        object.uploadObject(file.getInputStream());

        return publicUrl + "/" + filename;
    }
}
