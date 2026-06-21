package com.simon.workspace.file.model;

import java.time.LocalDateTime;

public record FileResource(
        long id,
        long ownerUserId,
        String sourceType,
        String originalFilename,
        String storagePath,
        long fileSize,
        String contentType,
        String fileExtension,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
