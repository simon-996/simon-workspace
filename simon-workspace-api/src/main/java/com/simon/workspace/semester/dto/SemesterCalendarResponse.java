package com.simon.workspace.semester.dto;

import com.simon.workspace.semester.model.SemesterCalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SemesterCalendarResponse(
        String id,
        String semesterId,
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
    public static SemesterCalendarResponse from(SemesterCalendar calendar) {
        return new SemesterCalendarResponse(
                String.valueOf(calendar.id()),
                String.valueOf(calendar.semesterId()),
                calendar.weekNo(),
                calendar.startDate(),
                calendar.endDate(),
                calendar.examWeek(),
                calendar.holiday(),
                calendar.holidayNote(),
                calendar.adjustmentNote(),
                calendar.createdTime(),
                calendar.updatedTime()
        );
    }
}
