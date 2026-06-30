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
 * Mints JWTs signed with the test secret so integration tests can authenticate against the real
 * {@code JwtAuthenticationFilter} (exercising the actual security chain rather than mocking it).
 */
public final class JwtTestTokens {

    private static final String SECRET =
            "Y2FyZWVyb3MtYWktbG9jYWwtZGV2LWFuZC10ZXN0LXNpZ25pbmctc2VjcmV0LWtleS0yNTZiaXQ=";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

    private JwtTestTokens() {
    }

    public static String accessToken(UUID userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer("careeros-ai")
                .subject(userId.toString())
                .claim("email", email)
                .claim("authorities", List.of("ROLE_USER"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(KEY)
                .compact();
    }
}
