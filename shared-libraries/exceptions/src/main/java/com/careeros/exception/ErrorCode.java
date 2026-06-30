package com.careeros.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Canonical catalogue of application error codes.
 *
 * <p>Each constant binds a stable, client-facing {@code code} (never reuse or repurpose a code once
 * shipped) to a default HTTP status and message. New modules should add codes here rather than
 * inventing ad-hoc strings, so that error semantics stay centralized and documentable.
 */
@Getter
public enum ErrorCode {

    // ----- Generic (000–099) -----
    INTERNAL_ERROR("CRS-000", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    VALIDATION_FAILED("CRS-001", HttpStatus.BAD_REQUEST, "Request validation failed"),
    MALFORMED_REQUEST("CRS-002", HttpStatus.BAD_REQUEST, "The request could not be read"),
    METHOD_NOT_ALLOWED("CRS-003", HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported"),
    CONSTRAINT_VIOLATION("CRS-004", HttpStatus.BAD_REQUEST, "A constraint was violated"),

    // ----- Resource (100–199) -----
    RESOURCE_NOT_FOUND("CRS-100", HttpStatus.NOT_FOUND, "The requested resource was not found"),
    RESOURCE_CONFLICT("CRS-101", HttpStatus.CONFLICT, "The resource already exists or is in conflict"),

    // ----- Auth & Access (200–299) -----
    UNAUTHENTICATED("CRS-200", HttpStatus.UNAUTHORIZED, "Authentication is required"),
    ACCESS_DENIED("CRS-201", HttpStatus.FORBIDDEN, "You do not have permission to perform this action"),
    INVALID_TOKEN("CRS-202", HttpStatus.UNAUTHORIZED, "The provided token is invalid or expired"),

    // ----- Business (300–399) -----
    BUSINESS_RULE_VIOLATION("CRS-300", HttpStatus.UNPROCESSABLE_ENTITY, "A business rule was violated"),

    // ----- Downstream / Integration (400–499) -----
    DEPENDENCY_UNAVAILABLE("CRS-400", HttpStatus.SERVICE_UNAVAILABLE, "A downstream dependency is unavailable"),
    RATE_LIMITED("CRS-401", HttpStatus.TOO_MANY_REQUESTS, "Too many requests");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
