package com.careeros.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to initiate a password reset for the given email.
 */
public record ForgotPasswordRequest(

        @NotBlank @Email
        String email
) {
}
