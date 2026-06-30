package com.careeros.career.controller;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.JwtTestTokens;
import com.careeros.career.dto.DailyPlannerDto;
import com.careeros.career.dto.PlannerTask;
import com.careeros.career.entity.PlannerTaskStatus;
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
class DailyPlannerControllerTest extends AbstractPostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void canCreateAndRetrievePlanner() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = JwtTestTokens.accessToken(userId, "test@careeros.com");

        PlannerTask t1 = new PlannerTask();
        t1.setId("1");
        t1.setTitle("Do laundry");
        t1.setStatus(PlannerTaskStatus.PENDING);

        DailyPlannerDto dto = new DailyPlannerDto(LocalDate.now(), "Personal", List.of(t1), null, false);

        // Create
        String responseContent = mockMvc.perform(post("/api/career/planners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.focusArea").value("Personal"))
                .andExpect(jsonPath("$.data.tasks[0].title").value("Do laundry"))
                .andReturn().getResponse().getContentAsString();

        String plannerId = objectMapper.readTree(responseContent).at("/data/id").asText();

        // Retrieve
        mockMvc.perform(get("/api/career/planners/" + plannerId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.focusArea").value("Personal"));
    }
}
