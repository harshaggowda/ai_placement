package com.careeros.user.security;

import com.careeros.user.AbstractPostgresContainerTest;
import com.careeros.user.JwtTestTokens;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security integration tests for the User Service JWT filter chain.
 *
 * <p>Verifies:
 * <ul>
 *   <li>No token → 401</li>
 *   <li>Valid token → 200 (or appropriate business response)</li>
 *   <li>Expired token → 401</li>
 *   <li>Wrong issuer → 401</li>
 *   <li>Tampered signature → 401</li>
 *   <li>OPTIONS preflight → 200 (CORS)</li>
 *   <li>Public endpoints (actuator/health) → 200 without token</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserServiceSecurityTest extends AbstractPostgresContainerTest {

    private static final String AUTH = "Authorization";
    private static final String TEST_SECRET =
            "Y2FyZWVyb3MtYWktbG9jYWwtZGV2LWFuZC10ZXN0LXNpZ25pbmctc2VjcmV0LWtleS0yNTZiaXQ=";

    @Autowired private MockMvc mockMvc;

    @Test
    void protectedEndpointReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointReturns401WithExpiredToken() throws Exception {
        String expired = buildExpiredToken(UUID.randomUUID());
        mockMvc.perform(get("/api/users/profile").header(AUTH, "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointReturns401WithWrongSignature() throws Exception {
        String tampered = buildTamperedToken(UUID.randomUUID());
        mockMvc.perform(get("/api/users/profile").header(AUTH, "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointReturns401WithWrongIssuer() throws Exception {
        String wrongIssuer = buildWrongIssuerToken(UUID.randomUUID());
        mockMvc.perform(get("/api/users/profile").header(AUTH, "Bearer " + wrongIssuer))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointReturns401WithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/users/profile").header(AUTH, "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenAllowsAccessToProtectedEndpoint() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "Bearer " + JwtTestTokens.accessToken(userId, userId + "@test.com");

        // Profile doesn't exist yet, so we expect 404 (business) not 401 (security)
        mockMvc.perform(get("/api/users/profile").header(AUTH, token))
                .andExpect(status().isNotFound());
    }

    @Test
    void actuatorHealthIsPublicWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void optionsPreflightIsPermittedWithoutToken() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .options("/api/users/profile"))
                .andExpect(status().isOk());
    }

    @Test
    void goalsEndpointReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/goals")).andExpect(status().isUnauthorized());
    }

    @Test
    void studySessionsEndpointReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/study-sessions")).andExpect(status().isUnauthorized());
    }

    @Test
    void roadmapsEndpointReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/roadmaps")).andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Token builders
    // -----------------------------------------------------------------------

    private String buildExpiredToken(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.builder()
                .issuer("careeros-ai")
                .subject(userId.toString())
                .claim("email", userId + "@test.com")
                .claim("authorities", List.of("ROLE_USER"))
                .issuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .expiration(Date.from(Instant.now().minusSeconds(1800)))
                .signWith(key)
                .compact();
    }

    private String buildTamperedToken(UUID userId) {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode("d3JvbmdrZXktZm9yLXRlc3RpbmctdGFtcGVyZWQtdG9rZW5zLTI1NmJpdA=="));
        return Jwts.builder()
                .issuer("careeros-ai")
                .subject(userId.toString())
                .claim("email", userId + "@test.com")
                .claim("authorities", List.of("ROLE_USER"))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(900)))
                .signWith(wrongKey)
                .compact();
    }

    private String buildWrongIssuerToken(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.builder()
                .issuer("attacker-service")   // Wrong issuer
                .subject(userId.toString())
                .claim("email", userId + "@test.com")
                .claim("authorities", List.of("ROLE_USER"))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(900)))
                .signWith(key)
                .compact();
    }
}
