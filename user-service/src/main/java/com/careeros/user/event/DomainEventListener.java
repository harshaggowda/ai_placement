package com.careeros.user.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * In-process consumer of domain events.
 *
 * <p>Handlers run {@code AFTER_COMMIT} so side effects never fire for a rolled-back transaction.
 * Today they log; when a message bus is introduced these become the publish points — the events and
 * call sites stay unchanged.
 */
@Slf4j
@Component
public class DomainEventListener {

    @TransactionalEventListener
    public void on(ProfileCreatedEvent event) {
        log.info("event=ProfileCreated userId={} profileId={}", event.userId(), event.profileId());
    }

    @TransactionalEventListener
    public void on(ProfileUpdatedEvent event) {
        log.info("event=ProfileUpdated userId={} profileId={}", event.userId(), event.profileId());
    }

    @TransactionalEventListener
    public void on(GoalCompletedEvent event) {
        log.info("event=GoalCompleted userId={} goalId={} title='{}'", event.userId(), event.goalId(), event.title());
    }

    @TransactionalEventListener
    public void on(RoadmapCompletedEvent event) {
        log.info("event=RoadmapCompleted userId={} roadmapId={}", event.userId(), event.roadmapId());
    }

    @TransactionalEventListener
    public void on(AchievementUnlockedEvent event) {
        log.info("event=AchievementUnlocked userId={} achievementId={} code={}",
                event.userId(), event.achievementId(), event.code());
    }

    @TransactionalEventListener
    public void on(SkillAssignedEvent event) {
        log.info("event=SkillAssigned userId={} skillId={} skillName='{}'",
                event.userId(), event.skillId(), event.skillName());
    }

    @TransactionalEventListener
    public void on(StudySessionCompletedEvent event) {
        log.info("event=StudySessionCompleted userId={} sessionId={} durationMinutes={}",
                event.userId(), event.sessionId(), event.durationMinutes());
    }
}
