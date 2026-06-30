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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Per-user dashboard personalisation settings. One row per user — {@code unique(user_id)}.
 * {@code widgets_config} is a free-form JSON blob the frontend drives; the backend only stores it.
 */
@Getter
@Setter
@Entity
@Table(
        name = "dashboard_settings",
        uniqueConstraints = @UniqueConstraint(name = "uk_dashboard_settings_user", columnNames = "user_id"),
        indexes = @Index(name = "idx_dashboard_settings_user", columnList = "user_id"))
public class DashboardSettings extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 10)
    private Theme theme = Theme.SYSTEM;

    @Column(name = "language", length = 10)
    private String language = "en";

    @Column(name = "timezone", length = 60)
    private String timezone = "UTC";

    @Column(name = "compact_mode", nullable = false)
    private boolean compactMode = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "widgets_config", columnDefinition = "jsonb")
    private String widgetsConfig;
}
