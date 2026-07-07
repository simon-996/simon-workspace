package com.simon.workspace.blog.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BlogPostSummaryResponse(
        String id,
        String title,
        String summary,
        String slug,
        String status,
        String authorName,
        String authorUserId,
        BlogCategoryResponse category,
        List<BlogTagResponse> tags,
        Long viewCount,
        Long commentCount,
        LocalDateTime publishedTime,
        LocalDateTime updatedTime
) {
    public BlogPostSummaryResponse {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
