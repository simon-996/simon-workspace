package com.simon.workspace.course.dto;

import com.simon.workspace.course.model.CourseMaterial;

import java.time.LocalDateTime;

public record CourseMaterialResponse(
        String id,
        String courseId,
        String section,
        String materialType,
        String fileId,
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
    public static CourseMaterialResponse from(CourseMaterial material) {
        return new CourseMaterialResponse(
                String.valueOf(material.id()),
                String.valueOf(material.courseId()),
                material.section(),
                material.materialType(),
                material.fileId() == null ? null : String.valueOf(material.fileId()),
                material.externalUrl(),
                material.title(),
                material.description(),
                material.sortOrder(),
                material.status(),
                material.originalFilename(),
                material.publicUrl(),
                material.contentType(),
                material.fileExtension(),
                material.fileSize(),
                material.createdTime(),
                material.updatedTime()
        );
    }
}
