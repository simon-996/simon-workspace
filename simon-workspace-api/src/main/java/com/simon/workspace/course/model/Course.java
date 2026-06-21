package com.simon.workspace.course.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Course(
        long id,
        String courseName,
        String courseCode,
        String major,
        String grade,
        Integer totalHours,
        Integer theoryHours,
        Integer experimentHours,
        Integer weeklyHours,
        BigDecimal credit,
        String textbook,
        String courseGoal,
        String keyPoint,
        String difficultPoint,
        String assessmentMethod,
        String syllabus,
        String description,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
