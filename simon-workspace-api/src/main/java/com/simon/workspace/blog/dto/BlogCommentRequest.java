package com.simon.workspace.blog.dto;

import jakarta.validation.constraints.NotBlank;

public record BlogCommentRequest(
        Long parentId,
        @NotBlank(message = "评论内容不能为空")
        String content
) {
}
