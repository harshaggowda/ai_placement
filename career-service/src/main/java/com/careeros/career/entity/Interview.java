package com.careeros.career.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@SQLDelete(sql = "UPDATE interviews SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Interview extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private InterviewType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 50)
    private InterviewFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "round_name", length = 255)
    private String roundName;

    @Column(name = "meeting_url", length = 1000)
    private String meetingUrl;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
