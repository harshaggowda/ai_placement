package com.careeros.exception;

import com.careeros.common.response.ApiResponse;
import com.careeros.common.response.ErrorResponse;
import com.careeros.logging.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

/**
 * Single point of translation from exceptions to the platform's {@link ApiResponse} error envelope.
 *
 * <p>Keeps controllers free of try/catch boilerplate and guarantees that every error — whether a
 * deliberate {@link ApplicationException}, a framework validation failure, a security exception, or
 * an unexpected runtime fault — is logged with the request {@code traceId} and returned in one
 * consistent shape. Unexpected errors never leak stack traces or internal messages to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------------------------------------ deliberate application errors

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException ex,
                                                                        HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        log.warn("Application error [{}]: {}", code.getCode(), ex.getMessage());
        return build(code, ex.getMessage(), request, null);
    }

    // ------------------------------------------------------------------ request validation

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                          HttpServletRequest request) {
        ErrorResponse.ErrorResponseBuilder builder = baseError(ErrorCode.VALIDATION_FAILED, request);
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            builder.fieldError(ErrorResponse.FieldViolation.builder()
                    .field(fieldError.getField())
                    .rejectedValue(fieldError.getRejectedValue())
                    .message(fieldError.getDefaultMessage())
                    .build());
        }
        log.warn("Validation failed for {}: {} field error(s)",
                request.getRequestURI(), ex.getBindingResult().getFieldErrorCount());
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(ApiResponse.error(builder.build()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex,
                                                                       HttpServletRequest request) {
        ErrorResponse.ErrorResponseBuilder builder = baseError(ErrorCode.CONSTRAINT_VIOLATION, request);
        ex.getConstraintViolations().forEach(violation -> builder.fieldError(
                ErrorResponse.FieldViolation.builder()
                        .field(String.valueOf(violation.getPropertyPath()))
                        .rejectedValue(violation.getInvalidValue())
                        .message(violation.getMessage())
                        .build()));
        return ResponseEntity.status(ErrorCode.CONSTRAINT_VIOLATION.getHttpStatus())
                .body(ApiResponse.error(builder.build()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex,
                                                               HttpServletRequest request) {
        log.warn("Malformed request body for {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.MALFORMED_REQUEST, ErrorCode.MALFORMED_REQUEST.getDefaultMessage(), request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                      HttpServletRequest request) {
        return build(ErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), request, null);
    }

    // ------------------------------------------------------------------ security

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex,
                                                                  HttpServletRequest request) {
        log.warn("Authentication failure for {}: {}", request.getRequestURI(), ex.getMessage());
        return build(ErrorCode.UNAUTHENTICATED, ErrorCode.UNAUTHENTICATED.getDefaultMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex,
                                                                HttpServletRequest request) {
        log.warn("Access denied for {}: {}", request.getRequestURI(), ex.getMessage());
        return build(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage(), request, null);
    }

    // ------------------------------------------------------------------ catch-all

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, WebRequest request) {
        // Full detail goes to logs only; the client receives a generic message + traceId for support.
        log.error("Unhandled exception", ex);
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_ERROR.getCode())
                .message(ErrorCode.INTERNAL_ERROR.getDefaultMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .traceId(currentTraceId())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error));
    }

    // ------------------------------------------------------------------ helpers

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode code, String message,
                                                    HttpServletRequest request, Object ignored) {
        ErrorResponse error = baseError(code, request).message(message).build();
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.error(error));
    }

    private ErrorResponse.ErrorResponseBuilder baseError(ErrorCode code, HttpServletRequest request) {
        return ErrorResponse.builder()
                .code(code.getCode())
                .message(code.getDefaultMessage())
                .status(code.getHttpStatus().value())
                .path(request.getRequestURI())
                .traceId(currentTraceId())
                .timestamp(Instant.now());
    }

    private String currentTraceId() {
        return MDC.get(CorrelationIdFilter.TRACE_ID_KEY);
    }
}
