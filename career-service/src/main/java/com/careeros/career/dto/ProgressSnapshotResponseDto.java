package com.careeros.career.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProgressSnapshotResponseDto(
        UUID id,
        UUID userId,
        LocalDate snapshotDate,
        int applicationsSent,
        int interviewsScheduled,
        int offersReceived,
        int rejections,
        int activeProjects,
        List<String> activeSkills,
        Instant createdAt,
        Instant updatedAt
) {
}
