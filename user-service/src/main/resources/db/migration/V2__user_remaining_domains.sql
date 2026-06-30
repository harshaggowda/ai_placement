-- =============================================================================
-- CareerOS User Service — V2: Skills, Roadmaps, Study Sessions, Achievements,
-- Dashboard Settings, Notification Preferences.
-- Every column mirrors its JPA entity exactly so Hibernate ddl-auto=validate passes.
-- =============================================================================

-- ---------- Skill catalog (admin-managed, soft-deleted) ----------------------

CREATE TABLE skills (
    id          UUID         NOT NULL,
    version     BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMPTZ,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(512),
    category    VARCHAR(30)  NOT NULL,
    icon_url    VARCHAR(512),
    verified    BOOLEAN      NOT NULL,
    CONSTRAINT pk_skills PRIMARY KEY (id),
    CONSTRAINT uk_skills_name UNIQUE (name)
);
CREATE INDEX idx_skills_category ON skills (category);

-- ---------- User–skill assignment (hard-deleted, AuditableEntity) ------------

CREATE TABLE user_skills (
    id                   UUID         NOT NULL,
    version              BIGINT       NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL,
    updated_at           TIMESTAMPTZ  NOT NULL,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    user_id              UUID         NOT NULL,
    skill_id             UUID         NOT NULL,
    proficiency          VARCHAR(20)  NOT NULL,
    years_of_experience  NUMERIC(4,1),
    last_practiced_date  DATE,
    is_verified          BOOLEAN      NOT NULL,
    CONSTRAINT pk_user_skills PRIMARY KEY (id),
    CONSTRAINT uk_user_skills UNIQUE (user_id, skill_id),
    CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
);
CREATE INDEX idx_user_skills_user ON user_skills (user_id);
CREATE INDEX idx_user_skills_skill ON user_skills (skill_id);

-- ---------- Roadmaps (soft-deleted) ------------------------------------------

CREATE TABLE roadmaps (
    id           UUID         NOT NULL,
    version      BIGINT       NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    deleted_at   TIMESTAMPTZ,
    user_id      UUID         NOT NULL,
    goal_id      UUID,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    status       VARCHAR(20)  NOT NULL,
    start_date   DATE,
    target_date  DATE,
    completed_at TIMESTAMPTZ,
    is_template  BOOLEAN      NOT NULL,
    CONSTRAINT pk_roadmaps PRIMARY KEY (id),
    CONSTRAINT fk_roadmaps_goal FOREIGN KEY (goal_id) REFERENCES goals (id)
);
CREATE INDEX idx_roadmaps_user   ON roadmaps (user_id);
CREATE INDEX idx_roadmaps_goal   ON roadmaps (goal_id);
CREATE INDEX idx_roadmaps_status ON roadmaps (status);

-- ---------- Roadmap tasks (soft-deleted) -------------------------------------

CREATE TABLE roadmap_tasks (
    id                 UUID         NOT NULL,
    version            BIGINT       NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    deleted_at         TIMESTAMPTZ,
    roadmap_id         UUID         NOT NULL,
    title              VARCHAR(200) NOT NULL,
    description        TEXT,
    status             VARCHAR(20)  NOT NULL,
    priority           VARCHAR(10)  NOT NULL,
    order_index        INTEGER      NOT NULL,
    estimated_hours    NUMERIC(6,2),
    actual_hours       NUMERIC(6,2),
    due_date           DATE,
    completed_at       TIMESTAMPTZ,
    dependency_task_id UUID,
    CONSTRAINT pk_roadmap_tasks PRIMARY KEY (id),
    CONSTRAINT fk_roadmap_tasks_roadmap FOREIGN KEY (roadmap_id) REFERENCES roadmaps (id)
);
CREATE INDEX idx_roadmap_tasks_roadmap ON roadmap_tasks (roadmap_id);
CREATE INDEX idx_roadmap_tasks_status  ON roadmap_tasks (status);

-- ---------- Study sessions (AuditableEntity, never deleted) ------------------

CREATE TABLE study_sessions (
    id                    UUID         NOT NULL,
    version               BIGINT       NOT NULL,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    user_id               UUID         NOT NULL,
    roadmap_task_id       UUID,
    status                VARCHAR(20)  NOT NULL,
    started_at            TIMESTAMPTZ  NOT NULL,
    last_paused_at        TIMESTAMPTZ,
    ended_at              TIMESTAMPTZ,
    total_paused_seconds  BIGINT       NOT NULL,
    duration_minutes      INTEGER,
    notes                 TEXT,
    CONSTRAINT pk_study_sessions PRIMARY KEY (id)
);
CREATE INDEX idx_study_sessions_user       ON study_sessions (user_id);
CREATE INDEX idx_study_sessions_started_at ON study_sessions (started_at);
CREATE INDEX idx_study_sessions_status     ON study_sessions (status);

-- ---------- Achievement catalog (AuditableEntity) ----------------------------

CREATE TABLE achievements (
    id          UUID         NOT NULL,
    version     BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    code        VARCHAR(80)  NOT NULL,
    title       VARCHAR(160) NOT NULL,
    description VARCHAR(512),
    category    VARCHAR(20)  NOT NULL,
    icon_url    VARCHAR(512),
    is_active   BOOLEAN      NOT NULL,
    CONSTRAINT pk_achievements PRIMARY KEY (id),
    CONSTRAINT uk_achievements_code UNIQUE (code)
);
CREATE INDEX idx_achievements_category ON achievements (category);

-- ---------- User–achievement join (AuditableEntity) --------------------------

CREATE TABLE user_achievements (
    id             UUID         NOT NULL,
    version        BIGINT       NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    user_id        UUID         NOT NULL,
    achievement_id UUID         NOT NULL,
    earned_at      TIMESTAMPTZ  NOT NULL,
    metadata       JSONB,
    CONSTRAINT pk_user_achievements PRIMARY KEY (id),
    CONSTRAINT uk_user_achievements UNIQUE (user_id, achievement_id),
    CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (achievement_id) REFERENCES achievements (id)
);
CREATE INDEX idx_user_achievements_user ON user_achievements (user_id);

-- ---------- Dashboard settings (1-1, AuditableEntity) -----------------------

CREATE TABLE dashboard_settings (
    id             UUID         NOT NULL,
    version        BIGINT       NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    user_id        UUID         NOT NULL,
    theme          VARCHAR(10)  NOT NULL,
    language       VARCHAR(10),
    timezone       VARCHAR(60),
    compact_mode   BOOLEAN      NOT NULL,
    widgets_config JSONB,
    CONSTRAINT pk_dashboard_settings PRIMARY KEY (id),
    CONSTRAINT uk_dashboard_settings_user UNIQUE (user_id)
);
CREATE INDEX idx_dashboard_settings_user ON dashboard_settings (user_id);

-- ---------- Notification preferences (1-1, AuditableEntity) -----------------

CREATE TABLE notification_preferences (
    id                  UUID         NOT NULL,
    version             BIGINT       NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    user_id             UUID         NOT NULL,
    email_enabled       BOOLEAN      NOT NULL,
    push_enabled        BOOLEAN      NOT NULL,
    weekly_digest       BOOLEAN      NOT NULL,
    goal_reminders      BOOLEAN      NOT NULL,
    roadmap_updates     BOOLEAN      NOT NULL,
    achievement_alerts  BOOLEAN      NOT NULL,
    marketing_emails    BOOLEAN      NOT NULL,
    quiet_hours_start   TIME,
    quiet_hours_end     TIME,
    CONSTRAINT pk_notification_preferences PRIMARY KEY (id),
    CONSTRAINT uk_notification_prefs_user UNIQUE (user_id)
);
CREATE INDEX idx_notification_prefs_user ON notification_preferences (user_id);

-- ---------- Seed achievement catalog -----------------------------------------

INSERT INTO achievements (id, version, created_at, updated_at, created_by, code, title, description, category, is_active) VALUES
    (gen_random_uuid(), 0, now(), now(), 'system', 'PROFILE_COMPLETE',   'Profile Pro',          'Completed your profile to 100%',         'PROFILE', true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'FIRST_GOAL',         'Goal Setter',          'Created your first goal',                'GOAL',    true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'FIRST_GOAL_DONE',    'Goal Crusher',         'Completed your first goal',              'GOAL',    true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'FIRST_SKILL',        'Skill Starter',        'Added your first skill',                 'SKILL',   true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'SKILL_EXPERT',       'Expert Status',        'Reached Expert level in any skill',      'SKILL',   true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'FIRST_ROADMAP',      'Road Builder',         'Created your first roadmap',             'MILESTONE', true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'ROADMAP_COMPLETE',   'Road Runner',          'Completed your first roadmap',           'MILESTONE', true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'STUDY_STREAK_7',     '7-Day Streak',         'Studied for 7 consecutive days',         'STREAK',  true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'STUDY_STREAK_30',    '30-Day Streak',        'Studied for 30 consecutive days',        'STREAK',  true),
    (gen_random_uuid(), 0, now(), now(), 'system', 'FIRST_SESSION',      'First Steps',          'Completed your first study session',     'STUDY',   true);
