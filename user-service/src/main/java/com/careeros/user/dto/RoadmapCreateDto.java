package com.careeros.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record RoadmapCreateDto(
        UUID goalId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        LocalDate startDate,
        LocalDate targetDate,
        Boolean isTemplate
) {
}
