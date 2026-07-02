package com.simon.workspace.semester;

import com.simon.workspace.auth.permission.RequirePermission;
import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.semester.dto.SemesterCalendarResponse;
import com.simon.workspace.semester.dto.SemesterCalendarUpdateRequest;
import com.simon.workspace.semester.dto.SemesterRequest;
import com.simon.workspace.semester.dto.SemesterResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
@RequirePermission("semester:manage")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    public ApiResponse<List<SemesterResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(semesterService.list(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<SemesterResponse> detail(@PathVariable long id) {
        return ApiResponse.ok(semesterService.detail(id));
    }

    @PostMapping
    public ApiResponse<SemesterResponse> create(@Valid @RequestBody SemesterRequest request) {
        return ApiResponse.ok(semesterService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SemesterResponse> update(@PathVariable long id, @Valid @RequestBody SemesterRequest request) {
        return ApiResponse.ok(semesterService.update(id, request));
    }

    @PostMapping("/{id}/calendar/generate")
    public ApiResponse<List<SemesterCalendarResponse>> generateCalendar(@PathVariable long id) {
        return ApiResponse.ok(semesterService.generateCalendar(id));
    }

    @GetMapping("/{id}/calendar")
    public ApiResponse<List<SemesterCalendarResponse>> calendar(@PathVariable long id) {
        return ApiResponse.ok(semesterService.calendar(id));
    }

    @PutMapping("/{id}/calendar/{calendarId}")
    public ApiResponse<SemesterCalendarResponse> updateCalendar(
            @PathVariable long id,
            @PathVariable long calendarId,
            @Valid @RequestBody SemesterCalendarUpdateRequest request
    ) {
        return ApiResponse.ok(semesterService.updateCalendar(id, calendarId, request));
    }
}
