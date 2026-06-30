package com.careeros.user.entity;

import com.careeros.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Platform-wide skill catalog entry. Admin-managed; users assign skills via {@link UserSkill}.
 *
 * <p>Soft-deleted so existing user-skill assignments are preserved when a skill is retired.
 */
@Getter
@Setter
@Entity
@Table(
        name = "skills",
        uniqueConstraints = @UniqueConstraint(name = "uk_skills_name", columnNames = "name"),
        indexes = @Index(name = "idx_skills_category", columnList = "category"))
@SQLDelete(sql = "UPDATE skills SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Skill extends SoftDeletableEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 512)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private SkillCategory category;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;
}
