package com.careeros.auth.service.impl;

import com.careeros.auth.entity.EmailVerificationToken;
import com.careeros.auth.entity.PasswordResetToken;
import com.careeros.auth.entity.RefreshToken;
import com.careeros.auth.entity.User;
import com.careeros.auth.repository.EmailVerificationTokenRepository;
import com.careeros.auth.repository.PasswordResetTokenRepository;
import com.careeros.auth.repository.RefreshTokenRepository;
import com.careeros.auth.service.TokenService;
import com.careeros.exception.InternalServerException;
import com.careeros.exception.UnauthorizedException;
import com.careeros.exception.ValidationException;
import com.careeros.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Default {@link TokenService}. Tokens are 256-bit random values returned raw to the caller and
 * stored only as SHA-256 hashes. Mutations rely on JPA dirty checking within the caller's
 * transaction; issue operations save explicitly.
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private static final int TOKEN_BYTES = 32;
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);
    private static final Duration PASSWORD_RESET_TTL = Duration.ofHours(1);

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String issueRefreshToken(User user) {
        String raw = generateRawToken();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration()));
        refreshTokenRepository.save(token);
        return raw;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (token.getRevokedAt() != null) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }
        return token;
    }

    @Override
    @Transactional
    public String rotateRefreshToken(RefreshToken current, User user) {
        String raw = generateRawToken();
        String newHash = hash(raw);
        current.setRevokedAt(Instant.now());
        current.setReplacedByTokenHash(newHash);

        RefreshToken next = new RefreshToken();
        next.setUser(user);
        next.setTokenHash(newHash);
        next.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration()));
        refreshTokenRepository.save(next);
        return raw;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> token.setRevokedAt(Instant.now()));
    }

    @Override
    @Transactional
    public void revokeAllRefreshTokens(UUID userId) {
        Instant now = Instant.now();
        refreshTokenRepository.findAllByUserId(userId).stream()
                .filter(token -> token.getRevokedAt() == null)
                .forEach(token -> token.setRevokedAt(now));
    }

    @Override
    @Transactional
    public String issueEmailVerificationToken(User user) {
        String raw = generateRawToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(EMAIL_VERIFICATION_TTL));
        emailVerificationTokenRepository.save(token);
        return raw;
    }

    @Override
    @Transactional(readOnly = true)
    public EmailVerificationToken consumeEmailVerificationToken(String rawToken) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new ValidationException("Invalid verification token"));
        if (token.getVerifiedAt() != null) {
            throw new ValidationException("Verification token has already been used");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException("Verification token has expired");
        }
        return token;
    }

    @Override
    @Transactional
    public String issuePasswordResetToken(User user) {
        String raw = generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(PASSWORD_RESET_TTL));
        passwordResetTokenRepository.save(token);
        return raw;
    }

    @Override
    @Transactional(readOnly = true)
    public PasswordResetToken consumePasswordResetToken(String rawToken) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new ValidationException("Invalid reset token"));
        if (token.getUsedAt() != null) {
            throw new ValidationException("Reset token has already been used");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException("Reset token has expired");
        }
        return token;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerException("SHA-256 algorithm unavailable", e);
        }
    }
}
