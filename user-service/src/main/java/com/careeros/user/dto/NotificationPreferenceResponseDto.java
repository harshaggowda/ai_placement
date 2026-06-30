package com.careeros.user.dto;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record NotificationPreferenceResponseDto(
        UUID id,
        UUID userId,
        boolean emailEnabled,
        boolean pushEnabled,
        boolean weeklyDigest,
        boolean goalReminders,
        boolean roadmapUpdates,
        boolean achievementAlerts,
        boolean marketingEmails,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd,
        Instant updatedAt
) {
}
