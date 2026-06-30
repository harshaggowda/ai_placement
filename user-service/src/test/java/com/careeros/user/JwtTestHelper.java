package com.careeros.user;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Utility that mints valid and invalid JWT tokens for controller and security tests.
 *
 * <p>Uses the same Base64 secret configured in {@code application-test.yml} so the
 * {@link com.careeros.security.JwtTokenValidator} will accept the tokens.
 */
public final class JwtTestHelper {

    /**
     * The test secret — must match {@code careeros.security.jwt.secret} in application-test.yml.
     */
    public static final String TEST_SECRET =
            "Y2FyZWVyb3MtYWktbG9jYWwtZGV2LWFuZC10ZXN0LXNpZ25pbmctc2VjcmV0LWtleS0yNTZiaXQ=";

    public static final String TEST_ISSUER = "careeros-ai";

    private static final SecretKey SIGNING_KEY =
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));

    private JwtTestHelper() {}

    /** Builds a valid Bearer token string for the given userId and email. */
    public static String bearerToken(UUID userId, String email, String... roles) {
        return "Bearer " + buildToken(userId, email, Instant.now().plusSeconds(900), roles);
    }

    /** Builds a valid Bearer token for a random user. */
    public static String bearerToken(UUID userId) {
        return bearerToken(userId, userId + "@test.com", "ROLE_USER");
    }

    /** Builds an already-expired Bearer token (for security rejection tests). */
    public static String expiredBearerToken(UUID userId) {
        return "Bearer " + buildToken(userId, userId + "@test.com",
                Instant.now().minusSeconds(60), "ROLE_USER");
    }

    /** Builds a token signed with a DIFFERENT key (for signature-tamper tests). */
    public static String tamperedBearerToken(UUID userId) {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode("d3JvbmdrZXktZm9yLXRlc3RpbmctdGFtcGVyZWQtdG9rZW5zLTI1NmJpdA=="));
        String token = Jwts.builder()
                .issuer(TEST_ISSUER)
                .subject(userId.toString())
                .claim("email", userId + "@test.com")
                .claim("authorities", List.of("ROLE_USER"))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(900)))
                .signWith(wrongKey)
                .compact();
        return "Bearer " + token;
    }

    private static String buildToken(UUID userId, String email, Instant expiry, String... roles) {
        return Jwts.builder()
                .issuer(TEST_ISSUER)
                .subject(userId.toString())
                .claim("email", email)
                .claim("authorities", List.of(roles))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiry))
                .signWith(SIGNING_KEY)
                .compact();
    }
}
