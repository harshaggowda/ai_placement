package com.careeros.user.dto;

import com.careeros.user.entity.SkillCategory;

import java.time.Instant;
import java.util.UUID;

public record SkillResponseDto(
        UUID id,
        String name,
        String description,
        SkillCategory category,
        String iconUrl,
        boolean verified,
        Instant createdAt
) {
}
