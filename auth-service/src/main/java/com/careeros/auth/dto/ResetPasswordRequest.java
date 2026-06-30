package com.careeros.auth.dto;

import com.careeros.auth.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to complete a password reset using a previously issued reset token.
 */
public record ResetPasswordRequest(

        @NotBlank
        String token,

        @NotBlank @StrongPassword
        String newPassword
) {
}
