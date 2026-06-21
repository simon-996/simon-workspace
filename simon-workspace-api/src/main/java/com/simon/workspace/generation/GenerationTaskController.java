package com.simon.workspace.generation;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.generation.dto.DocumentDataResponse;
import com.simon.workspace.generation.dto.DocumentDataUpdateRequest;
import com.simon.workspace.generation.dto.GenerationTaskResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/generation/tasks")
public class GenerationTaskController {

    private final GenerationTaskService generationTaskService;
    private final GenerationPreviewService generationPreviewService;

    public GenerationTaskController(
            GenerationTaskService generationTaskService,
            GenerationPreviewService generationPreviewService
    ) {
        this.generationTaskService = generationTaskService;
        this.generationPreviewService = generationPreviewService;
    }

    @GetMapping
    public ApiResponse<List<GenerationTaskResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(generationTaskService.list(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<GenerationTaskResponse> detail(@PathVariable long id) {
        return ApiResponse.ok(generationTaskService.detail(id));
    }

    @PutMapping("/{id}/document")
    public ApiResponse<DocumentDataResponse> updateDocument(
            @PathVariable long id,
            @Valid @RequestBody DocumentDataUpdateRequest request
    ) {
        return ApiResponse.ok(generationPreviewService.updateDocument(id, request));
    }
}
