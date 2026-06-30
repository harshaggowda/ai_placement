package com.careeros.user.dto;

import com.careeros.user.entity.TaskPriority;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RoadmapTaskCreateDto(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        TaskPriority priority,
        @Min(0) int orderIndex,
        @DecimalMin("0.0") BigDecimal estimatedHours,
        LocalDate dueDate,
        UUID dependencyTaskId
) {
}
