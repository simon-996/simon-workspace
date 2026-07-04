package com.simon.workspace.storage;

public record StoredObject(
        String providerCode,
        StorageProviderType providerType,
        String objectKey,
        String storagePath,
        long fileSize,
        String publicUrl
) {
}
