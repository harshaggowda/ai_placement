package com.careeros.user.dto;

import com.careeros.user.entity.SkillCategory;
import jakarta.validation.constraints.Size;

public record SkillUpdateDto(
        @Size(max = 512) String description,
        SkillCategory category,
        @Size(max = 512) String iconUrl,
        Boolean verified
) {
}
