package com.careeros.user.service;

import com.careeros.user.event.GoalCompletedEvent;
import com.careeros.user.event.ProfileCreatedEvent;
import com.careeros.user.event.RoadmapCompletedEvent;
import com.careeros.user.event.SkillAssignedEvent;
import com.careeros.user.event.StudySessionCompletedEvent;
import com.careeros.user.entity.GoalStatus;
import com.careeros.user.entity.SkillProficiency;
import com.careeros.user.entity.UserAchievement;
import com.careeros.user.event.AchievementUnlockedEvent;
import com.careeros.user.repository.AchievementRepository;
import com.careeros.user.repository.GoalRepository;
import com.careeros.user.repository.UserAchievementRepository;
import com.careeros.user.repository.UserProfileRepository;
import com.careeros.user.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.Instant;
import java.util.UUID;

/**
 * Listens for domain events and unlocks achievements when criteria match. Runs async in separate
 * transactions (REQUIRES_NEW) so a failed unlock never aborts the originating transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementEngine {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProfileRepository profileRepository;
    private final GoalRepository goalRepository;
    private final UserSkillRepository userSkillRepository;
    private final ProfileCompletionCalculator completionCalculator;
    private final StudyStreakCalculator studyStreakCalculator;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ProfileCreatedEvent event) {
        checkProfileCompletion(event.userId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(GoalCompletedEvent event) {
        long firstGoalDone = goalRepository.countByUserIdAndStatus(event.userId(), GoalStatus.COMPLETED);
        if (firstGoalDone == 1) {
            tryUnlock(event.userId(), "FIRST_GOAL_DONE");
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(SkillAssignedEvent event) {
        long count = userSkillRepository.countByUserId(event.userId());
        if (count == 1) {
            tryUnlock(event.userId(), "FIRST_SKILL");
        } else {
            long expert = userSkillRepository.countByUserIdAndProficiency(event.userId(), SkillProficiency.EXPERT);
            if (expert == 1) {
                tryUnlock(event.userId(), "SKILL_EXPERT");
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(RoadmapCompletedEvent event) {
        tryUnlock(event.userId(), "ROADMAP_COMPLETE");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(StudySessionCompletedEvent event) {
        // tryUnlock is idempotent — it checks existsByUserIdAndAchievementCode before saving.
        tryUnlock(event.userId(), "FIRST_SESSION");
        int streak = studyStreakCalculator.calculate(event.userId());
        if (streak == 7) {
            tryUnlock(event.userId(), "STUDY_STREAK_7");
        } else if (streak == 30) {
            tryUnlock(event.userId(), "STUDY_STREAK_30");
        }
    }

    private void checkProfileCompletion(UUID userId) {
        profileRepository.findByUserId(userId).ifPresent(profile -> {
            if (completionCalculator.calculate(profile) == 100) {
                tryUnlock(userId, "PROFILE_COMPLETE");
            }
        });
    }

    private void tryUnlock(UUID userId, String code) {
        if (userAchievementRepository.existsByUserIdAndAchievementCode(userId, code)) {
            return;
        }
        achievementRepository.findByCode(code).ifPresent(achievement -> {
            if (!achievement.isActive()) return;
            UserAchievement ua = new UserAchievement();
            ua.setUserId(userId);
            ua.setAchievement(achievement);
            ua.setEarnedAt(Instant.now());
            userAchievementRepository.save(ua);
            eventPublisher.publishEvent(new AchievementUnlockedEvent(userId, achievement.getId(), code));
            log.info("Achievement unlocked: userId={} code={}", userId, code);
        });
    }

}

