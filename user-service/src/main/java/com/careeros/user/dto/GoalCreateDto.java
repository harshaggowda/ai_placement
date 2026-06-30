package com.careeros.user.dto;

import com.careeros.user.entity.GoalCategory;
import com.careeros.user.entity.GoalPriority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Payload to create a goal for the current user. */
public record GoalCreateDto(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotNull GoalCategory category,
        GoalPriority priority,
        @Min(0) @Max(100) Integer progressPercent,
        LocalDate targetDate
) {
}
