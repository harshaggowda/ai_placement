package com.careeros.user.dto;

import com.careeros.user.entity.GoalCategory;
import com.careeros.user.entity.GoalPriority;
import com.careeros.user.entity.GoalStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Partial update of a goal. {@code null} fields are left unchanged. */
public record GoalUpdateDto(
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        GoalCategory category,
        GoalStatus status,
        GoalPriority priority,
        @Min(0) @Max(100) Integer progressPercent,
        LocalDate targetDate
) {
}
