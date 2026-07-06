package com.simon.workspace.course.dto;

public record CourseMaterialRequest(
        String section,
        String materialType,
        Long fileId,
        String externalUrl,
        String title,
        String description,
        Integer sortOrder,
        String status
) {
}
