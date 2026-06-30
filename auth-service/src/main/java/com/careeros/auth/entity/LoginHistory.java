package com.careeros.auth.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Append-only record of an authentication attempt (success or failure).
 *
 * <p>{@code user} is nullable so attempts against an unknown email are still recorded (with the raw
 * {@code email} captured). The inherited {@code created_at} is the attempt timestamp. Used for
 * security auditing and anomaly detection; never updated.
 */
@Getter
@Setter
@Entity
@Table(
        name = "login_history",
        indexes = {
                @Index(name = "idx_login_history_user", columnList = "user_id"),
                @Index(name = "idx_login_history_created_at", columnList = "created_at")
        })
public class LoginHistory extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_login_history_user"))
    private User user;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;
}
