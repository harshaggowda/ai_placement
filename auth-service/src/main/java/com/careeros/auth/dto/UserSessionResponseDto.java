package com.careeros.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound view of a user session (for "active devices" listings). Read-only.
 */
public record UserSessionResponseDto(
        UUID id,
        String ipAddress,
        String userAgent,
        Instant lastSeenAt,
        Instant expiresAt,
        Instant revokedAt,
        Instant createdAt
) {
}
