package com.careeros.career.dto;

import com.careeros.career.entity.HiringStatus;
import com.careeros.career.entity.InterviewDifficulty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CompanyDto(
        @NotBlank(message = "Company name is required")
        String name,
        String industry,
        HiringStatus hiringStatus,
        InterviewDifficulty interviewDifficulty,
        List<String> techStack,
        String placementCategory,
        String eligibilityCriteria,
        List<String> hiringSeasons,
        String websiteUrl,
        String logoUrl
) {
}
