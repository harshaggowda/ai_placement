package com.careeros.exception;

/**
 * Thrown when a business invariant or rule is violated.
 *
 * <p>Defaults to {@link ErrorCode#BUSINESS_RULE_VIOLATION} but accepts any {@link ErrorCode} so that
 * modules can be specific while keeping a single catchable type for the rule-violation family.
 */
public class BusinessException extends ApplicationException {

    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
