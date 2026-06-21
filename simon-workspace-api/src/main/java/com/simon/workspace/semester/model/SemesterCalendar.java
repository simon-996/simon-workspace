package com.simon.workspace.semester.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SemesterCalendar(
        long id,
        long semesterId,
        Integer weekNo,
        LocalDate startDate,
        LocalDate endDate,
        Boolean examWeek,
        Boolean holiday,
        String holidayNote,
        String adjustmentNote,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
