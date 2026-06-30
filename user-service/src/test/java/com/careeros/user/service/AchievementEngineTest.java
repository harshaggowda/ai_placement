package com.careeros.user.service;

import com.careeros.user.entity.Achievement;
import com.careeros.user.entity.GoalStatus;
import com.careeros.user.entity.SkillProficiency;
import com.careeros.user.entity.UserAchievement;
import com.careeros.user.event.GoalCompletedEvent;
import com.careeros.user.event.RoadmapCompletedEvent;
import com.careeros.user.event.SkillAssignedEvent;
import com.careeros.user.event.StudySessionCompletedEvent;
import com.careeros.user.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AchievementEngine}.
 *
 * <p>After Bug Fix #1 the engine no longer injects {@code RoadmapRepository} or
 * {@code StudySessionRepository} directly — those were removed and replaced by
 * {@link StudyStreakCalculator}. Mock list updated accordingly.
 */
@ExtendWith(MockitoExtension.class)
class AchievementEngineTest {

    @Mock AchievementRepository achievementRepository;
    @Mock UserAchievementRepository userAchievementRepository;
    @Mock UserProfileRepository profileRepository;
    @Mock GoalRepository goalRepository;
    @Mock UserSkillRepository userSkillRepository;
    @Mock ProfileCompletionCalculator completionCalculator;
    @Mock StudyStreakCalculator studyStreakCalculator;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks AchievementEngine engine;

    private final UUID userId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // Goal events
    // -----------------------------------------------------------------------

    @Test
    void firstGoalCompletedUnlocksAchievement() {
        when(goalRepository.countByUserIdAndStatus(userId, GoalStatus.COMPLETED)).thenReturn(1L);
        stubUnlock("FIRST_GOAL_DONE");

        engine.on(new GoalCompletedEvent(userId, UUID.randomUUID(), "Test"));

        verify(userAchievementRepository).save(any(UserAchievement.class));
        verify(eventPublisher).publishEvent(any(com.careeros.user.event.AchievementUnlockedEvent.class));
    }

    @Test
    void subsequentGoalCompletedDoesNotUnlock() {
        when(goalRepository.countByUserIdAndStatus(userId, GoalStatus.COMPLETED)).thenReturn(2L);

        engine.on(new GoalCompletedEvent(userId, UUID.randomUUID(), "Another goal"));

        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void alreadyUnlockedAchievementIsSkipped() {
        when(goalRepository.countByUserIdAndStatus(userId, GoalStatus.COMPLETED)).thenReturn(1L);
        when(userAchievementRepository.existsByUserIdAndAchievementCode(userId, "FIRST_GOAL_DONE"))
                .thenReturn(true);

        engine.on(new GoalCompletedEvent(userId, UUID.randomUUID(), "Third goal"));

        verify(userAchievementRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Skill events
    // -----------------------------------------------------------------------

    @Test
    void firstSkillAssignedUnlocksAchievement() {
        when(userSkillRepository.countByUserId(userId)).thenReturn(1L);
        stubUnlock("FIRST_SKILL");

        engine.on(new SkillAssignedEvent(userId, UUID.randomUUID(), "Java"));

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void expertSkillUnlocksAchievement() {
        when(userSkillRepository.countByUserId(userId)).thenReturn(5L);
        when(userSkillRepository.countByUserIdAndProficiency(userId, SkillProficiency.EXPERT)).thenReturn(1L);
        stubUnlock("SKILL_EXPERT");

        engine.on(new SkillAssignedEvent(userId, UUID.randomUUID(), "Spring"));

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void inactiveAchievementIsNotUnlocked() {
        when(goalRepository.countByUserIdAndStatus(userId, GoalStatus.COMPLETED)).thenReturn(1L);
        when(userAchievementRepository.existsByUserIdAndAchievementCode(userId, "FIRST_GOAL_DONE"))
                .thenReturn(false);
        Achievement inactive = makeAchievement("FIRST_GOAL_DONE");
        inactive.setActive(false);
        when(achievementRepository.findByCode("FIRST_GOAL_DONE")).thenReturn(Optional.of(inactive));

        engine.on(new GoalCompletedEvent(userId, UUID.randomUUID(), "Goal"));

        verify(userAchievementRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Roadmap events
    // -----------------------------------------------------------------------

    @Test
    void roadmapCompletedAlwaysTriesUnlock() {
        stubUnlock("ROADMAP_COMPLETE");

        engine.on(new RoadmapCompletedEvent(userId, UUID.randomUUID()));

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    // -----------------------------------------------------------------------
    // Study session events
    // -----------------------------------------------------------------------

    @Test
    void studySessionCompletedUnlocksFirstSession() {
        when(studyStreakCalculator.calculate(userId)).thenReturn(1);
        stubUnlock("FIRST_SESSION");
        // streak of 1 should not trigger streak achievements
        lenient().when(userAchievementRepository.existsByUserIdAndAchievementCode(userId, "STUDY_STREAK_7"))
                .thenReturn(false);

        engine.on(new StudySessionCompletedEvent(userId, UUID.randomUUID(), 30));

        verify(userAchievementRepository, atLeastOnce()).save(any(UserAchievement.class));
    }

    @Test
    void sevenDayStreakUnlocksStreakAchievement() {
        when(studyStreakCalculator.calculate(userId)).thenReturn(7);
        // Allow FIRST_SESSION to be already unlocked
        when(userAchievementRepository.existsByUserIdAndAchievementCode(userId, "FIRST_SESSION"))
                .thenReturn(true);
        stubUnlock("STUDY_STREAK_7");

        engine.on(new StudySessionCompletedEvent(userId, UUID.randomUUID(), 45));

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    @Test
    void thirtyDayStreakUnlocksStreakAchievement() {
        when(studyStreakCalculator.calculate(userId)).thenReturn(30);
        when(userAchievementRepository.existsByUserIdAndAchievementCode(userId, "FIRST_SESSION"))
                .thenReturn(true);
        stubUnlock("STUDY_STREAK_30");

        engine.on(new StudySessionCompletedEvent(userId, UUID.randomUUID(), 60));

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void stubUnlock(String code) {
        when(userAchievementRepository.existsByUserIdAndAchievementCode(userId, code))
                .thenReturn(false);
        Achievement ach = makeAchievement(code);
        when(achievementRepository.findByCode(code)).thenReturn(Optional.of(ach));
    }

    private static Achievement makeAchievement(String code) {
        Achievement ach = new Achievement();
        ach.setId(UUID.randomUUID());
        ach.setCode(code);
        ach.setActive(true);
        return ach;
    }
}
