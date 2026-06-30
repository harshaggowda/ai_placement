package com.careeros.user.dto;

import com.careeros.user.entity.SkillCategory;
import com.careeros.user.entity.SkillProficiency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserSkillResponseDto(
        UUID id,
        UUID userId,
        UUID skillId,
        String skillName,
        String skillDescription,
        SkillCategory skillCategory,
        SkillProficiency proficiency,
        BigDecimal yearsOfExperience,
        LocalDate lastPracticedDate,
        boolean isVerified,
        Instant createdAt
) {
}
