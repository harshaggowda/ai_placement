package com.careeros.career.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DailyPlannerResponseDto(
        UUID id,
        UUID userId,
        LocalDate targetDate,
        String focusArea,
        List<PlannerTask> tasks,
        String notes,
        boolean completed,
        Instant createdAt,
        Instant updatedAt
) {
}
