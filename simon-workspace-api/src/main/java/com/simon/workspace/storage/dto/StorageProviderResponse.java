package com.simon.workspace.storage.dto;

import com.simon.workspace.storage.model.StorageProviderState;

import java.time.LocalDateTime;

public record StorageProviderResponse(
        String providerCode,
        String providerType,
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

    public static StorageProviderResponse from(StorageProviderState state) {
        return new StorageProviderResponse(
                state.providerCode(),
                state.providerType().name(),
                state.displayName(),
                state.configured(),
                state.enabled(),
                state.active(),
                state.endpoint(),
                state.bucket(),
                state.publicBaseUrl(),
                state.lastTestStatus(),
                state.lastTestMessage(),
                state.lastTestTime()
        );
    }
}
