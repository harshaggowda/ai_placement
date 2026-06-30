package com.careeros.user.controller;

import com.careeros.user.AbstractPostgresContainerTest;
import com.careeros.user.JwtTestTokens;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack controller tests for the Goal API.
 *
 * <p>Runs against a real Spring context with Testcontainers PostgreSQL and the actual
 * JWT filter. Tests cover the full request path including security, validation, ownership, and
 * the complete CRUD + progress lifecycle.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoalControllerTest extends AbstractPostgresContainerTest {

    private static final String AUTH = "Authorization";
    private static final String BASE = "/api/users/goals";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // Security: 401 without token
    // -----------------------------------------------------------------------

    @Test
    void getAllGoalsReturns401WithoutToken() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    void createGoalReturns401WithoutToken() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\",\"category\":\"CAREER\"}"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    @Test
    void createGoalReturns201WithValidPayload() throws Exception {
        String token = token();
        mockMvc.perform(post(BASE).header(AUTH, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Land a FAANG role\",\"category\":\"CAREER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Land a FAANG role"))
                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"));
    }

    @Test
    void createGoalReturns400WhenTitleMissing() throws Exception {
        String token = token();
        mockMvc.perform(post(BASE).header(AUTH, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"CAREER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGoalReturns400WhenCategoryMissing() throws Exception {
        String token = token();
        mockMvc.perform(post(BASE).header(AUTH, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Some goal\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGoalReturns400WhenProgressOutOfRange() throws Exception {
        String token = token();
        mockMvc.perform(post(BASE).header(AUTH, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Bad\",\"category\":\"CAREER\",\"progressPercent\":150}"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Get
    // -----------------------------------------------------------------------

    @Test
    void getGoalReturns200ForOwner() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "Bearer " + JwtTestTokens.accessToken(userId, userId + "@test.com");
        String goalId = createGoal(token, "Read Clean Code", "SKILL");

        mockMvc.perform(get(BASE + "/" + goalId).header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(goalId));
    }

    @Test
    void getGoalReturns404ForDifferentUser() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID intruder = UUID.randomUUID();
        String ownerToken = "Bearer " + JwtTestTokens.accessToken(owner, owner + "@test.com");
        String intruderToken = "Bearer " + JwtTestTokens.accessToken(intruder, intruder + "@test.com");

        String goalId = createGoal(ownerToken, "Private goal", "CAREER");

        mockMvc.perform(get(BASE + "/" + goalId).header(AUTH, intruderToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGoalReturns404ForNonExistentGoal() throws Exception {
        String token = token();
        mockMvc.perform(get(BASE + "/" + UUID.randomUUID()).header(AUTH, token))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // UpdateProgress
    // -----------------------------------------------------------------------

    @Test
    void updateProgressTo100CompletesGoal() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "Bearer " + JwtTestTokens.accessToken(userId, userId + "@test.com");
        String goalId = createGoal(token, "Finish LeetCode", "INTERVIEW");

        mockMvc.perform(patch(BASE + "/" + goalId + "/progress")
                .header(AUTH, token).param("percent", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.progressPercent").value(100));
    }

    @Test
    void updateProgressReturns400ForNegativeValue() throws Exception {
        String token = token();
        String goalId = createGoal(token, "Any goal", "CAREER");

        mockMvc.perform(patch(BASE + "/" + goalId + "/progress")
                .header(AUTH, token).param("percent", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProgressReturns400For101() throws Exception {
        String token = token();
        String goalId = createGoal(token, "Any goal", "CAREER");

        mockMvc.perform(patch(BASE + "/" + goalId + "/progress")
                .header(AUTH, token).param("percent", "101"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    @Test
    void deleteGoalReturns200ForOwner() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = "Bearer " + JwtTestTokens.accessToken(userId, userId + "@test.com");
        String goalId = createGoal(token, "Goal to delete", "OTHER");

        mockMvc.perform(delete(BASE + "/" + goalId).header(AUTH, token))
                .andExpect(status().isOk());

        // Deleted goal should not be visible
        mockMvc.perform(get(BASE + "/" + goalId).header(AUTH, token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGoalReturns404ForAnotherUser() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID intruder = UUID.randomUUID();
        String ownerToken = "Bearer " + JwtTestTokens.accessToken(owner, owner + "@test.com");
        String intruderToken = "Bearer " + JwtTestTokens.accessToken(intruder, intruder + "@test.com");

        String goalId = createGoal(ownerToken, "Sensitive goal", "CAREER");

        mockMvc.perform(delete(BASE + "/" + goalId).header(AUTH, intruderToken))
                .andExpect(status().isNotFound());

        // Verify goal still exists for the owner
        mockMvc.perform(get(BASE + "/" + goalId).header(AUTH, ownerToken))
                .andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String token() {
        UUID userId = UUID.randomUUID();
        return "Bearer " + JwtTestTokens.accessToken(userId, userId + "@test.com");
    }

    private String createGoal(String token, String title, String category) throws Exception {
        MvcResult result = mockMvc.perform(post(BASE).header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"%s","category":"%s"}
                                """.formatted(title, category)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asText();
    }
}
