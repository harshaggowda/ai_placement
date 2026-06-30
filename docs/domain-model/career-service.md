# Career Service — Domain Model & Database Design

Database: **`careeros_career`**. Base package (future): `com.careeros.career`. Conventions per
[`00-overview-erd.md`](00-overview-erd.md). User-owned rows carry a logical `user_id UUID`
(indexed, no cross-DB FK). Largest service: 18 entities.

## Entities (18)

| Table | Base | Soft-del | Key columns | Unique | Indexes | FKs |
|---|---|---|---|---|---|---|
| `resumes` | SoftDeletable | ✓ | `user_id`(NN), title(varchar160,NN), is_primary(bool,NN), `current_version_id`(uuid,null), `status`(enum,NN) | — | user_id, status | (current_version_id → resume_versions, deferred) |
| `resume_versions` | SoftDeletable | ✓ | `resume_id`(NN), version_number(int,NN), storage_key(varchar512,NN, S3), `format`(enum,NN), content(jsonb,null) | `uk_resume_version`(resume_id,version_number) | resume_id | →resumes |
| `resume_analyses` | Auditable | — | `resume_version_id`(NN), score(num5,2), ats_score(num5,2), strengths(jsonb), weaknesses(jsonb), suggestions(jsonb), analyzed_at(ts) | `uk_resume_analysis_version`(resume_version_id) | — | →resume_versions |
| `interviews` | SoftDeletable | ✓ | `user_id`(NN), `type`(enum,NN), role(varchar120), `company_id`(null), `status`(enum,NN), scheduled_at, started_at, completed_at, overall_score(num5,2,null) | — | user_id, status, company_id | →companies |
| `interview_questions` | Auditable | — | `interview_id`(NN), prompt(text,NN), `category`(enum), `difficulty`(enum), order_index(int,NN), expected_points(jsonb) | — | interview_id | →interviews |
| `interview_answers` | Auditable | — | `interview_question_id`(NN), answer_text(text), audio_key(varchar512,null), duration_seconds(int), submitted_at(ts) | `uk_interview_answer_question`(interview_question_id) | — | →interview_questions |
| `interview_feedback` | Auditable | — | `interview_id`(NN), summary(text), strengths(jsonb), improvements(jsonb), communication_score(num5,2), technical_score(num5,2), `recommendation`(enum) | `uk_interview_feedback`(interview_id) | — | →interviews |
| `companies` | SoftDeletable | ✓ | name(varchar160,NN), website(varchar255), industry(varchar120), `size`(enum), headquarters(varchar160), logo_url(varchar512) | `uk_companies_name`(name) | industry | — |
| `applications` | SoftDeletable | ✓ | `user_id`(NN), `company_id`(NN), role_title(varchar160,NN), `status`(enum,NN), source(varchar120), applied_at(date), job_url(varchar512), salary_min/max(num19,4), currency(char3), notes(text) | — | user_id, company_id, status | →companies |
| `company_preparations` | SoftDeletable | ✓ | `user_id`(NN), `company_id`(NN), target_role(varchar120), `status`(enum), notes(text), prep_checklist(jsonb) | `uk_company_prep`(user_id,company_id) | user_id | →companies |
| `leetcode_progress` | Auditable | — | `user_id`(NN), problem_slug(varchar160,NN), title(varchar200), `difficulty`(enum), `status`(enum,NN), language(varchar40), attempts(int,NN), last_attempted_at, solved_at | `uk_leetcode_user_problem`(user_id,problem_slug) | user_id, status | — |
| `sql_progress` | Auditable | — | `user_id`(NN), problem_slug(varchar160,NN), title(varchar200), `difficulty`(enum), `status`(enum,NN), platform(varchar60), last_attempted_at, solved_at | `uk_sql_user_problem`(user_id,problem_slug) | user_id, status | — |
| `projects` | SoftDeletable | ✓ | `user_id`(NN), title(varchar160,NN), description(text), repo_url, demo_url, tech_stack(jsonb), `status`(enum), started_on(date), completed_on(date) | — | user_id | — |
| `project_reviews` | Auditable | — | `project_id`(NN), `reviewer`(enum), score(num5,2), feedback(text), strengths(jsonb), improvements(jsonb), reviewed_at(ts) | — | project_id | →projects |
| `certificates` | SoftDeletable | ✓ | `user_id`(NN), name(varchar200,NN), issuer(varchar160), issued_on(date), expires_on(date,null), credential_id(varchar160), credential_url(varchar512) | — | user_id | — |
| `learning_resources` | SoftDeletable | ✓ | `user_id`(null=global), title(varchar200,NN), `type`(enum,NN), url(varchar512), provider(varchar120), topic(varchar120), `status`(enum), rating(int,null) | — | user_id, topic | — |
| `daily_plans` | SoftDeletable | ✓ | `user_id`(NN), plan_date(date,NN), focus(varchar200), items(jsonb), completed(bool,NN) | `uk_daily_plan_user_date`(user_id,plan_date) | user_id, plan_date | — |
| `progress_snapshots` | Auditable | — | `user_id`(NN), snapshot_date(date,NN), leetcode_solved(int), sql_solved(int), applications_count(int), interviews_count(int), study_minutes(int), metrics(jsonb) | `uk_progress_snapshot`(user_id,snapshot_date) | user_id, snapshot_date | — |

**Enums:** resume `status`(DRAFT,ACTIVE,ARCHIVED), `format`(PDF,DOCX,JSON); interview `type`
(BEHAVIORAL,TECHNICAL,SYSTEM_DESIGN,MIXED), `status`(SCHEDULED,IN_PROGRESS,COMPLETED,ABANDONED),
question `category`+`difficulty`(EASY,MEDIUM,HARD), `recommendation`(STRONG_HIRE,HIRE,NO_HIRE,
STRONG_NO_HIRE); company `size`(STARTUP,SMB,MID,ENTERPRISE); application `status`(SAVED,APPLIED,
SCREEN,INTERVIEW,OFFER,REJECTED,ACCEPTED); progress `status`(TODO,ATTEMPTED,SOLVED); review
`reviewer`(AI,PEER,MENTOR); resource `type`(COURSE,BOOK,ARTICLE,VIDEO,OTHER).

## Relationships & JPA

```
resumes 1───* resume_versions 1───1 resume_analyses     // each version analyzed once
interviews 1───* interview_questions 1───1 interview_answers
interviews 1───1 interview_feedback
companies 1───* applications        companies 1───* company_preparations
projects 1───* project_reviews
user-scoped (by user_id): leetcode_progress, sql_progress, certificates,
                          learning_resources, daily_plans, progress_snapshots
```
- All children `@ManyToOne(LAZY, optional=false)` to parents (`interview_id`, `resume_id`,
  `project_id`, `company_id`). `interviews.company_id` and `learning_resources.user_id` are nullable.
- Aggregates with cascade/orphanRemoval: `Resume→ResumeVersion`, `Interview→InterviewQuestion→
  InterviewAnswer`. `ResumeAnalysis`/`InterviewFeedback` are 1-1 children (`unique` FK).
- `companies` is a **shared catalog** (not user-owned); `applications`/`company_preparations`
  reference it `@ManyToOne`. Progress/certificate/snapshot tables are flat user-owned records.

## DTOs

- Full `Create/Update/Response/Summary/Search`: **Resume, Interview, Application, Company, Project,
  Certificate, LearningResource, DailyPlan, LeetCodeProgress, SQLProgress**.
- `Create/Response` (+`Summary`): **ResumeVersion** (immutable once created → no `Update`),
  **InterviewQuestion**, **InterviewAnswer**, **CompanyPreparation**.
- `Response` only (system-generated, read-only): **ResumeAnalysis, InterviewFeedback,
  ProjectReview, ProgressSnapshot**.

## Repositories (18) — contracts only (selected derived queries)

`ResumeRepository` (`findAllByUserId`, `findByUserIdAndIsPrimaryTrue`) · `ResumeVersionRepository`
(`findAllByResumeIdOrderByVersionNumberDesc`, `findByResumeIdAndVersionNumber`) ·
`ResumeAnalysisRepository` (`findByResumeVersionId`) · `InterviewRepository` (`findAllByUserId`,
`findByUserIdAndStatus`) · `InterviewQuestionRepository` (`findAllByInterviewIdOrderByOrderIndex`) ·
`InterviewAnswerRepository` (`findByInterviewQuestionId`) · `InterviewFeedbackRepository`
(`findByInterviewId`) · `CompanyRepository` (`findByName`, `findByIndustry`) ·
`ApplicationRepository` (`findAllByUserId`, `findByUserIdAndStatus`, `countByUserIdAndStatus`) ·
`CompanyPreparationRepository` (`findByUserIdAndCompanyId`) · `LeetCodeProgressRepository`
(`findByUserIdAndProblemSlug`, `countByUserIdAndStatus`) · `SqlProgressRepository`
(`findByUserIdAndProblemSlug`) · `ProjectRepository` (`findAllByUserId`) · `ProjectReviewRepository`
(`findAllByProjectId`) · `CertificateRepository` (`findAllByUserId`) · `LearningResourceRepository`
(`findAllByUserIdOrUserIdIsNull`, `findByTopic`) · `DailyPlanRepository` (`findByUserIdAndPlanDate`) ·
`ProgressSnapshotRepository` (`findByUserIdAndSnapshotDate`, `findTop30ByUserIdOrderBySnapshotDateDesc`).

## API contracts (aggregate roots; controllers not implemented)

- **Resumes** `/api/career/resumes` (+`/{id}`, `/{id}/versions`, `/versions/{vid}/analysis`):
  `GET/POST/PUT/PATCH/DELETE`, `POST /search`.
- **Interviews** `/api/career/interviews` (+`/{id}/questions`, `/questions/{qid}/answer`,
  `/{id}/feedback`): `GET/POST/PATCH/DELETE`, `POST /search`.
- **Applications** `/api/career/applications`: full CRUD + `POST /search`.
- **Companies** `/api/career/companies` (catalog): `GET`, `GET /{id}`, `POST` (admin), `POST /search`.
- **Company prep** `/api/career/company-preparations`: `GET/POST/PUT/DELETE`.
- **LeetCode / SQL** `/api/career/leetcode-progress`, `/api/career/sql-progress`:
  `GET`, `PUT` (upsert by slug), `POST /search`.
- **Projects** `/api/career/projects` (+`/{id}/reviews`): `GET/POST/PUT/PATCH/DELETE`.
- **Certificates** `/api/career/certificates`: full CRUD.
- **Learning resources** `/api/career/learning-resources`: `GET/POST/PUT/DELETE`, `POST /search`.
- **Daily plans** `/api/career/daily-plans`: `GET`, `GET /{date}`, `PUT /{date}` (upsert).
- **Progress snapshots** `/api/career/progress-snapshots`: `GET` (read-only, range query).

## Key justifications

- **Immutable resume versioning** — `resume_versions` are append-only (`version_number` unique per
  resume) with the binary in S3 (`storage_key`) and optional parsed `content` jsonb; `resumes.
  current_version_id` points at the live one. Gives full history, diffing, and cheap rollback
  without mutating files. Hence `ResumeVersion` has no `Update` DTO.
- **1-1 analysis/feedback via `unique` FK** — `resume_analyses`/`interview_feedback` are separate
  tables (different lifecycle, AI-generated later) joined `unique` to their parent, not inlined.
- **`unique(user_id, problem_slug)` on progress** — makes LeetCode/SQL tracking an idempotent upsert
  keyed by problem; `PUT` semantics, no duplicates.
- **`companies` as a shared catalog** — deduplicates company data across users' applications and prep;
  `applications`/`company_preparations` reference it rather than copying name/industry (3NF).
- **`progress_snapshots` is intentional, controlled denormalization** — a daily read-model
  (`unique(user_id, snapshot_date)`) precomputing dashboard counters so trend charts don't aggregate
  across six tables on every load. Documented as derived data, rebuildable from sources.
- **`jsonb` for AI/blobs** (strengths, suggestions, tech_stack, prep_checklist, metrics) — flexible,
  evolving, non-relational payloads; relational core stays normalized.
