package com.simon.workspace.storage;

import java.io.InputStream;

public interface WorkspaceStorageProvider {

    String code();

    StorageProviderType type();

    boolean enabled();

    boolean configured();

    StoredObject store(
            InputStream inputStream,
            String originalFilename,
            String contentType,
            StorageVisibility visibility
    );

    StorageObjectDownload download(String objectKey);

    void delete(String objectKey);

    StorageConnectionTestResult testConnection();

    String publicUrl(String objectKey);
}
