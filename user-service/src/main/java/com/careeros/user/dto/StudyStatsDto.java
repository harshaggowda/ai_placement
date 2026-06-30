package com.careeros.user.dto;

/** Aggregated study statistics over a time window. */
public record StudyStatsDto(
        long todayMinutes,
        long weekMinutes,
        long monthMinutes,
        long totalMinutes,
        int currentStreak,
        long totalSessions
) {
}
