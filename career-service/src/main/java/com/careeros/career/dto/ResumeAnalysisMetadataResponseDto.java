package com.careeros.career.dto;

import com.careeros.career.entity.AnalysisStatus;
import java.time.Instant;
import java.util.UUID;

public record ResumeAnalysisMetadataResponseDto(
        UUID id,
        UUID resumeId,
        String analysisId,
        Integer resumeVersion,
        AnalysisStatus analysisStatus,
        String aiProvider,
        Instant generatedTime,
        Long processingTimeMs,
        Instant createdAt,
        Instant updatedAt
) {
}
