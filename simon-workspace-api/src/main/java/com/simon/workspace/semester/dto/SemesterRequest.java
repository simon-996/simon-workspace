package com.simon.workspace.semester.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SemesterRequest(
        @NotBlank(message = "学年不能为空")
        String academicYear,

        @NotBlank(message = "学期名称不能为空")
        String semesterName,

        @NotNull(message = "开学日期不能为空")
        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "总周数不能为空")
        @Min(value = 1, message = "总周数至少为 1")
        Integer totalWeeks,

        @Min(value = 1, message = "考试周至少为 1")
        Integer examWeek,

        String holidayJson,
        String adjustmentJson,
        String remark,
        String status
) {
}
