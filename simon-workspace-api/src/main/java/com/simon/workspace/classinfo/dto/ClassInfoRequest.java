package com.simon.workspace.classinfo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ClassInfoRequest(
        @NotBlank(message = "班级名称不能为空")
        String className,

        String major,
        String grade,

        @Min(value = 0, message = "学生人数不能小于 0")
        Integer studentCount,

        String counselor,
        String remark
) {
}
