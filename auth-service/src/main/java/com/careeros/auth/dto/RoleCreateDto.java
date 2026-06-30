package com.careeros.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Inbound payload to create a role and (optionally) its permission set.
 */
public record RoleCreateDto(

        @NotBlank @Size(max = 50)
        String name,

        @Size(max = 255)
        String description,

        Set<String> permissionNames
) {
}
