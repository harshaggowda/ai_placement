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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the remaining User Service domains — skills, roadmaps, study sessions,
 * achievements, settings. Runs against Testcontainers PostgreSQL with Flyway V2 schema.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserServiceDomainsIntegrationTest extends AbstractPostgresContainerTest {

    private static final String AUTH = "Authorization";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void skillCatalogAndUserSkillFlow() throws Exception {
        String adminToken = "Bearer " + JwtTestTokens.accessToken(UUID.randomUUID(), "admin@careeros.ai");
        String userToken = "Bearer " + JwtTestTokens.accessToken(UUID.randomUUID(), "user@careeros.ai");

        // Admin creates skill
        MvcResult created = mockMvc.perform(post("/api/users/skills").header(AUTH, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Kotlin","category":"LANGUAGE","description":"JVM language"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String skillId = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("data").get("id").asText();

        // User assigns skill
        mockMvc.perform(post("/api/users/my-skills").header(AUTH, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"skillId":"%s","proficiency":"INTERMEDIATE"}
                                """, skillId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.skillName").value("Kotlin"))
                .andExpect(jsonPath("$.data.proficiency").value("INTERMEDIATE"));

        // List user's skills
        mockMvc.perform(get("/api/users/my-skills").header(AUTH, userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void roadmapWithTasksFlow() throws Exception {
        String token = "Bearer " + JwtTestTokens.accessToken(UUID.randomUUID(), "learner@careeros.ai");

        // Create roadmap
        MvcResult roadmapCreated = mockMvc.perform(post("/api/users/roadmaps").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Master Spring Boot","description":"Learn Boot from scratch"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String roadmapId = objectMapper.readTree(roadmapCreated.getResponse().getContentAsString())
                .get("data").get("id").asText();

        // Add task
        mockMvc.perform(post("/api/users/roadmaps/" + roadmapId + "/tasks").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Read docs","orderIndex":0,"priority":"HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Read docs"));

        // List tasks
        mockMvc.perform(get("/api/users/roadmaps/" + roadmapId + "/tasks").header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void studySessionLifecycle() throws Exception {
        String token = "Bearer " + JwtTestTokens.accessToken(UUID.randomUUID(), "student@careeros.ai");

        // Start session
        MvcResult started = mockMvc.perform(post("/api/users/study-sessions/start").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notes":"DSA practice"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        String sessionId = objectMapper.readTree(started.getResponse().getContentAsString())
                .get("data").get("id").asText();

        // Finish session
        mockMvc.perform(post("/api/users/study-sessions/" + sessionId + "/finish").header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.durationMinutes").isNumber());

        // Stats
        mockMvc.perform(get("/api/users/study-sessions/stats").header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSessions").value(1));
    }

    @Test
    void dashboardAggregation() throws Exception {
        String token = "Bearer " + JwtTestTokens.accessToken(UUID.randomUUID(), "overview@careeros.ai");

        mockMvc.perform(get("/api/users/dashboard").header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileCompletion").isNumber())
                .andExpect(jsonPath("$.data.studyStats").exists())
                .andExpect(jsonPath("$.data.totalAchievements").isNumber());
    }

    @Test
    void settingsUpsert() throws Exception {
        String token = "Bearer " + JwtTestTokens.accessToken(UUID.randomUUID(), "settings@careeros.ai");

        mockMvc.perform(put("/api/users/dashboard-settings").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"theme":"DARK","language":"en","compactMode":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.theme").value("DARK"))
                .andExpect(jsonPath("$.data.compactMode").value(true));

        mockMvc.perform(get("/api/users/dashboard-settings").header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.theme").value("DARK"));
    }
}
