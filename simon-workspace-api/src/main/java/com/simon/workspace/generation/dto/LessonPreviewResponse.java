package com.simon.workspace.generation.dto;

public record LessonPreviewResponse(
        GenerationTaskResponse task,
        String documentDataId,
        String dataJson
) {
}
