package com.careeros.user.dto;

import com.careeros.user.entity.SkillProficiency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UserSkillAssignDto(
        @NotNull UUID skillId,
        SkillProficiency proficiency,
        @DecimalMin("0.0") BigDecimal yearsOfExperience,
        LocalDate lastPracticedDate
) {
}
