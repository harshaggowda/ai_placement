package com.careeros.user.service.impl;

import com.careeros.user.dto.NotificationPreferenceDto;
import com.careeros.user.dto.NotificationPreferenceResponseDto;
import com.careeros.user.entity.NotificationPreference;
import com.careeros.user.mapper.NotificationPreferenceMapper;
import com.careeros.user.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationPreferenceServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceImplTest {

    @Mock NotificationPreferenceRepository repository;
    @Mock NotificationPreferenceMapper mapper;

    @InjectMocks NotificationPreferenceServiceImpl preferenceService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getReturnsExistingPreferenceWithoutCreating() {
        NotificationPreference pref = makePref(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(pref));
        when(mapper.toResponse(pref)).thenReturn(mockResponse());

        preferenceService.get(userId);

        verify(repository, never()).save(any());
        verify(mapper).toResponse(pref);
    }

    @Test
    void getCreatesDefaultPreferenceWhenNoneExist() {
        NotificationPreference defaultPref = makePref(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(repository.save(any(NotificationPreference.class))).thenReturn(defaultPref);
        when(mapper.toResponse(defaultPref)).thenReturn(mockResponse());

        preferenceService.get(userId);

        verify(repository).save(argThat(p -> userId.equals(p.getUserId())));
    }

    @Test
    void upsertUpdatesExistingPreference() {
        NotificationPreference pref = makePref(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(pref));
        when(mapper.toResponse(pref)).thenReturn(mockResponse());

        preferenceService.upsert(userId, new NotificationPreferenceDto(
                false, null, null, null, null, null, null, null, null));

        verify(mapper).updateEntity(any(NotificationPreferenceDto.class), eq(pref));
        verify(repository, never()).save(any()); // Dirty-checking auto-flushes
    }

    @Test
    void upsertCreatesAndUpdatesWhenNoneExist() {
        NotificationPreference defaultPref = makePref(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(repository.save(any(NotificationPreference.class))).thenReturn(defaultPref);
        when(mapper.toResponse(defaultPref)).thenReturn(mockResponse());

        preferenceService.upsert(userId, new NotificationPreferenceDto(
                null, null, null, null, null, null, null, null, null));

        verify(repository).save(any(NotificationPreference.class));
        verify(mapper).updateEntity(any(NotificationPreferenceDto.class), any());
    }

    private static NotificationPreference makePref(UUID userId) {
        NotificationPreference p = new NotificationPreference();
        p.setId(UUID.randomUUID());
        p.setUserId(userId);
        p.setEmailEnabled(true);
        return p;
    }

    private static NotificationPreferenceResponseDto mockResponse() {
        return new NotificationPreferenceResponseDto(UUID.randomUUID(), UUID.randomUUID(),
                true, true, true, true, true, true, false,
                LocalTime.of(22, 0), LocalTime.of(8, 0), Instant.now());
    }
}
