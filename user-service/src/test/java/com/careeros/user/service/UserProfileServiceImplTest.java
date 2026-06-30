package com.careeros.user.service.impl;

import com.careeros.exception.ConflictException;
import com.careeros.exception.ResourceNotFoundException;
import com.careeros.user.dto.ProfileCreateDto;
import com.careeros.user.dto.ProfileResponseDto;
import com.careeros.user.entity.UserProfile;
import com.careeros.user.event.ProfileCreatedEvent;
import com.careeros.user.event.ProfileUpdatedEvent;
import com.careeros.user.mapper.UserProfileMapper;
import com.careeros.user.repository.UserProfileRepository;
import com.careeros.user.service.ProfileCompletionCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserProfileServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock UserProfileRepository profileRepository;
    @Mock UserProfileMapper profileMapper;
    @Mock ProfileCompletionCalculator completionCalculator;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks UserProfileServiceImpl profileService;

    private final UUID userId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // createProfile()
    // -----------------------------------------------------------------------

    @Test
    void createProfileSuccessfully() {
        when(profileRepository.existsByUserId(userId)).thenReturn(false);
        UserProfile saved = new UserProfile();
        saved.setUserId(userId);
        saved.setId(UUID.randomUUID());
        when(profileMapper.toEntity(any(ProfileCreateDto.class))).thenReturn(saved);
        when(profileRepository.save(any())).thenReturn(saved);
        when(completionCalculator.calculate(saved)).thenReturn(0);
        when(profileMapper.toResponse(saved, 0)).thenReturn(mockResponse());

        profileService.createProfile(userId, new ProfileCreateDto(
                "Hello", null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null));

        verify(profileRepository).save(any(UserProfile.class));
        verify(eventPublisher).publishEvent(any(ProfileCreatedEvent.class));
    }

    @Test
    void createProfileThrowsConflictWhenAlreadyExists() {
        when(profileRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> profileService.createProfile(userId,
                new ProfileCreateDto(null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ConflictException.class);

        verify(profileRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // -----------------------------------------------------------------------
    // getProfile()
    // -----------------------------------------------------------------------

    @Test
    void getProfileSuccessfully() {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(completionCalculator.calculate(profile)).thenReturn(50);
        when(profileMapper.toResponse(profile, 50)).thenReturn(mockResponse());

        profileService.getProfile(userId);

        verify(profileMapper).toResponse(profile, 50);
    }

    @Test
    void getProfileThrowsNotFoundWhenMissing() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // updateProfile()
    // -----------------------------------------------------------------------

    @Test
    void updateProfilePublishesEvent() {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setId(UUID.randomUUID());
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(completionCalculator.calculate(profile)).thenReturn(25);
        when(profileMapper.toResponse(profile, 25)).thenReturn(mockResponse());

        profileService.updateProfile(userId, new com.careeros.user.dto.ProfileUpdateDto(
                "Updated headline", null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null));

        verify(eventPublisher).publishEvent(any(ProfileUpdatedEvent.class));
    }

    @Test
    void updateProfileThrowsNotFoundWhenMissing() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile(userId,
                new com.careeros.user.dto.ProfileUpdateDto(null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // deleteProfile()
    // -----------------------------------------------------------------------

    @Test
    void deleteProfileCallsSoftDelete() {
        UserProfile profile = new UserProfile();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        profileService.deleteProfile(userId);

        verify(profileRepository).delete(profile);
    }

    @Test
    void deleteProfileThrowsNotFoundWhenMissing() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.deleteProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static ProfileResponseDto mockResponse() {
        return new ProfileResponseDto(UUID.randomUUID(), UUID.randomUUID(), null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, 0, null, null);
    }
}
