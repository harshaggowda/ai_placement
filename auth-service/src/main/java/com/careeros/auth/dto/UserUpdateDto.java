package com.careeros.auth.dto;

import com.careeros.auth.entity.UserStatus;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Inbound payload to update mutable user attributes. All fields optional (partial update).
 */
public record UserUpdateDto(

        @Size(max = 100)
        String displayName,

        UserStatus status,

        Set<String> roleNames
) {
}
