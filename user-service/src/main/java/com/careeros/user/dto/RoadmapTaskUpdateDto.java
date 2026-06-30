package com.careeros.user.dto;

import com.careeros.user.entity.TaskPriority;
import com.careeros.user.entity.TaskStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RoadmapTaskUpdateDto(
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        TaskStatus status,
        TaskPriority priority,
        Integer orderIndex,
        @DecimalMin("0.0") BigDecimal estimatedHours,
        @DecimalMin("0.0") BigDecimal actualHours,
        LocalDate dueDate,
        UUID dependencyTaskId
) {
}
