package com.simon.workspace.blog.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BlogPostDetailResponse(
        String id,
        String title,
        String summary,
        String slug,
        String contentMd,
        String status,
        String authorName,
        BlogCategoryResponse category,
        List<BlogTagResponse> tags,
        Long viewCount,
        Long commentCount,
        LocalDateTime publishedTime,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
    public BlogPostDetailResponse {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
