package com.careeros.common.dto;

/**
 * Minimal response carrying a single human-readable message.
 *
 * <p>Reusable for acknowledgement-style endpoints that return only a status message and no domain
 * payload.
 */
public record MessageResponse(String message) {

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
