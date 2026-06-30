package com.careeros.user.service;

import com.careeros.user.entity.StudySessionStatus;
import com.careeros.user.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Calculates the current study-day streak for a user.
 *
 * <p>A streak is a consecutive sequence of calendar days (UTC) on which the user completed at
 * least one study session. The streak counts from today backwards; if today has no session the
 * count starts from yesterday (so a user who hasn't studied yet today doesn't lose their streak).
 *
 * <p>Extracted as a shared component to avoid duplicating the identical logic between
 * {@link StudySessionService} and {@link AchievementEngine}.
 */
@Component
@RequiredArgsConstructor
public class StudyStreakCalculator {

    private final StudySessionRepository sessionRepository;

    /**
     * Returns the length of the current consecutive-day study streak for the given user.
     * Inspects up to the most recent 90 distinct study days.
     */
    public int calculate(UUID userId) {
        List<Date> dates = sessionRepository.findRecentStudyDates(userId, 90);
        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate expected = today;
        int streak = 0;

        for (Date sqlDate : dates) {
            LocalDate d = sqlDate.toLocalDate();
            // If streak is 0 and we have not yet studied today, start counting from yesterday.
            if (d.equals(expected) || (streak == 0 && d.equals(today.minusDays(1)))) {
                streak++;
                expected = d.minusDays(1);
            } else if (!d.equals(expected)) {
                break;
            }
        }
        return streak;
    }
}
