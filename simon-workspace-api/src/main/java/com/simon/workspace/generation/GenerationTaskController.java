package com.simon.workspace.generation;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.generation.dto.GenerationTaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/generation/tasks")
public class GenerationTaskController {

    private final GenerationTaskService generationTaskService;

    public GenerationTaskController(GenerationTaskService generationTaskService) {
        this.generationTaskService = generationTaskService;
    }

    @GetMapping
    public ApiResponse<List<GenerationTaskResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(generationTaskService.list(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<GenerationTaskResponse> detail(@PathVariable long id) {
        return ApiResponse.ok(generationTaskService.detail(id));
    }
}
