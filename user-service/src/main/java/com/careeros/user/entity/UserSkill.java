package com.careeros.user.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Assignment of a catalog {@link Skill} to a user with proficiency metadata.
 *
 * <p>A first-class entity (not a {@code @ManyToMany} join table) because it carries attributes —
 * proficiency, years of experience, last-practiced date, and verification status. Hard-deleted:
 * remove the row when a user unassigns a skill.
 */
@Getter
@Setter
@Entity
@Table(
        name = "user_skills",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_skills", columnNames = {"user_id", "skill_id"}),
        indexes = {
                @Index(name = "idx_user_skills_user", columnList = "user_id"),
                @Index(name = "idx_user_skills_skill", columnList = "skill_id")
        })
public class UserSkill extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_skills_skill"))
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false, length = 20)
    private SkillProficiency proficiency = SkillProficiency.BEGINNER;

    @Column(name = "years_of_experience", precision = 4, scale = 1)
    private BigDecimal yearsOfExperience;

    @Column(name = "last_practiced_date")
    private LocalDate lastPracticedDate;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;
}
