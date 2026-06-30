package com.careeros.exception;

/**
 * Thrown for unexpected, non-recoverable server-side failures that should surface as HTTP 500.
 *
 * <p>Resolves to {@link ErrorCode#INTERNAL_ERROR}. Prefer a more specific exception where one
 * applies; this exists so deliberate "this should never happen" paths still carry the platform
 * error contract. The global handler logs full detail and returns only a generic message + trace id.
 */
public class InternalServerException extends ApplicationException {

    public InternalServerException(String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, message, cause);
    }
}
