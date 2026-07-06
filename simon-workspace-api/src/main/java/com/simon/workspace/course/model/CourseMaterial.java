package com.simon.workspace.course.model;

import java.time.LocalDateTime;

public record CourseMaterial(
        long id,
        long courseId,
        String section,
        String materialType,
        Long fileId,
        String externalUrl,
        String title,
        String description,
        Integer sortOrder,
        String status,
        String originalFilename,
        String publicUrl,
        String contentType,
        String fileExtension,
        Long fileSize,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
