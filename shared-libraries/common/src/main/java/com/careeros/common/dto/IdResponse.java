package com.careeros.common.dto;

/**
 * Minimal response carrying a single identifier.
 *
 * <p>Reusable across services for create/command endpoints that only need to return the id of the
 * affected resource (e.g. {@code 201 Created}), keeping such endpoints free of bespoke DTOs.
 *
 * @param <T> identifier type (e.g. {@code Long}, {@code UUID}, {@code String})
 */
public record IdResponse<T>(T id) {

    public static <T> IdResponse<T> of(T id) {
        return new IdResponse<>(id);
    }
}
