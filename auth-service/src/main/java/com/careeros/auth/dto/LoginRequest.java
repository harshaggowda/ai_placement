package com.careeros.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credentials for password-based login.
 */
public record LoginRequest(

        @NotBlank @Email
        String email,

        @NotBlank
        String password
) {
}
