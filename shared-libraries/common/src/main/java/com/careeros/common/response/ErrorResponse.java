package com.careeros.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.Instant;
import java.util.List;

/**
 * Machine-readable error detail embedded in {@link ApiResponse#getError()}.
 *
 * <p>{@code code} is a stable application error code (see {@code ErrorCode}) that clients may switch
 * on; {@code message} is human-readable; {@code fieldErrors} carries per-field validation failures.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final int status;
    private final String path;
    private final String traceId;
    private final Instant timestamp;

    @Singular
    private final List<FieldViolation> fieldErrors;

    /**
     * Represents a single failed field-level validation constraint.
     */
    @Getter
    @Builder
    public static class FieldViolation {
        private final String field;
        private final Object rejectedValue;
        private final String message;
    }
}
