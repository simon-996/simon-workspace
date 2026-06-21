package com.simon.workspace.generation.dto;

import com.simon.workspace.generation.model.GenerationTask;

import java.time.LocalDateTime;

public record GenerationTaskResponse(
        String id,
        String ownerUserId,
        String taskType,
        String taskName,
        String courseId,
        String classId,
        String semesterId,
        String templateId,
        String status,
        String inputJson,
        String resultSummary,
        String failureReason,
        LocalDateTime startedTime,
        LocalDateTime finishedTime,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
    public static GenerationTaskResponse from(GenerationTask task) {
        return new GenerationTaskResponse(
                String.valueOf(task.id()),
                String.valueOf(task.ownerUserId()),
                task.taskType(),
                task.taskName(),
                stringId(task.courseId()),
                stringId(task.classId()),
                stringId(task.semesterId()),
                stringId(task.templateId()),
                task.status(),
                task.inputJson(),
                task.resultSummary(),
                task.failureReason(),
                task.startedTime(),
                task.finishedTime(),
                task.createdTime(),
                task.updatedTime()
        );
    }

    private static String stringId(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
