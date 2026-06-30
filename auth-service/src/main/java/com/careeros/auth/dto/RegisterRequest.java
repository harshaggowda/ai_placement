package com.careeros.auth.dto;

import com.careeros.auth.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration request. Roles are intentionally NOT accepted from the client — the default role is
 * assigned server-side.
 */
public record RegisterRequest(

        @NotBlank @Email @Size(max = 254)
        String email,

        @NotBlank @StrongPassword
        String password,

        @Size(max = 100)
        String displayName
) {
}
