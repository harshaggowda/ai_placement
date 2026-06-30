package com.careeros.career.dto;

import com.careeros.career.entity.HiringStatus;
import com.careeros.career.entity.InterviewDifficulty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompanyResponseDto(
        UUID id,
        String name,
        String industry,
        HiringStatus hiringStatus,
        InterviewDifficulty interviewDifficulty,
        List<String> techStack,
        String placementCategory,
        String eligibilityCriteria,
        List<String> hiringSeasons,
        String websiteUrl,
        String logoUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
