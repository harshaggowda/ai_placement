package com.careeros.career.dto;

import com.careeros.career.entity.ResumeStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResumeResponseDto(
        UUID id,
        UUID userId,
        String title,
        String fileUrl,
        ResumeStatus status,
        Integer versionNumber,
        List<String> tags,
        Boolean isPublic,
        Instant createdAt,
        Instant updatedAt
) {
}
