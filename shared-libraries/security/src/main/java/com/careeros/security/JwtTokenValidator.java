package com.careeros.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Verifies and parses JWT access tokens issued by the auth service.
 *
 * <p>Validation-only — every service depends on this to authenticate inbound tokens; <strong>only
 * auth-service signs/issues tokens</strong>. Verifies the HMAC signature, the required issuer, and
 * (implicitly) the expiry. The claim names match those written by auth-service's token issuer.
 */
@Component
public class JwtTokenValidator {

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_AUTHORITIES = "authorities";

    private final SecretKey signingKey;
    private final String issuer;

    public JwtTokenValidator(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret()));
        this.issuer = properties.getIssuer();
    }

    /** Verify and parse a token's claims. Throws {@code JwtException} if invalid/expired/wrong issuer. */
    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
