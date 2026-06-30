-- =============================================================================
-- CareerOS User Service — initial schema (database: careeros_user).
-- Columns mirror the JPA entities so Hibernate `ddl-auto: validate` passes.
-- User-owned rows carry a logical `user_id` (the auth-service user id; no cross-DB FK).
-- Schema-light list fields are stored as jsonb.
-- =============================================================================

CREATE TABLE user_profiles (
    id                   UUID         NOT NULL,
    version              BIGINT       NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL,
    updated_at           TIMESTAMPTZ  NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    deleted_at           TIMESTAMPTZ,
    user_id              UUID         NOT NULL,
    headline             VARCHAR(200),
    bio                  TEXT,
    location             VARCHAR(120),
    avatar_url           VARCHAR(512),
    experience_level     VARCHAR(20),
    target_company       VARCHAR(160),
    target_role          VARCHAR(120),
    graduation_year      INTEGER,
    current_semester     INTEGER,
    github_url           VARCHAR(512),
    linkedin_url         VARCHAR(512),
    website_url          VARCHAR(512),
    twitter_url          VARCHAR(512),
    preferred_languages  JSONB,
    preferred_tech_stack JSONB,
    education            JSONB,
    experience           JSONB,
    CONSTRAINT pk_user_profiles PRIMARY KEY (id),
    CONSTRAINT uk_user_profiles_user UNIQUE (user_id)
);
CREATE INDEX idx_user_profiles_user ON user_profiles (user_id);

CREATE TABLE goals (
    id               UUID         NOT NULL,
    version          BIGINT       NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    deleted_at       TIMESTAMPTZ,
    user_id          UUID         NOT NULL,
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    category         VARCHAR(30)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    priority         VARCHAR(10)  NOT NULL,
    progress_percent INTEGER      NOT NULL,
    target_date      DATE,
    completed_at     TIMESTAMPTZ,
    CONSTRAINT pk_goals PRIMARY KEY (id)
);
CREATE INDEX idx_goals_user ON goals (user_id);
CREATE INDEX idx_goals_status ON goals (status);
