package com.careeros.auth.dto;

import com.careeros.auth.entity.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Full outbound representation of a user. Never exposes the password hash.
 */
public record UserResponseDto(
        UUID id,
        String email,
        String displayName,
        UserStatus status,
        boolean emailVerified,
        Instant lastLoginAt,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
