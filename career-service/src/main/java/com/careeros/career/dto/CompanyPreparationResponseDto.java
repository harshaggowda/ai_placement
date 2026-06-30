package com.careeros.career.dto;

import com.careeros.career.entity.PreparationStatus;
import com.careeros.career.entity.Priority;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompanyPreparationResponseDto(
        UUID id,
        UUID userId,
        CompanyResponseDto company,
        PreparationStatus preparationStatus,
        Priority priority,
        Integer completionPercentage,
        Object studyProgress,
        String notes,
        List<UUID> bookmarks,
        Instant createdAt,
        Instant updatedAt
) {
}
