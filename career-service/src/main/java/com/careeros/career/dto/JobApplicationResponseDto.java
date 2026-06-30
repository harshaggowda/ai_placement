package com.careeros.career.dto;

import com.careeros.career.entity.ApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record JobApplicationResponseDto(
        UUID id,
        UUID userId,
        CompanyResponseDto company,
        UUID resumeId,
        String role,
        String url,
        String referer,
        String location,
        String offerDetails,
        ApplicationStatus status,
        Instant appliedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
