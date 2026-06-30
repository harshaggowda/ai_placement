package com.careeros.user.entity;

import com.careeros.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A user-defined goal (career, skill, interview, certification…).
 *
 * <p>Owned directly by {@code user_id} for straightforward ownership checks. Tracks status, priority,
 * a 0–100 progress percentage, an optional target date, and the completion timestamp. Soft-deleted.
 */
@Getter
@Setter
@Entity
@Table(
        name = "goals",
        indexes = {
                @Index(name = "idx_goals_user", columnList = "user_id"),
                @Index(name = "idx_goals_status", columnList = "status")
        })
@SQLDelete(sql = "UPDATE goals SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Goal extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private GoalCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status = GoalStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private GoalPriority priority = GoalPriority.MEDIUM;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent = 0;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "completed_at")
    private Instant completedAt;
}
