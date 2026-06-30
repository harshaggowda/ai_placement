package com.careeros.auth.security;

import com.careeros.security.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "Y2FyZWVyb3MtYWktbG9jYWwtZGV2LWFuZC10ZXN0LXNpZ25pbmctc2VjcmV0LWtleS0yNTZiaXQ=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer("careeros-ai");
        properties.setAccessTokenExpiration(Duration.ofMinutes(15));
        properties.setRefreshTokenExpiration(Duration.ofDays(7));
        jwtService = new JwtService(properties);
    }

    @Test
    void generatesAndParsesTokenWithSubjectEmailAndAuthorities() {
        UUID userId = UUID.randomUUID();
        AuthUserPrincipal principal = AuthUserPrincipal.fromClaims(
                userId, "user@careeros.ai", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwtService.generateAccessToken(principal);
        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get(JwtService.CLAIM_EMAIL, String.class)).isEqualTo("user@careeros.ai");
        assertThat(claims.getIssuer()).isEqualTo("careeros-ai");
        assertThat(claims.get(JwtService.CLAIM_AUTHORITIES, List.class)).contains("ROLE_USER");
    }

    @Test
    void accessTokenTtlMatchesConfiguration() {
        assertThat(jwtService.accessTokenTtlSeconds()).isEqualTo(Duration.ofMinutes(15).toSeconds());
    }
}
