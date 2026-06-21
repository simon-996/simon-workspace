package com.simon.workspace.generation.dto;

import java.time.LocalDateTime;

public record DocumentDataResponse(
        String id,
        String taskId,
        String documentType,
        String dataJson,
        String editedJson,
        String version,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
