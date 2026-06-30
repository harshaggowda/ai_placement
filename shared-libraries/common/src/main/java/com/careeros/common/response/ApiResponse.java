package com.careeros.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Uniform response envelope returned by every REST endpoint in the platform.
 *
 * <p>A single, predictable shape ({@code success}, {@code message}, {@code data}, {@code error},
 * {@code traceId}, {@code timestamp}) lets clients and gateways parse responses generically and
 * correlate them with server logs via {@code traceId}.
 *
 * @param <T> payload type carried on success
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final ErrorResponse error;
    private final String traceId;
    private final Instant timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Request completed successfully");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorResponse error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(error.getMessage())
                .error(error)
                .traceId(error.getTraceId())
                .timestamp(Instant.now())
                .build();
    }
}
