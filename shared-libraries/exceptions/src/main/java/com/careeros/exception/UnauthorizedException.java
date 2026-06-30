package com.careeros.exception;

/**
 * Thrown when a request lacks valid authentication.
 *
 * <p>Resolves to HTTP 401 via {@link ErrorCode#UNAUTHENTICATED}. For an authenticated principal that
 * lacks permission, use access-denied semantics ({@link ErrorCode#ACCESS_DENIED}) instead.
 */
public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHENTICATED, message);
    }

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHENTICATED);
    }
}
