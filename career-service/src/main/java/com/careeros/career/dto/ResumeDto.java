package com.careeros.career.dto;

import com.careeros.career.entity.ResumeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ResumeDto(
        @NotBlank(message = "Title is required")
        String title,
        
        @NotBlank(message = "File URL is required")
        String fileUrl,
        
        @NotNull(message = "Status is required")
        ResumeStatus status,
        
        List<String> tags,
        Boolean isPublic
) {
}
