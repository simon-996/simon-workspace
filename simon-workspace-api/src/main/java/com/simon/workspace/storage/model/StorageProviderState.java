package com.simon.workspace.storage.model;

import com.simon.workspace.storage.StorageProviderType;

import java.time.LocalDateTime;

public record StorageProviderState(
        String providerCode,
        StorageProviderType providerType,
        String displayName,
        boolean configured,
        boolean enabled,
        boolean active,
        String endpoint,
        String bucket,
        String publicBaseUrl,
        String lastTestStatus,
        String lastTestMessage,
        LocalDateTime lastTestTime
) {
}
