package com.careeros.exception;

/**
 * Thrown when domain-level validation fails (beyond bean-validation annotations).
 *
 * <p>Resolves to HTTP 400 via {@link ErrorCode#VALIDATION_FAILED}. Use for cross-field or
 * stateful rules that {@code @Valid} cannot express; framework constraint failures are already
 * handled separately by the global handler.
 */
public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
