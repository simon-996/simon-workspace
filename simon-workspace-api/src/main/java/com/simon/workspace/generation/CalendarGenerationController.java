package com.simon.workspace.generation;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.generation.dto.CalendarPreviewRequest;
import com.simon.workspace.generation.dto.CalendarPreviewResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generation/calendars")
public class CalendarGenerationController {

    private final GenerationPreviewService generationPreviewService;

    public CalendarGenerationController(GenerationPreviewService generationPreviewService) {
        this.generationPreviewService = generationPreviewService;
    }

    @PostMapping("/preview")
    public ApiResponse<CalendarPreviewResponse> preview(@Valid @RequestBody CalendarPreviewRequest request) {
        return ApiResponse.ok(generationPreviewService.createCalendarPreview(request));
    }
}
