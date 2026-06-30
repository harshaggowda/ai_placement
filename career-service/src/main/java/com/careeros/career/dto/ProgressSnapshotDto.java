package com.careeros.career.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ProgressSnapshotDto(
        @NotNull(message = "Snapshot date is required")
        LocalDate snapshotDate,
        
        int applicationsSent,
        int interviewsScheduled,
        int offersReceived,
        int rejections,
        int activeProjects,
        List<String> activeSkills
) {
}
