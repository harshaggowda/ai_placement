package com.careeros.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Inbound payload to update a permission's description.
 */
public record PermissionUpdateDto(

        @Size(max = 255)
        String description
) {
}
