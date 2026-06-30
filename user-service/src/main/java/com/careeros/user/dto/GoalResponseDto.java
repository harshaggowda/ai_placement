package com.careeros.user.dto;

import com.careeros.user.entity.GoalCategory;
import com.careeros.user.entity.GoalPriority;
import com.careeros.user.entity.GoalStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Full goal representation. */
public record GoalResponseDto(
        UUID id,
        UUID userId,
        String title,
        String description,
        GoalCategory category,
        GoalStatus status,
        GoalPriority priority,
        int progressPercent,
        LocalDate targetDate,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
