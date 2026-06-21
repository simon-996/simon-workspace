package com.simon.workspace.generation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CalendarPreviewRequest(
        @NotNull(message = "课程不能为空")
        Long courseId,

        @NotNull(message = "学期不能为空")
        Long semesterId,

        @NotNull(message = "模板不能为空")
        Long templateId,

        @Size(max = 200, message = "主题不能超过 200 个字符")
        String topic
) {
}
