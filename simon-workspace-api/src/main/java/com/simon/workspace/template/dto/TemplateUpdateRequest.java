package com.simon.workspace.template.dto;

import jakarta.validation.constraints.NotBlank;

public record TemplateUpdateRequest(
        @NotBlank(message = "模板名称不能为空")
        String templateName,

        String templateType,
        String description,
        String status
) {
}
