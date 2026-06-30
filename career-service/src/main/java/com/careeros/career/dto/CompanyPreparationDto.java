package com.careeros.career.dto;

import com.careeros.career.entity.PreparationStatus;
import com.careeros.career.entity.Priority;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CompanyPreparationDto(
        @NotNull(message = "Company ID is required")
        UUID companyId,
        
        @NotNull(message = "Preparation status is required")
        PreparationStatus preparationStatus,
        
        Priority priority,
        Integer completionPercentage,
        Object studyProgress,
        String notes,
        List<UUID> bookmarks
) {
}
