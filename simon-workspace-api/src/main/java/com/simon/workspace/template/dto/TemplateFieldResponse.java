package com.simon.workspace.template.dto;

import com.simon.workspace.template.model.TemplateField;

import java.time.LocalDateTime;

public record TemplateFieldResponse(
        String id,
        String templateId,
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
    public static TemplateFieldResponse from(TemplateField field) {
        return new TemplateFieldResponse(
                String.valueOf(field.id()),
                String.valueOf(field.templateId()),
                field.fieldKey(),
                field.fieldLabel(),
                field.fieldType(),
                field.required(),
                field.defaultValue(),
                field.sortOrder(),
                field.remark(),
                field.status(),
                field.createdTime(),
                field.updatedTime()
        );
    }
}
