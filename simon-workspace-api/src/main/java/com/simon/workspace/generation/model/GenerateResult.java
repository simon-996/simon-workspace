package com.simon.workspace.generation.model;

import java.time.LocalDateTime;

public record GenerateResult(
        long id,
        long taskId,
        long fileResourceId,
        String fileName,
        String fileType,
        String resultType,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
