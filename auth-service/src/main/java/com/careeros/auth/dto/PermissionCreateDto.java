package com.careeros.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload to create a permission (e.g. {@code resume:write}).
 */
public record PermissionCreateDto(

        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 255)
        String description
) {
}
