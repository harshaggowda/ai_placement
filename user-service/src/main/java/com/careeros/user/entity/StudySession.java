package com.careeros.user.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A timed study session — records start, pause, resume, and completion.
 *
 * <p>Pause/resume cycles accumulate into {@code total_paused_seconds}. Net study duration (minutes)
 * is computed from {@code (ended_at - started_at - total_paused_seconds)} and stored when the session
 * is completed. {@code roadmap_task_id} is a nullable logical reference to the task being studied;
 * an unlinked ad-hoc session is also valid.
 */
@Getter
@Setter
@Entity
@Table(
        name = "study_sessions",
        indexes = {
                @Index(name = "idx_study_sessions_user", columnList = "user_id"),
                @Index(name = "idx_study_sessions_started_at", columnList = "started_at"),
                @Index(name = "idx_study_sessions_status", columnList = "status")
        })
public class StudySession extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "roadmap_task_id")
    private UUID roadmapTaskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StudySessionStatus status = StudySessionStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "last_paused_at")
    private Instant lastPausedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    /** Accumulated seconds spent paused (to correctly compute net study time). */
    @Column(name = "total_paused_seconds", nullable = false)
    private long totalPausedSeconds = 0L;

    /** Net study duration in minutes — populated when the session completes. */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;
}
