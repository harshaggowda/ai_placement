package com.careeros.auth.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Full outbound representation of a role, including its permission names.
 */
public record RoleResponseDto(
        UUID id,
        String name,
        String description,
        Set<String> permissions,
        Instant createdAt,
        Instant updatedAt
) {
}
