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
 * A structured learning path owned by a user, optionally linked to a {@link Goal}.
 *
 * <p>{@code goal_id} is a logical reference to a goal in the same database — stored as a UUID with
 * a SQL FK but not a JPA {@code @ManyToOne} (keeps the aggregate boundary clean; goals are fetched
 * independently). Soft-deleted. {@code is_template} marks reusable roadmap templates.
 */
@Getter
@Setter
@Entity
@Table(
        name = "roadmaps",
        indexes = {
                @Index(name = "idx_roadmaps_user", columnList = "user_id"),
                @Index(name = "idx_roadmaps_goal", columnList = "goal_id"),
                @Index(name = "idx_roadmaps_status", columnList = "status")
        })
@SQLDelete(sql = "UPDATE roadmaps SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Roadmap extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Logical FK to goals (same DB). Nullable — a roadmap can exist without a parent goal. */
    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoadmapStatus status = RoadmapStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "is_template", nullable = false)
    private boolean isTemplate = false;
}
