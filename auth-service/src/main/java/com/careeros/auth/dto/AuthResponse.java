package com.careeros.auth.dto;

/**
 * Authentication result returned by login and registration-with-auto-login flows.
 *
 * @param accessToken      signed JWT access token
 * @param refreshToken     opaque refresh token (store securely; rotated on each refresh)
 * @param tokenType        always {@code "Bearer"}
 * @param expiresInSeconds access-token lifetime in seconds
 * @param user             the authenticated user's profile
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserResponseDto user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInSeconds, UserResponseDto user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, user);
    }
}
