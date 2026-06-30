package com.careeros.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound view of a single login attempt. Read-only ({@code createdAt} is the attempt time).
 */
public record LoginHistoryResponseDto(
        UUID id,
        String email,
        String ipAddress,
        String userAgent,
        boolean successful,
        String failureReason,
        Instant createdAt
) {
}
