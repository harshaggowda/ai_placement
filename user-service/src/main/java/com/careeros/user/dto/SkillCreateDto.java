package com.careeros.user.dto;

import com.careeros.user.entity.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkillCreateDto(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 512) String description,
        @NotNull SkillCategory category,
        @Size(max = 512) String iconUrl
) {
}
