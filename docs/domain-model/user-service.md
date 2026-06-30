# User Service — Domain Model & Database Design

Database: **`careeros_user`**. Base package (future): `com.careeros.user`. Conventions per
[`00-overview-erd.md`](00-overview-erd.md) (UUID PK, `@Version`, audit, soft delete, snake_case).
User identity lives in auth-service; here every user-owned row carries a logical `user_id UUID`
(indexed, no cross-DB FK).

## Entities (10)

| Table | Base | Soft-del | Key columns (type, null) | Unique | Indexes | FKs |
|---|---|---|---|---|---|---|
| `user_profiles` | SoftDeletable | ✓ | `user_id`(uuid,NN), headline(varchar200), bio(text), location(varchar120), avatar_url(varchar512), `experience_level`(enum), target_role(varchar120), github_url, linkedin_url, website_url | `uk_user_profiles_user`(user_id) | user_id | — |
| `skills` | SoftDeletable | ✓ | name(varchar100,NN), `category`(enum,NN), description(varchar255) | `uk_skills_name`(name) | category | — |
| `user_skills` | Auditable | — | `user_profile_id`(NN), `skill_id`(NN), `proficiency`(enum,NN), years_experience(numeric4,1), endorsed(bool,NN) | `uk_user_skills`(user_profile_id,skill_id) | user_profile_id, skill_id | →user_profiles, →skills |
| `goals` | SoftDeletable | ✓ | `user_profile_id`(NN), title(varchar200,NN), description(text), `category`(enum,NN), `status`(enum,NN), target_date(date), progress_percent(int,NN) | — | user_profile_id, status | →user_profiles |
| `roadmaps` | SoftDeletable | ✓ | `goal_id`(NN), title(varchar200,NN), description(text), `status`(enum,NN), start_date(date), target_date(date) | — | goal_id, status | →goals |
| `roadmap_tasks` | SoftDeletable | ✓ | `roadmap_id`(NN), title(varchar200,NN), description(text), `status`(enum,NN), order_index(int,NN), estimated_hours(numeric5,1), due_date(date), completed_at(ts) | — | roadmap_id, status | →roadmaps |
| `study_sessions` | Auditable | — | `user_id`(NN), `roadmap_task_id`(null), started_at(ts,NN), ended_at(ts), duration_minutes(int), notes(text) | — | user_id, roadmap_task_id, started_at | →roadmap_tasks |
| `achievements` | Auditable | — | `user_id`(NN), `type`(enum,NN), title(varchar200,NN), description(varchar512), awarded_at(ts,NN), metadata(jsonb) | — | user_id, awarded_at | — |
| `notification_preferences` | Auditable | — | `user_id`(NN), email_enabled(bool,NN), push_enabled(bool,NN), weekly_digest(bool,NN), reminders_enabled(bool,NN), quiet_hours_start(time), quiet_hours_end(time) | `uk_notification_prefs_user`(user_id) | — | — |
| `dashboard_settings` | Auditable | — | `user_id`(NN), `theme`(enum,NN), default_view(varchar60), widgets(jsonb), layout(jsonb) | `uk_dashboard_settings_user`(user_id) | — | — |

**Enums:** `experience_level` (STUDENT, JUNIOR, MID, SENIOR), skill `category` (LANGUAGE, FRAMEWORK,
TOOL, CONCEPT, SOFT_SKILL), `proficiency` (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT), goal
`category` (CAREER, SKILL, INTERVIEW, CERTIFICATION), `status` (NOT_STARTED, IN_PROGRESS, COMPLETED,
ARCHIVED), achievement `type` (BADGE, MILESTONE, STREAK), `theme` (LIGHT, DARK, SYSTEM).

## Relationships & JPA

```
user_profiles 1───1 (logical: user_id → auth.users)
user_profiles 1───* user_skills *───1 skills          // join WITH attributes ⇒ entity, not @ManyToMany
user_profiles 1───* goals 1───* roadmaps 1───* roadmap_tasks 1───* study_sessions
user (user_id) 1───* achievements
user (user_id) 1───1 notification_preferences          // unique(user_id)
user (user_id) 1───1 dashboard_settings                // unique(user_id)
```
- `@ManyToOne(fetch = LAZY, optional = false)` on every child → parent (`user_skills→user_profiles/
  skills`, `goals→user_profiles`, `roadmaps→goals`, `roadmap_tasks→roadmaps`, `study_sessions→
  roadmap_tasks`). `study_sessions.roadmap_task_id` is nullable (ad-hoc study allowed).
- Parent→child `@OneToMany(mappedBy=…)` mapped **only** where the children are a bounded part of the
  aggregate and cascade-managed (e.g. `roadmap → roadmap_tasks` with `cascade = ALL, orphanRemoval`).
  Unbounded history collections (`study_sessions`, `achievements`) are **not** mapped on the parent.
- `user_skills` is a first-class entity (carries proficiency/years/endorsed) — never `@ManyToMany`.

## DTOs

- Full `Create/Update/Response/Summary/Search`: **UserProfile, Goal, Roadmap, RoadmapTask, Skill**.
- `Create/Update/Response`: **UserSkill** (proficiency edits), **NotificationPreference**,
  **DashboardSettings** (these are 1-1 settings — `Update`+`Response`, no `Search`).
- `Response/Summary`: **Achievement** (system-awarded, read-only), **StudySession** (`Create`+
  `Response`+`Search` — users log sessions).

## Repositories (10) — contracts only

`UserProfileRepository` (`findByUserId`, `existsByUserId`) · `SkillRepository` (`findByName`,
`findByCategory`) · `UserSkillRepository` (`findAllByUserProfileId`, `findByUserProfileIdAndSkillId`) ·
`GoalRepository` (`findAllByUserProfileId`, `findByUserProfileIdAndStatus`) · `RoadmapRepository`
(`findAllByGoalId`) · `RoadmapTaskRepository` (`findAllByRoadmapIdOrderByOrderIndex`) ·
`StudySessionRepository` (`findAllByUserId(Pageable)`, `findAllByRoadmapTaskId`) ·
`AchievementRepository` (`findAllByUserId`) · `NotificationPreferenceRepository` (`findByUserId`) ·
`DashboardSettingsRepository` (`findByUserId`). All `JpaRepository<E, UUID>`.

## API contracts (aggregate roots; controllers not implemented)

- **Profile** `/api/users/profile`: `GET`, `POST`, `PUT`, `PATCH` (self, from JWT subject).
- **Skills catalog** `/api/users/skills`: `GET`, `GET /{id}`, `POST` (admin), `POST /search`.
- **User skills** `/api/users/profile/skills`: `GET`, `POST`, `PUT /{id}`, `DELETE /{id}`.
- **Goals** `/api/users/goals`: `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `PATCH /{id}`, `DELETE /{id}`, `POST /search`.
- **Roadmaps** `/api/users/goals/{goalId}/roadmaps` + `/api/users/roadmaps/{id}`: `GET/POST/PUT/PATCH/DELETE`.
- **Roadmap tasks** `/api/users/roadmaps/{roadmapId}/tasks` + `/tasks/{id}`: `GET/POST/PUT/PATCH/DELETE`.
- **Study sessions** `/api/users/study-sessions`: `GET`, `POST`, `DELETE /{id}`, `POST /search`.
- **Achievements** `/api/users/achievements`: `GET` (read-only).
- **Settings** `/api/users/notification-preferences` & `/api/users/dashboard-settings`: `GET`, `PUT`.

## Key justifications

- **`user_skills` as an entity, not `@ManyToMany`** — the association carries attributes
  (proficiency, years, endorsed); modeling it explicitly keeps 3NF and lets it grow.
- **1-1 settings split into their own tables** (`notification_preferences`, `dashboard_settings`)
  rather than widening `user_profiles` — different change cadence, optional creation, and avoids a
  wide sparse profile row. `unique(user_id)` enforces the 1-1.
- **`jsonb` for `dashboard_settings.widgets/layout` and `achievements.metadata`** — schemaless,
  UI-driven, non-relational data; keeps the relational core normalized while allowing flexible blobs.
- **Aggregate boundaries:** `Roadmap`+`RoadmapTask` cascade as one aggregate; `Goal` is its own
  root; history tables are unmapped on parents to prevent unbounded fetches.
- **`order_index` on `roadmap_tasks`** — explicit user-defined ordering (never rely on insertion order).
