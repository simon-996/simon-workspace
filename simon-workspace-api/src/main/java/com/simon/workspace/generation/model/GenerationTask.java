package com.simon.workspace.generation.model;

import java.time.LocalDateTime;

public record GenerationTask(
        long id,
        long ownerUserId,
        String taskType,
        String taskName,
        Long courseId,
        Long classId,
        Long semesterId,
        Long templateId,
        String status,
        String inputJson,
        String resultSummary,
        String failureReason,
        LocalDateTime startedTime,
        LocalDateTime finishedTime,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
