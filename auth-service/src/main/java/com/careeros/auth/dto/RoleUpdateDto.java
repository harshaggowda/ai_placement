package com.careeros.auth.dto;

import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Inbound payload to update a role's description and/or permission set. Fields optional.
 */
public record RoleUpdateDto(

        @Size(max = 255)
        String description,

        Set<String> permissionNames
) {
}
