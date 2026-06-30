package com.careeros.auth.dto;

import com.careeros.auth.entity.UserStatus;

/**
 * Optional filter criteria for searching users. All fields nullable (absent = no filter).
 */
public record UserSearchDto(
        String email,
        UserStatus status,
        Boolean emailVerified
) {
}
