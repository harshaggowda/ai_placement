package com.careeros.career.dto;

import com.careeros.career.entity.InterviewFormat;
import com.careeros.career.entity.InterviewStatus;
import com.careeros.career.entity.InterviewType;
import java.time.Instant;
import java.util.UUID;

public record InterviewResponseDto(
        UUID id,
        UUID userId,
        UUID applicationId,
        InterviewType type,
        InterviewFormat format,
        InterviewStatus status,
        Instant scheduledAt,
        String roundName,
        String meetingUrl,
        String location,
        String notes,
        String feedback,
        Instant createdAt,
        Instant updatedAt
) {
}
