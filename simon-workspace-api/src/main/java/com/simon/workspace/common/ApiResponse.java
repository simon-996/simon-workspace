package com.simon.workspace.common;

import com.simon.workspace.common.error.ErrorCode;
import com.simon.workspace.common.error.FieldErrorItem;

import java.util.List;
import java.util.Map;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        String errorCode,
        String traceId,
        Map<String, Object> params,
        List<FieldErrorItem> fieldErrors
) {
    public ApiResponse {
        params = params == null ? Map.of() : Map.copyOf(params);
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data, null, null, Map.of(), List.of());
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(
                ErrorCode.INTERNAL_ERROR.code(),
                message,
                null,
                ErrorCode.INTERNAL_ERROR.name(),
                null,
                Map.of(),
                List.of()
        );
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String traceId) {
        return fail(errorCode, traceId, Map.of(), List.of());
    }

    public static <T> ApiResponse<T> fail(
            ErrorCode errorCode,
            String traceId,
            Map<String, Object> params,
            List<FieldErrorItem> fieldErrors
    ) {
        return new ApiResponse<>(
                errorCode.code(),
                errorCode.defaultMessage(),
                null,
                errorCode.name(),
                traceId,
                params,
                fieldErrors
        );
    }
}
