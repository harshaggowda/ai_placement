package com.careeros.auth.dto;

import java.util.UUID;

/**
 * Lightweight permission projection for lists and references.
 */
public record PermissionSummaryDto(
        UUID id,
        String name
) {
}
