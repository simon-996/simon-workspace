package com.simon.workspace.template.dto;

import jakarta.validation.constraints.NotBlank;

public record TemplateFieldRequest(
        @NotBlank(message = "字段键不能为空")
        String fieldKey,

        String fieldLabel,
        String fieldType,
        Boolean required,
        String defaultValue,
        Integer sortOrder,
        String remark,
        String status
) {
}
