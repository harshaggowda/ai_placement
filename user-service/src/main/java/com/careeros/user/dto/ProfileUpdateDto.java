package com.careeros.user.dto;

import com.careeros.user.entity.ExperienceLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Partial update of the current user's profile. {@code null} fields are left unchanged.
 */
public record ProfileUpdateDto(
        @Size(max = 200) String headline,
        @Size(max = 2000) String bio,
        @Size(max = 120) String location,
        @Size(max = 512) String avatarUrl,
        ExperienceLevel experienceLevel,
        @Size(max = 160) String targetCompany,
        @Size(max = 120) String targetRole,
        @Min(1950) @Max(2100) Integer graduationYear,
        @Min(1) @Max(12) Integer currentSemester,
        @Size(max = 512) String githubUrl,
        @Size(max = 512) String linkedinUrl,
        @Size(max = 512) String websiteUrl,
        @Size(max = 512) String twitterUrl,
        List<String> preferredLanguages,
        List<String> preferredTechStack,
        List<String> education,
        List<String> experience
) {
}
