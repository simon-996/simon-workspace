package com.simon.workspace.blog.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record BlogPostRequest(
        @NotBlank(message = "文章标题不能为空")
        String title,
        String summary,
        String slug,
        Long categoryId,
        List<String> tags,
        @NotBlank(message = "文章内容不能为空")
        String contentMd,
        String status
) {
    public BlogPostRequest {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
