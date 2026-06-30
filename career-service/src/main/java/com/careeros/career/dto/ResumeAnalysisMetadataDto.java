package com.careeros.career.dto;

import com.careeros.career.entity.AnalysisStatus;
import jakarta.validation.constraints.NotNull;

public record ResumeAnalysisMetadataDto(
        String analysisId,
        
        @NotNull(message = "Analysis status is required")
        AnalysisStatus analysisStatus,
        
        String aiProvider,
        Long processingTimeMs
) {
}
