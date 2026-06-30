package com.careeros.career.dto;

import com.careeros.career.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record JobApplicationDto(
        @NotNull(message = "Company ID is required")
        UUID companyId,
        
        UUID resumeId,
        
        String role,
        String url,
        String referer,
        String location,
        String offerDetails,
        
        @NotNull(message = "Status is required")
        ApplicationStatus status
) {
}
