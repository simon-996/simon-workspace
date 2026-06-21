package com.simon.workspace.file.dto;

import com.simon.workspace.file.model.FileResource;

import java.time.LocalDateTime;

public record FileResourceResponse(
        String id,
        String ownerUserId,
        String sourceType,
        String originalFilename,
        Long fileSize,
        String contentType,
        String fileExtension,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
    public static FileResourceResponse from(FileResource fileResource) {
        return new FileResourceResponse(
                String.valueOf(fileResource.id()),
                String.valueOf(fileResource.ownerUserId()),
                fileResource.sourceType(),
                fileResource.originalFilename(),
                fileResource.fileSize(),
                fileResource.contentType(),
                fileResource.fileExtension(),
                fileResource.status(),
                fileResource.createdTime(),
                fileResource.updatedTime()
        );
    }
}
