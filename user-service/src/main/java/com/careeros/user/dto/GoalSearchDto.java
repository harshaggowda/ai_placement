package com.careeros.user.dto;

import com.careeros.user.entity.GoalCategory;
import com.careeros.user.entity.GoalPriority;
import com.careeros.user.entity.GoalStatus;

/** Optional filter criteria for searching the current user's goals. All fields nullable. */
public record GoalSearchDto(
        GoalStatus status,
        GoalCategory category,
        GoalPriority priority
) {
}
