package com.simon.workspace.generation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonPreviewRequest(
        @NotNull(message = "课程不能为空")
        Long courseId,

        @NotNull(message = "班级不能为空")
        Long classId,

        @NotNull(message = "学期不能为空")
        Long semesterId,

        @NotNull(message = "周次不能为空")
        @Min(value = 1, message = "周次必须大于 0")
        Integer weekNo,

        @NotNull(message = "模板不能为空")
        Long templateId,

        @Size(max = 200, message = "主题不能超过 200 个字符")
        String topic
) {
}
