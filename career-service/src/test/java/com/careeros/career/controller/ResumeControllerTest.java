package com.careeros.career.controller;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.JwtTestTokens;
import com.careeros.career.dto.ResumeDto;
import com.careeros.career.entity.ResumeStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumeControllerTest extends AbstractPostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void canCreateAndRetrieveResume() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtTestTokens.accessToken(userId, "test@careeros.com");

        ResumeDto createDto = new ResumeDto("My Java Resume", "http://s3.local/test.pdf", ResumeStatus.DRAFT, List.of("Java", "Spring"), false);

        // Create
        MvcResult result = mockMvc.perform(post("/api/career/resumes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("My Java Resume"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        String resumeId = objectMapper.readTree(responseContent).at("/data/id").asText();

        // Retrieve
        mockMvc.perform(get("/api/career/resumes/" + resumeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("My Java Resume"));
    }

    @Test
    void unauthenticatedFails() throws Exception {
        ResumeDto createDto = new ResumeDto("My Java Resume", "http://s3.local/test.pdf", ResumeStatus.DRAFT, List.of("Java"), false);

        mockMvc.perform(post("/api/career/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }
}
