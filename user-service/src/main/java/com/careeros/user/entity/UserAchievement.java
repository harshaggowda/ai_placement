package com.careeros.user.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Records a user earning an {@link Achievement}. Idempotent — unique on {@code (user_id, achievement_id)}.
 *
 * <p>{@code metadata} carries contextual data (e.g. which skill triggered the unlock) as JSON so
 * the engine can emit rich notifications without schema changes.
 */
@Getter
@Setter
@Entity
@Table(
        name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_achievements", columnNames = {"user_id", "achievement_id"}),
        indexes = @Index(name = "idx_user_achievements_user", columnList = "user_id"))
public class UserAchievement extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_achievements_achievement"))
    private Achievement achievement;

    @Column(name = "earned_at", nullable = false)
    private Instant earnedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
}
