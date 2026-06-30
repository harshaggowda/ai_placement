package com.careeros.auth.dto;

import java.time.Instant;

/**
 * Optional filter criteria for searching login history. All fields nullable.
 */
public record LoginHistorySearchDto(
        String email,
        Boolean successful,
        Instant from,
        Instant to
) {
}
