package com.careeros.user.dto;

import com.careeros.user.entity.AchievementCategory;

import java.time.Instant;
import java.util.UUID;

public record UserAchievementResponseDto(
        UUID id,
        UUID achievementId,
        String code,
        String title,
        String description,
        AchievementCategory category,
        String iconUrl,
        Instant earnedAt
) {
}
