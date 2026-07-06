package com.simon.workspace.blog.dto;

public record BlogTagResponse(
        String id,
        String name,
        String slug,
        Long usageCount
) {
}
