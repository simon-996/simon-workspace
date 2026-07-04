package com.simon.workspace.file.dto;

import com.simon.workspace.file.model.FileResource;

import java.time.LocalDateTime;

public record FileResourceResponse(
        String id,
        String ownerUserId,
        String sourceType,
        String originalFilename,
        String storageProvider,
        String objectKey,
        String visibility,
        String publicUrl,
        Long fileSize,
        String contentType,
        String fileExtension,
        String status,
        LocalDateTime orphanedTime,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
    public static FileResourceResponse from(FileResource fileResource) {
        return new FileResourceResponse(
                String.valueOf(fileResource.id()),
                String.valueOf(fileResource.ownerUserId()),
                fileResource.sourceType(),
                fileResource.originalFilename(),
                fileResource.storageProvider(),
                fileResource.objectKey(),
                fileResource.visibility(),
                fileResource.publicUrl(),
                fileResource.fileSize(),
                fileResource.contentType(),
                fileResource.fileExtension(),
                fileResource.status(),
                fileResource.orphanedTime(),
                fileResource.createdTime(),
                fileResource.updatedTime()
        );
    }
}
