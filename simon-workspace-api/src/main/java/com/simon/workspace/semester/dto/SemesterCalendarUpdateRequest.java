package com.simon.workspace.semester.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SemesterCalendarUpdateRequest(
        @NotNull(message = "周开始日期不能为空")
        LocalDate startDate,

        @NotNull(message = "周结束日期不能为空")
        LocalDate endDate,

        Boolean examWeek,
        Boolean holiday,
        String holidayNote,
        String adjustmentNote
) {
}
