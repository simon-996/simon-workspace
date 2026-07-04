package com.simon.workspace.file.model;

import java.time.LocalDateTime;

public record FileResource(
        long id,
        long ownerUserId,
        String sourceType,
        String originalFilename,
        String storagePath,
        String storageProvider,
        String objectKey,
        String visibility,
        String publicUrl,
        long fileSize,
        String contentType,
        String fileExtension,
        String status,
        LocalDateTime orphanedTime,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
