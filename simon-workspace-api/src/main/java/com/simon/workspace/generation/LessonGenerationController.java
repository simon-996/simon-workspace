package com.simon.workspace.generation;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.generation.dto.LessonPreviewRequest;
import com.simon.workspace.generation.dto.LessonPreviewResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generation/lessons")
public class LessonGenerationController {

    private final GenerationPreviewService generationPreviewService;

    public LessonGenerationController(GenerationPreviewService generationPreviewService) {
        this.generationPreviewService = generationPreviewService;
    }

    @PostMapping("/preview")
    public ApiResponse<LessonPreviewResponse> preview(@Valid @RequestBody LessonPreviewRequest request) {
        return ApiResponse.ok(generationPreviewService.createLessonPreview(request));
    }
}
