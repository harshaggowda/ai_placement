package com.careeros.user.service.impl;

import com.careeros.exception.ConflictException;
import com.careeros.exception.ResourceNotFoundException;
import com.careeros.user.dto.ProfileCreateDto;
import com.careeros.user.dto.ProfileResponseDto;
import com.careeros.user.dto.ProfileUpdateDto;
import com.careeros.user.entity.UserProfile;
import com.careeros.user.event.ProfileCreatedEvent;
import com.careeros.user.event.ProfileUpdatedEvent;
import com.careeros.user.mapper.UserProfileMapper;
import com.careeros.user.repository.UserProfileRepository;
import com.careeros.user.service.ProfileCompletionCalculator;
import com.careeros.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default {@link UserProfileService}. One profile per user; create/update emit domain events
 * (after commit). Completion percentage is computed on every read.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserProfileMapper profileMapper;
    private final ProfileCompletionCalculator completionCalculator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ProfileResponseDto createProfile(UUID userId, ProfileCreateDto request) {
        if (profileRepository.existsByUserId(userId)) {
            throw new ConflictException("UserProfile", "userId", userId);
        }
        UserProfile profile = profileMapper.toEntity(request);
        profile.setUserId(userId);
        UserProfile saved = profileRepository.save(profile);
        eventPublisher.publishEvent(new ProfileCreatedEvent(userId, saved.getId()));
        log.info("Profile created: userId={} profileId={}", userId, saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(UUID userId) {
        return toResponse(requireProfile(userId));
    }

    @Override
    public ProfileResponseDto updateProfile(UUID userId, ProfileUpdateDto request) {
        UserProfile profile = requireProfile(userId);
        profileMapper.updateEntity(request, profile);
        eventPublisher.publishEvent(new ProfileUpdatedEvent(userId, profile.getId()));
        log.info("Profile updated: userId={} profileId={}", userId, profile.getId());
        return toResponse(profile);
    }

    @Override
    public void deleteProfile(UUID userId) {
        profileRepository.delete(requireProfile(userId));
        log.info("Profile soft-deleted: userId={}", userId);
    }

    private UserProfile requireProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));
    }

    private ProfileResponseDto toResponse(UserProfile profile) {
        return profileMapper.toResponse(profile, completionCalculator.calculate(profile));
    }
}
