package com.careeros.user.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * A platform achievement in the catalog — admin-defined, system-unlocked.
 *
 * <p>The {@code code} is the stable, machine-readable key used by the {@code AchievementEngine} to
 * identify which achievements to unlock. Users earn achievements via {@link UserAchievement}.
 */
@Getter
@Setter
@Entity
@Table(
        name = "achievements",
        uniqueConstraints = @UniqueConstraint(name = "uk_achievements_code", columnNames = "code"),
        indexes = @Index(name = "idx_achievements_category", columnList = "category"))
public class Achievement extends AuditableEntity {

    @Column(name = "code", nullable = false, length = 80)
    private String code;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", length = 512)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private AchievementCategory category;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
