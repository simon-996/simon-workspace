package com.simon.workspace.common.error;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.simon.workspace.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return response(exception.errorCode(), exception.params(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        return response(ErrorCode.VALIDATION_FAILED, Map.of(), fieldErrorsOf(exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return response(ErrorCode.VALIDATION_FAILED, Map.of(), fieldErrorsOf(exception.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        List<FieldErrorItem> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new FieldErrorItem(
                        violation.getPropertyPath().toString(),
                        ErrorCode.VALIDATION_FIELD_INVALID.name(),
                        ErrorCode.VALIDATION_FIELD_INVALID.defaultMessage(),
                        Map.of(
                                "field", violation.getPropertyPath().toString(),
                                "message", violation.getMessage()
                        )
                ))
                .toList();
        return response(ErrorCode.VALIDATION_FAILED, Map.of(), fieldErrors);
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLoginException(NotLoginException exception) {
        return response(ErrorCode.AUTH_UNAUTHORIZED, Map.of(), List.of());
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotPermissionException(NotPermissionException exception) {
        return response(ErrorCode.AUTH_FORBIDDEN, Map.of(), List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return response(ErrorCode.BAD_REQUEST, Map.of("message", safeMessage(exception)), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        String traceId = ErrorTraceId.next();
        log.error("Unhandled API exception, traceId={}", traceId, exception);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.httpStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR, traceId));
    }

    private ResponseEntity<ApiResponse<Void>> response(
            ErrorCode errorCode,
            Map<String, Object> params,
            List<FieldErrorItem> fieldErrors
    ) {
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(ApiResponse.fail(errorCode, ErrorTraceId.next(), params, fieldErrors));
    }

    private List<FieldErrorItem> fieldErrorsOf(List<FieldError> errors) {
        return errors.stream()
                .map(error -> new FieldErrorItem(
                        error.getField(),
                        ErrorCode.VALIDATION_FIELD_INVALID.name(),
                        ErrorCode.VALIDATION_FIELD_INVALID.defaultMessage(),
                        Map.of(
                                "field", error.getField(),
                                "message", error.getDefaultMessage() == null ? "" : error.getDefaultMessage()
                        )
                ))
                .toList();
    }

    private String safeMessage(Exception exception) {
        return exception.getMessage() == null ? "" : exception.getMessage();
    }
}
