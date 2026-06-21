package com.simon.workspace.generation.model;

public enum GenerationTaskStatus {
    PENDING,
    PREVIEW_READY,
    FILLING_TEMPLATE,
    SUCCESS,
    FAILED,
    CANCELED;

    public boolean terminal() {
        return this == SUCCESS || this == FAILED || this == CANCELED;
    }
}
