package com.careeros.auth.service;

import com.careeros.auth.entity.EmailVerificationToken;
import com.careeros.auth.entity.PasswordResetToken;
import com.careeros.auth.entity.RefreshToken;
import com.careeros.auth.entity.User;

import java.util.UUID;

/**
 * Issues and validates the opaque, single-purpose tokens owned by the auth service.
 *
 * <p>These are NOT JWTs: a cryptographically-random value is returned to the caller while only its
 * SHA-256 hash is persisted, so a database leak never exposes a usable token. Covers refresh tokens
 * (with rotation/revocation), email-verification tokens, and password-reset tokens.
 */
public interface TokenService {

    /** Issue a new refresh token for the user; returns the raw token (only its hash is stored). */
    String issueRefreshToken(User user);

    /** Validate a raw refresh token (exists, not revoked, not expired) or throw. */
    RefreshToken validateRefreshToken(String rawToken);

    /** Revoke {@code current} and issue a replacement; returns the new raw refresh token. */
    String rotateRefreshToken(RefreshToken current, User user);

    /** Revoke a single refresh token by its raw value (logout). No-op if unknown. */
    void revokeRefreshToken(String rawToken);

    /** Revoke every active refresh token for a user (e.g. after a password reset). */
    void revokeAllRefreshTokens(UUID userId);

    /** Issue an email-verification token; returns the raw token. */
    String issueEmailVerificationToken(User user);

    /** Consume a raw email-verification token (valid, unused, unexpired) or throw. */
    EmailVerificationToken consumeEmailVerificationToken(String rawToken);

    /** Issue a password-reset token; returns the raw token. */
    String issuePasswordResetToken(User user);

    /** Consume a raw password-reset token (valid, unused, unexpired) or throw. */
    PasswordResetToken consumePasswordResetToken(String rawToken);
}
