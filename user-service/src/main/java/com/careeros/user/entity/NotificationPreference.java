package com.careeros.user.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Per-user notification settings. One row per user — {@code unique(user_id)}.
 */
@Getter
@Setter
@Entity
@Table(
        name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_prefs_user", columnNames = "user_id"),
        indexes = @Index(name = "idx_notification_prefs_user", columnList = "user_id"))
public class NotificationPreference extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled = true;

    @Column(name = "weekly_digest", nullable = false)
    private boolean weeklyDigest = true;

    @Column(name = "goal_reminders", nullable = false)
    private boolean goalReminders = true;

    @Column(name = "roadmap_updates", nullable = false)
    private boolean roadmapUpdates = true;

    @Column(name = "achievement_alerts", nullable = false)
    private boolean achievementAlerts = true;

    @Column(name = "marketing_emails", nullable = false)
    private boolean marketingEmails = false;

    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;
}
