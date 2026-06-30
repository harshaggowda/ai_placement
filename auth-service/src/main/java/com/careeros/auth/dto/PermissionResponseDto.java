package com.careeros.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Full outbound representation of a permission.
 */
public record PermissionResponseDto(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
