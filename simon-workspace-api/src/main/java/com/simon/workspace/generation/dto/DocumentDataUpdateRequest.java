package com.simon.workspace.generation.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentDataUpdateRequest(
        @NotBlank(message = "编辑内容不能为空")
        String editedJson
) {
}
