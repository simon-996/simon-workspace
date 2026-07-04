package com.simon.workspace.common.error;

import java.util.Map;

public record FieldErrorItem(
        String field,
        String errorCode,
        String message,
        Map<String, Object> params
) {
    public FieldErrorItem {
        params = params == null ? Map.of() : Map.copyOf(params);
    }
}
