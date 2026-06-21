package com.simon.workspace.generation.model;

import java.time.LocalDateTime;

public record DocumentData(
        long id,
        long taskId,
        String documentType,
        String dataJson,
        String editedJson,
        String version,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
