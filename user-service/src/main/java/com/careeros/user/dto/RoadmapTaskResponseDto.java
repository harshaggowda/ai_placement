package com.careeros.user.dto;

import com.careeros.user.entity.TaskPriority;
import com.careeros.user.entity.TaskStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RoadmapTaskResponseDto(
        UUID id,
        UUID roadmapId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        int orderIndex,
        BigDecimal estimatedHours,
        BigDecimal actualHours,
        LocalDate dueDate,
        Instant completedAt,
        UUID dependencyTaskId,
        Instant createdAt
) {
}
