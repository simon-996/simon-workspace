package com.simon.workspace.template.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TemplateFieldsRequest(
        @NotNull(message = "字段列表不能为空")
        List<@Valid TemplateFieldRequest> fields
) {
}
