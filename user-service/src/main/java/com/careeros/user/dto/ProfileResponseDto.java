package com.careeros.user.dto;

import com.careeros.user.entity.ExperienceLevel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full profile representation, including the derived {@code completionPercentage}.
 */
public record ProfileResponseDto(
        UUID id,
        UUID userId,
        String headline,
        String bio,
        String location,
        String avatarUrl,
        ExperienceLevel experienceLevel,
        String targetCompany,
        String targetRole,
        Integer graduationYear,
        Integer currentSemester,
        String githubUrl,
        String linkedinUrl,
        String websiteUrl,
        String twitterUrl,
        List<String> preferredLanguages,
        List<String> preferredTechStack,
        List<String> education,
        List<String> experience,
        int completionPercentage,
        Instant createdAt,
        Instant updatedAt
) {
}
