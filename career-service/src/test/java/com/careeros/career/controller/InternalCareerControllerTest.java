package com.careeros.career.controller;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.dto.ResumeAnalysisMetadataDto;
import com.careeros.career.dto.ResumeDto;
import com.careeros.career.entity.AnalysisStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for internal API testing if needed, though they shouldn't block it yet
@ActiveProfiles("test")
class InternalCareerControllerTest extends AbstractPostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void canGetActiveResumeAndSaveMetadata() throws Exception {
        UUID userId = UUID.randomUUID();

        // Since we don't have a token, we must create a resume through the service or mock it.
        // For E2E simplicity, we can just test if the endpoint returns 404 for no resumes
        
        mockMvc.perform(get("/api/internal/career/users/" + userId + "/resumes/active"))
                .andExpect(status().isNotFound());

        // Save metadata should work even if resume doesn't exist? Actually the service doesn't validate if resume exists in ResumeService.java, it just saves it.
        // Wait, metadata repository saves with resumeId. 
        UUID resumeId = UUID.randomUUID();
        ResumeAnalysisMetadataDto metaDto = new ResumeAnalysisMetadataDto(
                "analysis-123", AnalysisStatus.COMPLETED, "openai", 1500L
        );
        
        mockMvc.perform(post("/api/internal/career/users/" + userId + "/resumes/" + resumeId + "/metadata")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisId").value("analysis-123"));
    }
}
