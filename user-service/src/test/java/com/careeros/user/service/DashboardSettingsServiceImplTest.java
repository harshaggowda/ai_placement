package com.careeros.user.service.impl;

import com.careeros.user.dto.DashboardSettingsDto;
import com.careeros.user.dto.DashboardSettingsResponseDto;
import com.careeros.user.entity.DashboardSettings;
import com.careeros.user.entity.Theme;
import com.careeros.user.mapper.DashboardSettingsMapper;
import com.careeros.user.repository.DashboardSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DashboardSettingsServiceImpl}.
 *
 * <p>Key behavior: {@code get()} and {@code upsert()} both use "get-or-create" semantics.
 */
@ExtendWith(MockitoExtension.class)
class DashboardSettingsServiceImplTest {

    @Mock DashboardSettingsRepository repository;
    @Mock DashboardSettingsMapper mapper;

    @InjectMocks DashboardSettingsServiceImpl dashboardService;

    private final UUID userId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // get()
    // -----------------------------------------------------------------------

    @Test
    void getReturnsExistingSettingsWithoutCreating() {
        DashboardSettings settings = makeSettings(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(mapper.toResponse(settings)).thenReturn(mockResponse());

        dashboardService.get(userId);

        verify(repository, never()).save(any());
        verify(mapper).toResponse(settings);
    }

    @Test
    void getCreatesDefaultSettingsWhenNoneExist() {
        DashboardSettings defaultSettings = makeSettings(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(repository.save(any(DashboardSettings.class))).thenReturn(defaultSettings);
        when(mapper.toResponse(defaultSettings)).thenReturn(mockResponse());

        dashboardService.get(userId);

        verify(repository).save(argThat(s -> userId.equals(s.getUserId())));
    }

    // -----------------------------------------------------------------------
    // upsert()
    // -----------------------------------------------------------------------

    @Test
    void upsertUpdatesExistingSettings() {
        DashboardSettings settings = makeSettings(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(mapper.toResponse(settings)).thenReturn(mockResponse());

        dashboardService.upsert(userId, new DashboardSettingsDto(Theme.DARK, "en", "UTC", false, null));

        verify(mapper).updateEntity(any(DashboardSettingsDto.class), eq(settings));
        verify(repository, never()).save(any()); // No extra save needed — dirty-checking auto-flushes
    }

    @Test
    void upsertCreatesAndUpdatesWhenNoneExist() {
        DashboardSettings defaultSettings = makeSettings(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(repository.save(any(DashboardSettings.class))).thenReturn(defaultSettings);
        when(mapper.toResponse(defaultSettings)).thenReturn(mockResponse());

        dashboardService.upsert(userId, new DashboardSettingsDto(Theme.LIGHT, null, null, null, null));

        // save() is called for the create step
        verify(repository).save(any(DashboardSettings.class));
        verify(mapper).updateEntity(any(DashboardSettingsDto.class), any());
    }

    @Test
    void upsertWithNullThemePreservesExistingTheme() {
        DashboardSettings settings = makeSettings(userId);
        settings.setTheme(Theme.DARK);
        when(repository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(mapper.toResponse(settings)).thenReturn(mockResponse());

        // Pass null theme — mapper IGNORE strategy leaves existing value intact
        dashboardService.upsert(userId, new DashboardSettingsDto(null, null, null, null, null));

        verify(mapper).updateEntity(any(), same(settings));
        // The mapper is responsible for ignoring nulls; asserting settings.getTheme() == DARK
        // would test mapper behavior, not service behavior. We verify the mapper is called correctly.
        assertThat(settings.getTheme()).isEqualTo(Theme.DARK);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static DashboardSettings makeSettings(UUID userId) {
        DashboardSettings s = new DashboardSettings();
        s.setId(UUID.randomUUID());
        s.setUserId(userId);
        s.setTheme(Theme.SYSTEM);
        return s;
    }

    private static DashboardSettingsResponseDto mockResponse() {
        return new DashboardSettingsResponseDto(UUID.randomUUID(), UUID.randomUUID(),
                Theme.SYSTEM, "en", "UTC", false, null, Instant.now());
    }
}
