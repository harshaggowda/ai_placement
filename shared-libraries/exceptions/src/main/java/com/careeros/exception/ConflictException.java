package com.careeros.exception;

/**
 * Thrown when a request conflicts with current resource state (e.g. a duplicate or a concurrent
 * modification).
 *
 * <p>Resolves to HTTP 409 via {@link ErrorCode#RESOURCE_CONFLICT}.
 */
public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT, message);
    }

    public ConflictException(String resource, String field, Object value) {
        super(ErrorCode.RESOURCE_CONFLICT,
                "%s with %s '%s' already exists".formatted(resource, field, value));
    }
}
