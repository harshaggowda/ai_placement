-- =============================================================================
-- CareerOS Auth Service — initial schema (database: careeros_auth).
-- Columns mirror the JPA entities exactly so Hibernate `ddl-auto: validate` passes.
-- Conventions: UUID PKs, BIGINT optimistic-lock `version`, timestamptz audit columns,
-- nullable `deleted_at` for soft-deletable tables, snake_case, uk_/idx_/fk_ naming.
-- =============================================================================

-- ---------- reference / aggregate data (soft-deletable) ----------------------

CREATE TABLE users (
    id              UUID         NOT NULL,
    version         BIGINT       NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMPTZ,
    email           VARCHAR(254) NOT NULL,
    password_hash   VARCHAR(100),
    display_name    VARCHAR(100),
    status          VARCHAR(30)  NOT NULL,
    email_verified  BOOLEAN      NOT NULL,
    last_login_at   TIMESTAMPTZ,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_deleted_at ON users (deleted_at);

CREATE TABLE roles (
    id          UUID        NOT NULL,
    version     BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMPTZ,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE permissions (
    id          UUID         NOT NULL,
    version     BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMPTZ,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permissions_name UNIQUE (name)
);

-- ---------- RBAC join tables -------------------------------------------------

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);

CREATE TABLE role_permissions (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
);
CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);

-- ---------- credentials / sessions / audit (hard-deleted, auditable) ---------

CREATE TABLE refresh_tokens (
    id                     UUID         NOT NULL,
    version                BIGINT       NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    created_by             VARCHAR(100),
    updated_by             VARCHAR(100),
    user_id                UUID         NOT NULL,
    token_hash             VARCHAR(128) NOT NULL,
    expires_at             TIMESTAMPTZ  NOT NULL,
    revoked_at             TIMESTAMPTZ,
    replaced_by_token_hash VARCHAR(128),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

CREATE TABLE oauth_providers (
    id               UUID         NOT NULL,
    version          BIGINT       NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    user_id          UUID         NOT NULL,
    provider         VARCHAR(30)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(254),
    CONSTRAINT pk_oauth_providers PRIMARY KEY (id),
    CONSTRAINT uk_oauth_provider_identity UNIQUE (provider, provider_user_id),
    CONSTRAINT uk_oauth_provider_user UNIQUE (user_id, provider),
    CONSTRAINT fk_oauth_providers_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_oauth_providers_user ON oauth_providers (user_id);

CREATE TABLE user_sessions (
    id           UUID         NOT NULL,
    version      BIGINT       NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    user_id      UUID         NOT NULL,
    ip_address   VARCHAR(45),
    user_agent   VARCHAR(512),
    last_seen_at TIMESTAMPTZ,
    expires_at   TIMESTAMPTZ  NOT NULL,
    revoked_at   TIMESTAMPTZ,
    CONSTRAINT pk_user_sessions PRIMARY KEY (id),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_user_sessions_user ON user_sessions (user_id);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions (expires_at);

CREATE TABLE login_history (
    id             UUID         NOT NULL,
    version        BIGINT       NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    user_id        UUID,
    email          VARCHAR(254),
    ip_address     VARCHAR(45),
    user_agent     VARCHAR(512),
    successful     BOOLEAN      NOT NULL,
    failure_reason VARCHAR(100),
    CONSTRAINT pk_login_history PRIMARY KEY (id),
    CONSTRAINT fk_login_history_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_login_history_user ON login_history (user_id);
CREATE INDEX idx_login_history_created_at ON login_history (created_at);

CREATE TABLE password_reset_tokens (
    id         UUID         NOT NULL,
    version    BIGINT       NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    user_id    UUID         NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_password_reset_user ON password_reset_tokens (user_id);

CREATE TABLE email_verification_tokens (
    id          UUID         NOT NULL,
    version     BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    user_id     UUID         NOT NULL,
    token_hash  VARCHAR(128) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    verified_at TIMESTAMPTZ,
    CONSTRAINT pk_email_verification_tokens PRIMARY KEY (id),
    CONSTRAINT uk_email_verification_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_email_verification_user ON email_verification_tokens (user_id);
