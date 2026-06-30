package com.careeros.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Inbound payload to register a new user. Validation only — no auth logic in this phase.
 */
public record UserCreateDto(

        @NotBlank @Email @Size(max = 254)
        String email,

        @NotBlank @Size(min = 8, max = 72)
        String password,

        @Size(max = 100)
        String displayName,

        Set<String> roleNames
) {
}
