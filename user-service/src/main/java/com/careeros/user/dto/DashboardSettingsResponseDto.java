package com.careeros.user.dto;

import com.careeros.user.entity.Theme;

import java.time.Instant;
import java.util.UUID;

public record DashboardSettingsResponseDto(
        UUID id,
        UUID userId,
        Theme theme,
        String language,
        String timezone,
        boolean compactMode,
        String widgetsConfig,
        Instant updatedAt
) {
}
