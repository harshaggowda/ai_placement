package com.careeros.auth.security;

import com.careeros.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Issues and verifies signed JWT access tokens.
 *
 * <p>Tokens are HMAC-signed with the configured secret and carry the user id ({@code sub}), email,
 * and flattened authorities so downstream authorization is stateless (no per-request DB lookup).
 * Refresh tokens are <em>not</em> JWTs — they are opaque, hashed, and persisted (see TokenService).
 */
@Service
public class JwtService {

    static final String CLAIM_EMAIL = "email";
    static final String CLAIM_AUTHORITIES = "authorities";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret()));
    }

    /** Mint a signed access token for the given principal. */
    public String generateAccessToken(AuthUserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getAccessTokenExpiration());
        List<String> authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(principal.getUserId().toString())
                .claim(CLAIM_EMAIL, principal.getUsername())
                .claim(CLAIM_AUTHORITIES, authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /** Parse and verify a token, returning its claims. Throws {@code JwtException} if invalid/expired. */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Access-token lifetime in seconds (for the {@code expiresIn} response field). */
    public long accessTokenTtlSeconds() {
        return properties.getAccessTokenExpiration().toSeconds();
    }
}
