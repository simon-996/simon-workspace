package com.simon.workspace.template.model;

import java.time.LocalDateTime;

public record TemplateField(
        long id,
        long templateId,
        String fieldKey,
        String fieldLabel,
        String fieldType,
        Boolean required,
        String defaultValue,
        Integer sortOrder,
        String remark,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
