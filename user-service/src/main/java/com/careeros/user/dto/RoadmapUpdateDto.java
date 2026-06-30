package com.careeros.user.dto;

import com.careeros.user.entity.RoadmapStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record RoadmapUpdateDto(
        UUID goalId,
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        RoadmapStatus status,
        LocalDate startDate,
        LocalDate targetDate
) {
}
