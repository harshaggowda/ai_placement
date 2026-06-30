package com.careeros.career.dto;

import com.careeros.career.entity.InterviewFormat;
import com.careeros.career.entity.InterviewStatus;
import com.careeros.career.entity.InterviewType;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record InterviewDto(
        @NotNull(message = "Application ID is required")
        UUID applicationId,
        
        @NotNull(message = "Type is required")
        InterviewType type,
        
        @NotNull(message = "Format is required")
        InterviewFormat format,
        
        @NotNull(message = "Status is required")
        InterviewStatus status,
        
        Instant scheduledAt,
        String roundName,
        String meetingUrl,
        String location,
        String notes,
        String feedback
) {
}
