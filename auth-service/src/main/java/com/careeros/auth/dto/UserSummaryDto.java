package com.careeros.auth.dto;

import com.careeros.auth.entity.UserStatus;

import java.util.UUID;

/**
 * Lightweight user projection for lists and references.
 */
public record UserSummaryDto(
        UUID id,
        String email,
        String displayName,
        UserStatus status
) {
}
