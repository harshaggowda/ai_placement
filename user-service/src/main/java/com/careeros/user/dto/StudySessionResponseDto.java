package com.careeros.user.dto;

import com.careeros.user.entity.StudySessionStatus;

import java.time.Instant;
import java.util.UUID;

public record StudySessionResponseDto(
        UUID id,
        UUID userId,
        UUID roadmapTaskId,
        StudySessionStatus status,
        Instant startedAt,
        Instant lastPausedAt,
        Instant endedAt,
        long totalPausedSeconds,
        Integer durationMinutes,
        String notes,
        Instant createdAt
) {
}
