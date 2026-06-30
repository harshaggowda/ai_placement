package com.careeros.user.dto;

import com.careeros.user.entity.SkillProficiency;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserSkillUpdateDto(
        SkillProficiency proficiency,
        @DecimalMin("0.0") BigDecimal yearsOfExperience,
        LocalDate lastPracticedDate,
        Boolean isVerified
) {
}
