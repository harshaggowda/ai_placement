package com.careeros.career.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public record ProjectDto(
        @NotBlank(message = "Name is required")
        String name,
        
        String description,
        String role,
        String url,
        String repositoryUrl,
        List<String> tags,
        LocalDate startDate,
        LocalDate endDate,
        boolean ongoing
) {
}
