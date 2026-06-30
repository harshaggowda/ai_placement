package com.careeros.user.dto;

import com.careeros.user.entity.RoadmapStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RoadmapResponseDto(
        UUID id,
        UUID userId,
        UUID goalId,
        String title,
        String description,
        RoadmapStatus status,
        LocalDate startDate,
        LocalDate targetDate,
        Instant completedAt,
        boolean isTemplate,
        int totalTasks,
        int completedTasks,
        int progressPercent,
        Instant createdAt,
        Instant updatedAt
) {
}
