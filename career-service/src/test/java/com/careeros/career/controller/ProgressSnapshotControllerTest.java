package com.careeros.career.controller;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.JwtTestTokens;
import com.careeros.career.dto.ProgressSnapshotDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgressSnapshotControllerTest extends AbstractPostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void canCreateAndRetrieveSnapshot() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtTestTokens.accessToken(userId, "test@careeros.com");

        ProgressSnapshotDto dto = new ProgressSnapshotDto(LocalDate.now(), 12, 3, 0, 2, 1, List.of("AWS"));

        // Create
        String responseContent = mockMvc.perform(post("/api/career/snapshots")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.applicationsSent").value(12))
                .andExpect(jsonPath("$.data.activeSkills[0]").value("AWS"))
                .andReturn().getResponse().getContentAsString();

        String snapshotId = objectMapper.readTree(responseContent).at("/data/id").asText();

        // Retrieve
        mockMvc.perform(get("/api/career/snapshots/" + snapshotId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationsSent").value(12));
    }
}
