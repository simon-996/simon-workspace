package com.simon.workspace.blog.dto;

import jakarta.validation.constraints.NotBlank;

public record BlogCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        String name,
        String slug,
        String description,
        Integer sortOrder,
        String status
) {
}
