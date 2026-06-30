package com.careeros.career.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record DailyPlannerDto(
        @NotNull(message = "Target date is required")
        LocalDate targetDate,
        
        String focusArea,
        List<PlannerTask> tasks,
        String notes,
        boolean completed
) {
}
