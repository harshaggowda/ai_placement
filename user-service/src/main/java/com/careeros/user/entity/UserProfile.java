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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A user's profile and personalization data — the User Service aggregate root.
 *
 * <p>Keyed by {@code user_id} (the auth-service user id; one profile per user, no cross-DB FK).
 * List-valued, schema-light fields (preferred languages/stack, education, experience) are stored as
 * {@code jsonb}. Soft-deleted.
 */
@Getter
@Setter
@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_profiles_user", columnNames = "user_id"),
        indexes = @Index(name = "idx_user_profiles_user", columnList = "user_id"))
@SQLDelete(sql = "UPDATE user_profiles SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserProfile extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "headline", length = 200)
    private String headline;

    @Column(name = "bio", columnDefinition = "text")
    private String bio;

    @Column(name = "location", length = 120)
    private String location;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 20)
    private ExperienceLevel experienceLevel;

    @Column(name = "target_company", length = 160)
    private String targetCompany;

    @Column(name = "target_role", length = 120)
    private String targetRole;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "current_semester")
    private Integer currentSemester;

    @Column(name = "github_url", length = 512)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 512)
    private String linkedinUrl;

    @Column(name = "website_url", length = 512)
    private String websiteUrl;

    @Column(name = "twitter_url", length = 512)
    private String twitterUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_languages", columnDefinition = "jsonb")
    private List<String> preferredLanguages = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_tech_stack", columnDefinition = "jsonb")
    private List<String> preferredTechStack = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "education", columnDefinition = "jsonb")
    private List<String> education = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "experience", columnDefinition = "jsonb")
    private List<String> experience = new ArrayList<>();
}
