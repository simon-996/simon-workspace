package com.simon.workspace.semester.dto;

import com.simon.workspace.semester.model.Semester;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SemesterResponse(
        String id,
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
    public static SemesterResponse from(Semester semester) {
        return new SemesterResponse(
                String.valueOf(semester.id()),
                semester.academicYear(),
                semester.semesterName(),
                semester.startDate(),
                semester.endDate(),
                semester.totalWeeks(),
                semester.examWeek(),
                semester.holidayJson(),
                semester.adjustmentJson(),
                semester.remark(),
                semester.status(),
                semester.createdTime(),
                semester.updatedTime()
        );
    }
}
