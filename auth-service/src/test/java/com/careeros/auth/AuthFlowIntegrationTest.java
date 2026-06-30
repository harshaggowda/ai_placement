package com.careeros.auth;

import com.careeros.auth.entity.User;
import com.careeros.auth.entity.UserStatus;
import com.careeros.auth.repository.UserRepository;
import com.careeros.auth.service.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end auth flow against a real PostgreSQL (Flyway-migrated, schema validated): register →
 * verify email → login → access a protected endpoint → refresh. Also asserts that a protected
 * endpoint rejects unauthenticated access (401). Exercises the full security chain + JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest extends AbstractPostgresContainerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private TokenService tokenService;

    @Test
    void fullRegisterVerifyLoginRefreshFlow() throws Exception {
        String email = "flow@careeros.ai";
        String password = "password1";

        // 1. Register -> 201, account pending verification.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","displayName":"Flow User"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.status").value(UserStatus.PENDING_VERIFICATION.name()));

        User registered = userRepository.findByEmail(email).orElseThrow();
        assertThat(registered.isEmailVerified()).isFalse();

        // 2. Verify email using a freshly issued token -> account becomes ACTIVE.
        String verificationToken = tokenService.issueEmailVerificationToken(registered);
        mockMvc.perform(get("/api/auth/verify-email").param("token", verificationToken))
                .andExpect(status().isOk());

        assertThat(userRepository.findByEmail(email).orElseThrow().getStatus())
                .isEqualTo(UserStatus.ACTIVE);

        // 3. Login -> tokens.
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode loginData = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("data");
        String accessToken = loginData.get("accessToken").asText();
        String refreshToken = loginData.get("refreshToken").asText();

        // 4. Protected endpoint with token -> 200.
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        // 5. Protected endpoint without token -> 401.
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        // 6. Refresh -> new tokens.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void registrationRejectsWeakPasswordWith400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"weak@careeros.ai","password":"short","displayName":"Weak"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
