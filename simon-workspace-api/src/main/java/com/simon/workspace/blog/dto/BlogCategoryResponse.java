package com.simon.workspace.blog.dto;

import java.time.LocalDateTime;

public record BlogCategoryResponse(
        String id,
        String name,
        String slug,
        String description,
        Integer sortOrder,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
