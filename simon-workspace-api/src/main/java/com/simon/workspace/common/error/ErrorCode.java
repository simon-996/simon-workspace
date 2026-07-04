package com.simon.workspace.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_FAILED(40001, HttpStatus.BAD_REQUEST, "Validation failed"),
    BAD_REQUEST(40002, HttpStatus.BAD_REQUEST, "Bad request"),
    AUTH_UNAUTHORIZED(40101, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    AUTH_BAD_CREDENTIALS(40102, HttpStatus.UNAUTHORIZED, "Invalid username or password"),
    AUTH_ACCOUNT_DISABLED(40103, HttpStatus.FORBIDDEN, "Account disabled"),
    AUTH_FORBIDDEN(40301, HttpStatus.FORBIDDEN, "Forbidden"),
    RESOURCE_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(40901, HttpStatus.CONFLICT, "Conflict"),
    BUSINESS_ERROR(42201, HttpStatus.UNPROCESSABLE_ENTITY, "Business rule failed"),
    INTERNAL_ERROR(50001, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final int code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(int code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int code() {
        return code;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
