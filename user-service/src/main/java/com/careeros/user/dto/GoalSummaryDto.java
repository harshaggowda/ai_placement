package com.careeros.user.dto;

import com.careeros.user.entity.GoalCategory;
import com.careeros.user.entity.GoalPriority;
import com.careeros.user.entity.GoalStatus;

import java.util.UUID;

/** Lightweight goal projection for lists. */
public record GoalSummaryDto(
        UUID id,
        String title,
        GoalCategory category,
        GoalStatus status,
        GoalPriority priority,
        int progressPercent
) {
}
