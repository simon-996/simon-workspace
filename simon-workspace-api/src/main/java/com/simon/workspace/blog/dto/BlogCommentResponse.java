package com.simon.workspace.blog.dto;

import java.time.LocalDateTime;

public record BlogCommentResponse(
        String id,
        String postId,
        String parentId,
        String authorName,
        String content,
        String status,
        LocalDateTime createdTime
) {
}
