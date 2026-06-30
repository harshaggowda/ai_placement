package com.careeros.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full security + flow integration against real PostgreSQL (Flyway-migrated, schema validated).
 * Authenticates with real JWTs through the shared filter; verifies profile CRUD, 401 without a
 * token, and goal ownership isolation across users.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserServiceIntegrationTest extends AbstractPostgresContainerTest {

    private static final String AUTH = "Authorization";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void profileCreateGetAndUnauthorized() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "Bearer " + JwtTestTokens.accessToken(userId, "owner@careeros.ai");

        mockMvc.perform(post("/api/users/profile").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"headline":"Aspiring SWE","preferredLanguages":["Java"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.completionPercentage").isNumber());

        mockMvc.perform(get("/api/users/profile").header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.headline").value("Aspiring SWE"));

        // No token -> 401 from the security entry point.
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void goalOwnershipIsEnforcedAcrossUsers() throws Exception {
        String ownerToken = "Bearer " + JwtTestTokens.accessToken(UUID.randomUUID(), "a@careeros.ai");
        String otherToken = "Bearer " + JwtTestTokens.accessToken(UUID.randomUUID(), "b@careeros.ai");

        MvcResult created = mockMvc.perform(post("/api/users/goals").header(AUTH, ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Crack interviews","category":"INTERVIEW"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String goalId = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("data").get("id").asText();

        // Owner can read it.
        mockMvc.perform(get("/api/users/goals/" + goalId).header(AUTH, ownerToken))
                .andExpect(status().isOk());

        // A different user cannot — indistinguishable from non-existent (404).
        mockMvc.perform(get("/api/users/goals/" + goalId).header(AUTH, otherToken))
                .andExpect(status().isNotFound());
    }
}
