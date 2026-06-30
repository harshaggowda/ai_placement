# CareerOS AI — Domain Model & Database Design (Phase 2)

Production-grade domain model for the JVM microservices. **Auth Service is implemented as code**
(reference service); the other four services are specified here and implemented in a later phase.

> Scope: domain modeling + database design only. **No** business logic, authentication, or AI.

---

## 1. Global design conventions

Every table and entity follows these rules (enforced via shared base classes in `careeros-common`):

| Concern | Decision | Mechanism |
|---|---|---|
| **Primary key** | `UUID` (v4), application/DB generated, non-sequential | `BaseEntity.id` (`@GeneratedValue(strategy = UUID)`) |
| **Optimistic locking** | `version BIGINT` on every row | `BaseEntity.version` (`@Version`) |
| **Audit** | `created_at, updated_at, created_by, updated_by` | `AuditableEntity` + Spring Data `AuditingEntityListener` |
| **Soft delete** | nullable `deleted_at`; `delete` → `UPDATE`; queries filtered | `SoftDeletableEntity` + `@SQLDelete` / `@SQLRestriction` |
| **Naming** | `snake_case` tables/columns; plural tables; `uk_*` unique, `idx_*` index, `fk_*` FK | explicit `@Table`/`@Column`/`@ForeignKey` |
| **Enums** | stored as `STRING` (`@Enumerated(EnumType.STRING)`), never ordinal | — |
| **Money** | `NUMERIC(19,4)` + ISO-4217 `currency` char(3); never floating point | — |
| **Timestamps** | `timestamptz` (UTC), Java `Instant` | platform-wide UTC |
| **Normalization** | 3NF; no duplicated data; lookups via FKs / join tables | — |
| **Fetch strategy** | all associations `LAZY`; load explicitly when needed | `@ManyToOne(fetch = LAZY)` etc. |

**Which base class to extend:**
- `SoftDeletableEntity` — long-lived aggregate/reference data (User, Resume, Plan, Subscription…).
- `AuditableEntity` — append-only or short-lived records (LoginHistory, tokens, WebhookEvent, logs).
- `BaseEntity` — only when audit metadata is genuinely irrelevant (rare).

---

## 2. Database-per-service & cross-service references

Each service owns a **separate PostgreSQL database**; there are **no cross-database foreign keys**.

```
careeros_auth     ← auth-service          careeros_payment  ← payment-service
careeros_user     ← user-service          careeros_ai       ← ai-orchestrator-service
careeros_career   ← career-service
```

A row in one service that refers to a user stores a **bare `user_id UUID`** (the auth user id) —
indexed, but **not** a FK (the target lives in another database). Referential integrity across
services is maintained by the application / future domain events, not by the DB. This is the
deliberate trade-off of the database-per-service pattern: independent schema evolution and scaling
in exchange for application-enforced cross-service integrity.

```
        ┌───────────────┐  user_id (UUID, logical ref)  ┌────────────────┐
        │  auth.users   │◀──────────────────────────────│ user.* / career.* /
        │  (source of   │                                │ payment.* / ai.* │
        │   identity)   │                                └────────────────┘
        └───────────────┘   no physical FK across DBs
```

---

## 3. Cross-service ER overview (text)

```
AUTH (careeros_auth)
  users 1───* refresh_tokens
  users 1───* oauth_providers
  users 1───* user_sessions
  users 1───* login_history
  users 1───* password_reset_tokens
  users 1───* email_verification_tokens
  users *───* roles  (user_roles)
  roles *───* permissions  (role_permissions)

USER (careeros_user)            — keyed by user_id (→ auth.users)
  user_profiles 1───1 (user_id)        user_profiles 1───* user_skills *───1 skills
  user_profiles 1───* goals            goals 1───* roadmaps 1───* roadmap_tasks
  roadmap_tasks 1───* study_sessions   user 1───* achievements
  user 1───1 notification_preferences  user 1───1 dashboard_settings

CAREER (careeros_career)        — keyed by user_id (→ auth.users)
  resumes 1───* resume_versions 1───1 resume_analyses
  interviews 1───* interview_questions 1───* interview_answers
  interviews 1───1 interview_feedback
  applications *───1 companies   companies 1───* company_preparations
  user 1───* leetcode_progress / sql_progress
  projects 1───* project_reviews   user 1───* certificates
  learning_resources, daily_plans, progress_snapshots (user-scoped)

PAYMENT (careeros_payment)      — keyed by user_id (→ auth.users)
  plans 1───* subscriptions     subscriptions 1───* invoices 1───* payments
  invoices *───0..1 coupons     subscriptions 1───* usage_records
  webhook_events (standalone, idempotent ingest)

AI ORCHESTRATOR (careeros_ai)   — keyed by user_id (→ auth.users). Metadata only; NO embeddings.
  conversations 1───* conversation_messages
  agent_executions 1───* tool_executions
  agent_executions 1───1 token_usage
  prompt_history, ai_request_logs, model_configurations
```

---

## 4. Auth Service (IMPLEMENTED as code)

DB `careeros_auth`. Entities under `com.careeros.auth.entity`; repositories under `…repository`;
DTOs under `…dto`. Tables:

| Table | Base | Soft-del | Key columns | Unique | Indexes |
|---|---|---|---|---|---|
| `users` | SoftDeletable | ✓ | email, password_hash(null), display_name, status, email_verified, last_login_at | `uk_users_email` | status, deleted_at |
| `roles` | SoftDeletable | ✓ | name, description | `uk_roles_name` | — |
| `permissions` | SoftDeletable | ✓ | name, description | `uk_permissions_name` | — |
| `user_roles` (join) | — | — | user_id, role_id | `uk_user_roles` | (FKs) |
| `role_permissions` (join) | — | — | role_id, permission_id | `uk_role_permissions` | (FKs) |
| `refresh_tokens` | Auditable | — | user_id→users, token_hash, expires_at, revoked_at, replaced_by_token_hash | `uk_refresh_tokens_token_hash` | user_id, expires_at |
| `oauth_providers` | Auditable | — | user_id→users, provider, provider_user_id, email | `uk_oauth_provider_identity`, `uk_oauth_provider_user` | user_id |
| `user_sessions` | Auditable | — | user_id→users, ip_address, user_agent, last_seen_at, expires_at, revoked_at | — | user_id, expires_at |
| `login_history` | Auditable | — | user_id→users(null), email, ip_address, user_agent, successful, failure_reason | — | user_id, created_at |
| `password_reset_tokens` | Auditable | — | user_id→users, token_hash, expires_at, used_at | `uk_password_reset_token_hash` | user_id |
| `email_verification_tokens` | Auditable | — | user_id→users, token_hash, expires_at, verified_at | `uk_email_verification_token_hash` | user_id |

**Relationships:** `User *↔* Role` (owning side `User`), `Role *↔* Permission` (owning side `Role`);
all token/session/history tables `*→1 User` (`@ManyToOne(LAZY)`). Child collections are **not**
mapped on `User` (queried via repositories) to keep the aggregate small and avoid unbounded loads.

**Repositories (9):** `User/Role/Permission/RefreshToken/OAuthProvider/UserSession/LoginHistory/
PasswordResetToken/EmailVerificationToken` — `JpaRepository<E, UUID>` + derived finders (e.g.
`findByEmail`, `findByTokenHash`, `deleteByExpiresAtBefore`).

**DTOs (19):** full `Create/Update/Response/Summary/Search` for `User`, `Role`, `Permission`;
read-only `Response`(+`Search`) for `UserSession`, `LoginHistory`, `OAuthProvider`.
**Design note:** `RefreshToken`, `PasswordResetToken`, `EmailVerificationToken` intentionally have
**no outward DTOs** — they hold secrets/hashes and are never serialized to clients.

**API contracts (designed; controllers not implemented):**
`/api/auth` is the auth surface; admin RBAC management under `/api/auth/admin`.
- `POST /register`, `POST /login`, `POST /refresh`, `POST /logout` *(behaviour deferred to auth phase)*
- `GET /me`, `GET /me/sessions`, `DELETE /me/sessions/{id}`, `GET /me/login-history`
- Admin: `GET/POST/PUT/PATCH/DELETE /admin/users`, `…/roles`, `…/permissions`, `POST …/search`

> The remaining services follow the **same conventions**; see the per-service documents:
> [`user-service.md`](user-service.md) · [`career-service.md`](career-service.md) ·
> [`payment-service.md`](payment-service.md) · [`ai-orchestrator-service.md`](ai-orchestrator-service.md)
