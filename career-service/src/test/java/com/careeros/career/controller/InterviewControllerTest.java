package com.careeros.career.controller;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.JwtTestTokens;
import com.careeros.career.dto.InterviewDto;
import com.careeros.career.entity.ApplicationStatus;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.InterviewFormat;
import com.careeros.career.entity.InterviewStatus;
import com.careeros.career.entity.InterviewType;
import com.careeros.career.entity.JobApplication;
import com.careeros.career.repository.CompanyRepository;
import com.careeros.career.repository.JobApplicationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InterviewControllerTest extends AbstractPostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Test
    void canCreateAndRetrieveInterview() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtTestTokens.accessToken(userId, "test@careeros.com");

        Company company = new Company();
        company.setName("Apple");
        company = companyRepository.save(company);

        JobApplication app = new JobApplication();
        app.setUserId(userId);
        app.setCompany(company);
        app.setRole("Hardware Engineer");
        app.setStatus(ApplicationStatus.INTERVIEWING);
        app = applicationRepository.save(app);

        InterviewDto dto = new InterviewDto(app.getId(), InterviewType.TECHNICAL, InterviewFormat.IN_PERSON, InterviewStatus.SCHEDULED, null, "Onsite", null, "Cupertino", null, null);

        // Create
        String responseContent = mockMvc.perform(post("/api/career/interviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("TECHNICAL"))
                .andExpect(jsonPath("$.data.format").value("IN_PERSON"))
                .andExpect(jsonPath("$.data.roundName").value("Onsite"))
                .andReturn().getResponse().getContentAsString();

        String interviewId = objectMapper.readTree(responseContent).at("/data/id").asText();

        // Retrieve
        mockMvc.perform(get("/api/career/interviews/" + interviewId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.location").value("Cupertino"));
    }
}
