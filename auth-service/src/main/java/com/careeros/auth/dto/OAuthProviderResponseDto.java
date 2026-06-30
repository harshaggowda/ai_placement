package com.careeros.auth.dto;

import com.careeros.auth.entity.OAuthProviderType;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound view of a linked external identity provider. Read-only.
 */
public record OAuthProviderResponseDto(
        UUID id,
        OAuthProviderType provider,
        String providerUserId,
        String email,
        Instant createdAt
) {
}
