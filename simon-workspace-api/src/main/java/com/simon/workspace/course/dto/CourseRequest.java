package com.simon.workspace.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CourseRequest(
        @NotBlank(message = "课程名称不能为空")
        String courseName,

        String courseCode,
        String major,
        String grade,

        @NotNull(message = "总学时不能为空")
        @Min(value = 0, message = "总学时不能小于 0")
        Integer totalHours,

        @Min(value = 0, message = "理论学时不能小于 0")
        Integer theoryHours,

        @Min(value = 0, message = "实验学时不能小于 0")
        Integer experimentHours,

        @Min(value = 0, message = "周学时不能小于 0")
        Integer weeklyHours,

        BigDecimal credit,
        String textbook,
        String courseGoal,
        String keyPoint,
        String difficultPoint,
        String assessmentMethod,
        String syllabus,
        String description,
        String status
) {
}
