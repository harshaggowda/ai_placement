package com.careeros.career.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CertificateDto(
        @NotBlank(message = "Name is required")
        String name,
        
        @NotBlank(message = "Issuer is required")
        String issuer,
        
        LocalDate issueDate,
        LocalDate expiryDate,
        String url,
        String credentialId
) {
}
