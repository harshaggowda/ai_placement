package com.careeros.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request carrying the opaque refresh token to be rotated for a new access token.
 */
public record RefreshTokenRequest(

        @NotBlank
        String refreshToken
) {
}
