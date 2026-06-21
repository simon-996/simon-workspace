package com.simon.workspace.generation.dto;

public record CalendarPreviewResponse(
        GenerationTaskResponse task,
        String documentDataId,
        String dataJson
) {
}
