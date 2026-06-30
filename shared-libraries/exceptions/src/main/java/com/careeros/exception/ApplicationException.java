package com.careeros.exception;

import lombok.Getter;

/**
 * Base type for all deliberate, application-level exceptions.
 *
 * <p>Carries an {@link ErrorCode} so the global handler can translate it into a consistent HTTP
 * response without instanceof ladders. Feature modules should subclass this (or {@link BusinessException})
 * rather than throwing raw {@link RuntimeException}s.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {

    private final transient ErrorCode errorCode;

    protected ApplicationException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    protected ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
