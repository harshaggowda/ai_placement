package com.careeros.user.dto;

import java.time.LocalTime;

public record NotificationPreferenceDto(
        Boolean emailEnabled,
        Boolean pushEnabled,
        Boolean weeklyDigest,
        Boolean goalReminders,
        Boolean roadmapUpdates,
        Boolean achievementAlerts,
        Boolean marketingEmails,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd
) {
}
