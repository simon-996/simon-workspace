package com.simon.workspace.semester.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Semester(
        long id,
        String academicYear,
        String semesterName,
        LocalDate startDate,
        LocalDate endDate,
        Integer totalWeeks,
        Integer examWeek,
        String holidayJson,
        String adjustmentJson,
        String remark,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
