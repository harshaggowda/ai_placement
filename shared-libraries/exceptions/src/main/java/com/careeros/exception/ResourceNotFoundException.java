package com.careeros.exception;

/**
 * Thrown when a requested resource cannot be located.
 *
 * <p>Resolves to HTTP 404 via {@link ErrorCode#RESOURCE_NOT_FOUND}. The convenience constructor
 * formats a consistent "{resource} with {field} '{value}' was not found" message.
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                "%s with %s '%s' was not found".formatted(resource, field, value));
    }
}
