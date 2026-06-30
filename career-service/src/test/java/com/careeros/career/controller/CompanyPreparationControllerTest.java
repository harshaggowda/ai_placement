package com.careeros.career.controller;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.JwtTestTokens;
import com.careeros.career.dto.CompanyPreparationDto;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.PreparationStatus;
import com.careeros.career.repository.CompanyRepository;
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
class CompanyPreparationControllerTest extends AbstractPostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void canUpsertAndRetrievePreparation() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtTestTokens.accessToken(userId, "test@careeros.com");

        Company company = new Company();
        company.setName("TestCompany");
        company = companyRepository.save(company);

        CompanyPreparationDto dto = new CompanyPreparationDto(company.getId(), PreparationStatus.IN_PROGRESS, null, 25, null, "Working on it", null);

        // Upsert
        mockMvc.perform(post("/api/career/preparations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preparationStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.completionPercentage").value(25))
                .andExpect(jsonPath("$.data.company.name").value("TestCompany"));

        // Retrieve
        mockMvc.perform(get("/api/career/preparations/" + company.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preparationStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.completionPercentage").value(25));
    }
}
