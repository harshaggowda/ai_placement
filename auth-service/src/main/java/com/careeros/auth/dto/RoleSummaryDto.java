package com.careeros.auth.dto;

import java.util.UUID;

/**
 * Lightweight role projection for lists and references.
 */
public record RoleSummaryDto(
        UUID id,
        String name
) {
}
