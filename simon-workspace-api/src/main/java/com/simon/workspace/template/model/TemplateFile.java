package com.simon.workspace.template.model;

import java.time.LocalDateTime;

public record TemplateFile(
        long id,
        String templateName,
        String templateType,
        String originalFilename,
        String storagePath,
        long fileSize,
        String contentType,
        String description,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
