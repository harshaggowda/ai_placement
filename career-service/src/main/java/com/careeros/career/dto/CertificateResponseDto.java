package com.careeros.career.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CertificateResponseDto(
        UUID id,
        UUID userId,
        String name,
        String issuer,
        LocalDate issueDate,
        LocalDate expiryDate,
        String url,
        String credentialId,
        Instant createdAt,
        Instant updatedAt
) {
}
