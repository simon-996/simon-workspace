package com.simon.workspace.common.error;

import java.util.Map;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> params;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, Map.of());
    }

    public BusinessException(ErrorCode errorCode, Map<String, Object> params) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.params = params == null ? Map.of() : Map.copyOf(params);
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Map<String, Object> params() {
        return params;
    }
}
