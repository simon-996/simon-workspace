package com.simon.workspace.template.dto;

import com.simon.workspace.template.model.TemplateFile;

import java.time.LocalDateTime;

public record TemplateResponse(
        String id,
        String templateName,
        String templateType,
        String originalFilename,
        Long fileSize,
        String contentType,
        String description,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
    public static TemplateResponse from(TemplateFile templateFile) {
        return new TemplateResponse(
                String.valueOf(templateFile.id()),
                templateFile.templateName(),
                templateFile.templateType(),
                templateFile.originalFilename(),
                templateFile.fileSize(),
                templateFile.contentType(),
                templateFile.description(),
                templateFile.status(),
                templateFile.createdTime(),
                templateFile.updatedTime()
        );
    }
}
