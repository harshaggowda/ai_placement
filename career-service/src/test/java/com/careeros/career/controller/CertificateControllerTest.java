package com.careeros.career.controller;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.JwtTestTokens;
import com.careeros.career.dto.CertificateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CertificateControllerTest extends AbstractPostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void canCreateAndRetrieveCertificate() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtTestTokens.accessToken(userId, "test@careeros.com");

        CertificateDto dto = new CertificateDto("Certified Kubernetes Administrator", "CNCF", LocalDate.of(2023, 6, 1), LocalDate.of(2026, 6, 1), null, "CKA-1234");

        // Create
        String responseContent = mockMvc.perform(post("/api/career/certificates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Certified Kubernetes Administrator"))
                .andExpect(jsonPath("$.data.issuer").value("CNCF"))
                .andReturn().getResponse().getContentAsString();

        String certId = objectMapper.readTree(responseContent).at("/data/id").asText();

        // Retrieve
        mockMvc.perform(get("/api/career/certificates/" + certId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Certified Kubernetes Administrator"));
    }
}
