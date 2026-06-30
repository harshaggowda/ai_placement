package com.careeros.career.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectResponseDto(
        UUID id,
        UUID userId,
        String name,
        String description,
        String role,
        String url,
        String repositoryUrl,
        List<String> tags,
        LocalDate startDate,
        LocalDate endDate,
        boolean ongoing,
        Instant createdAt,
        Instant updatedAt
) {
}
