package com.careeros.user.service;

import com.careeros.user.dto.ProfileCreateDto;
import com.careeros.user.dto.ProfileResponseDto;
import com.careeros.user.dto.ProfileUpdateDto;

import java.util.UUID;

/**
 * Profile use cases, always scoped to the acting user (ownership by construction).
 */
public interface UserProfileService {

    ProfileResponseDto createProfile(UUID userId, ProfileCreateDto request);

    ProfileResponseDto getProfile(UUID userId);

    ProfileResponseDto updateProfile(UUID userId, ProfileUpdateDto request);

    void deleteProfile(UUID userId);
}
