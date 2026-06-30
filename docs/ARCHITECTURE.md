# CareerOS AI — Architecture

A production-oriented, microservices SaaS platform for software-engineering placement
preparation. This document is the architectural reference for the **skeleton** stage: the
structure, boundaries, and contracts are in place; business logic, authentication, and AI
inference are intentionally **not** implemented yet.

> Scope rule for this stage: **architecture over implementation.** Every service compiles and
> boots, exposes its boundaries, and declares its configuration — but does no domain work yet.

---

## 1. Folder structure

```
placement_proj/
├── pom.xml                         # Maven aggregator + dependency mgmt for all JVM modules
├── .env.example                    # Compose environment template (copy to infrastructure/docker/.env)
│
├── shared-libraries/               # Shared JVM libraries (NOT services) — 4 independent modules
│   ├── common/                     # careeros-common: entity bases, ApiResponse/ErrorResponse,
│   │                               #   pagination, utils, auditing, OpenAPI/Redis/JPA config,
│   │                               #   application-common.yml. No internal deps.
│   ├── logging/                    # careeros-logging: CorrelationIdFilter + logback-spring.xml
│   ├── exceptions/                 # careeros-exceptions: ErrorCode, GlobalExceptionHandler,
│   │                               #   business exceptions  → depends on common, logging
│   └── security/                   # careeros-security: JWT filter/props, SecurityConfig, CORS
│                                   #   → depends on common, exceptions, logging
│                                   #   (Java packages stay com.careeros.{common,logging,exception,security})
│
├── api-gateway/                    # Public edge — routing only (Spring Cloud Gateway, reactive) :8080
├── auth-service/                   # Login/Register/JWT/RBAC          → DB careeros_auth   :8081
├── user-service/                   # Profile/Progress/Goals/Roadmaps  → DB careeros_user   :8082
├── career-service/                 # Resume/Interview/Analytics/LC/SQL→ DB careeros_career :8083
├── payment-service/                # Stripe/Subscriptions/Webhooks    → DB careeros_payment:8084
├── ai-orchestrator-service/             # Bridge to FastAPI (no DB, no AI logic)               :8085
│       └── src/main/java/com/careeros/<svc>/
│           ├── config/  controller/  dto/  entity/
│           └── mapper/  repository/  service/  util/      # MapStruct + layered structure
│
├── ai-service/                     # Independent Python service (NOT a Maven module)      :8000
│   ├── Dockerfile  requirements.txt  .env.example  README.md
│   └── app/
│       ├── main.py                 # FastAPI app — health endpoint only today
│       ├── config/                 # pydantic-settings (AI_ prefix)
│       ├── routers/                # HTTP boundary (health wired; agent routers later)
│       ├── agents/                 # base_agent + Resume/Interview/Roadmap/RAG/CareerPlanner + orchestrator
│       ├── rag/                    # KnowledgeCollection taxonomy + vector_store interface
│       ├── mcp/                    # MCP tool placeholders (web/github/leetcode/postgres/calendar/resume)
│       ├── chains/ memory/ prompts/ embeddings/ models/ services/ tools/ utils/
│       └── (no embeddings / no model calls yet)
│
├── frontend/                       # React + TS + Vite + Tailwind scaffold               :5173
│   ├── package.json  vite.config.ts  tsconfig*.json  tailwind.config.js  Dockerfile
│   └── src/
│       ├── main.tsx  App.tsx  index.css
│       ├── lib/apiClient.ts        # single transport boundary → API Gateway
│       ├── types/index.ts          # ApiResponse/PaginationResponse envelopes
│       ├── components/  pages/      # added as features land
│
├── infrastructure/
│   ├── docker/
│   │   ├── docker-compose.yml      # full local stack (services + 4 Postgres + Redis + pgAdmin)
│   │   └── pgadmin/servers.json    # pre-registered DB connections
│   ├── nginx/nginx.conf            # reverse-proxy placeholder (not yet wired into compose)
│   └── scripts/                    # build.sh, up.sh, down.sh helpers
│
├── docs/
│   └── ARCHITECTURE.md             # this document
│
└── .github/workflows/              # CI: backend-ci, ai-service-ci, frontend-ci
```

---

## 2. Architecture explanation

**Style:** microservices with a single public **API Gateway** as the only internet-facing port.
Everything behind it is on a private network and addressed by service name.

Key principles:

- **Database-per-service.** Each stateful service owns a dedicated PostgreSQL instance
  (`careeros_auth`, `careeros_user`, `careeros_career`, `careeros_payment`). No service reads
  another's tables — data is exchanged only through APIs. This keeps schemas independently
  evolvable and lets each service scale and deploy on its own.
- **Redis is cache-only**, shared across services. It is never a system of record, so sharing it
  does not couple service data ownership.
- **A polyglot AI boundary.** The Java ecosystem never imports AI libraries. The
  `ai-orchestrator-service` (Spring) is a thin, resilient bridge — auth forwarding, validation,
  timeouts, retries, logging — to the **`ai-service`** (Python/FastAPI), where all agents, RAG,
  and MCP live. This isolates the fast-moving AI stack from the stable business stack and lets
  each be deployed, scaled, and secured independently.
- **Shared libraries, not shared runtime.** Cross-cutting concerns live in four focused JVM
  modules under `shared-libraries/` — `common` (error envelope, entities, OpenAPI/cache config,
  auditing), `logging` (correlation-id filter), `exceptions` (global handler), and `security`
  (JWT plumbing, CORS, security baseline). Services depend only on the modules they need, staying
  consistent without a shared database or runtime. The reactive `api-gateway` depends on none.
- **Config is 12-factor.** Every external dependency (DB URL, Redis host, JWT secret, AI URL,
  Stripe keys) is environment-overridable, so the same image runs locally and on AWS.

---

## 3. Service responsibilities

| Service | Port | Owns DB | Responsibilities | Explicitly NOT responsible for |
|---|---|---|---|---|
| **api-gateway** | 8080 | — | Single public entry point; static routing to downstream services; (future) edge JWT validation + Redis rate limiting | Business logic; persistence |
| **auth-service** | 8081 | `careeros_auth` | Register, login, JWT issue/refresh, RBAC, (future) OAuth | User profile data; payments |
| **user-service** | 8082 | `careeros_user` | Profile, resume **metadata**, progress, goals, roadmaps, dashboard data | Resume **files**; auth tokens |
| **career-service** | 8083 | `careeros_career` | Resume upload, interview history, progress/company tracking, analytics, LeetCode & SQL progress | Identity; billing |
| **payment-service** | 8084 | `careeros_payment` | Stripe subscriptions, billing, webhook processing, premium plans | Feature gating logic elsewhere |
| **ai-orchestrator-service** | 8085 | — | Bridge to FastAPI: call, auth-forward, retry, timeout, validate, log | **Any AI logic** |
| **ai-service** (Python) | 8000 | — | Agents (Resume/Interview/Roadmap/RAG/Career Planner), LangGraph orchestration, RAG, MCP tools | Being publicly reachable; business persistence |
| **frontend** | 5173 | — | React SPA; talks only to the API Gateway | Direct service/DB access |

Supporting infra: **Redis** (shared cache), **4× PostgreSQL** (one per stateful service),
**pgAdmin** (DB admin UI).

---

## 4. Communication diagram

```
                                 ┌───────────────┐
                                 │    Browser    │
                                 │  React (5173) │
                                 └───────┬───────┘
                                         │ HTTPS  (only public origin)
                                         ▼
                                ┌──────────────────┐
                                │   API Gateway    │  :8080
                                │  routing + (edge │
                                │   auth, future)  │
                                └───┬───┬───┬───┬──┘
              ┌─────────────────────┘   │   │   └─────────────────────┐
              ▼                         ▼   ▼                         ▼
      ┌──────────────┐        ┌──────────────┐ ┌──────────────┐  ┌──────────────────┐
      │ auth-service │        │ user-service │ │career-service│  │ payment-service  │
      │    :8081     │        │    :8082     │ │    :8083     │  │     :8084        │
      └──────┬───────┘        └──────┬───────┘ └──────┬───────┘  └────────┬─────────┘
             │                       │                │                   │
        ┌────▼─────┐           ┌─────▼────┐     ┌─────▼────┐        ┌──────▼─────┐
        │ pg auth  │           │ pg user  │     │ pg career│        │ pg payment │
        │  :5433   │           │  :5434   │     │  :5435   │        │   :5436    │
        └──────────┘           └──────────┘     └──────────┘        └──────┬─────┘
                                                                           │ webhooks
                                         ┌──────────────────┐         ┌────▼────┐
                          /api/ai/** ───▶│ ai-orch-svc   │         │ Stripe  │
                                         │     :8085        │         └─────────┘
                                         │ (bridge: retry,  │
                                         │  timeout, auth)  │
                                         └────────┬─────────┘
                                                  │ REST (private)
                                                  ▼
                                         ┌──────────────────┐
                                         │   ai-service     │  :8000
                                         │ FastAPI agents / │
                                         │  LangGraph / RAG │
                                         │  / MCP (Python)  │
                                         └──────────────────┘

   Shared cache (NOT a system of record):  Redis :6379  ◀── auth, user, career, payment
   Admin UI:  pgAdmin :5050  ──▶ all four PostgreSQL instances

   Legend:  ▶ synchronous REST.  All inter-service calls are REST in v1; the gateway is the
   only port exposed to the public internet. Service-to-service traffic stays on careeros-net.
```

**Request flow example (`POST /api/ai/resume/review`):**
`Browser → API Gateway (/api/ai/** → ai-orchestrator-service) → ai-orchestrator-service (validate, attach
service key, set timeout/retry) → ai-service (FastAPI) → orchestrator → ResumeAgent → response
back up the chain.`

---

## 5. Suggested development order

1. **Auth-service first** — identity is the dependency of everything else. Implement
   register/login/JWT issue+refresh and RBAC; expose token verification.
2. **API Gateway edge auth** — add JWT validation + correlation-id propagation at the edge so
   downstream services can trust the gateway.
3. **User-service** — profiles, goals, roadmaps, dashboard. First "real" domain data; proves the
   database-per-service + gateway pattern end-to-end.
4. **Career-service** — resume upload (S3), interview history, progress/analytics, LeetCode/SQL
   tracking. Largest domain surface.
5. **ai-service (FastAPI)** — implement one agent end-to-end (Resume Agent), then the LangGraph
   orchestrator, then RAG ingestion/retrieval, then MCP tools incrementally.
6. **ai-orchestrator-service** — flesh out the `AiServiceClient` (retries, timeouts, error mapping)
   against the now-real FastAPI endpoints.
7. **Payment-service** — Stripe subscriptions + webhook processing + premium plan gating.
8. **Frontend** — build pages/components against the now-stable gateway APIs.
9. **Hardening** — CI/CD to AWS, observability (CloudWatch/Prometheus), secrets management,
   rate limiting.

Rationale: build along the dependency graph (identity → domain data → AI → payments → UI) so
each layer is exercised by the next as it lands.

---

## 6. Future scalability recommendations

- **Async messaging (RabbitMQ/Kafka).** v1 uses synchronous REST. The seams are already drawn
  (each service owns its data; calls cross explicit boundaries). Introduce an event bus for
  cross-service workflows — e.g. payment-service publishes `SubscriptionActivated`, consumed by
  user-service for feature gating — without rewriting the synchronous paths. Start with Stripe
  webhooks and analytics events, which are naturally event-shaped.
- **Service discovery + config server.** Replace static gateway URIs with Eureka/Consul and a
  centralized config server (Spring Cloud Config) so routing and config aren't baked into images.
- **Edge concerns at the gateway.** Add Redis-backed rate limiting, request quotas per plan, and
  centralized JWT validation so downstream services trust a single edge.
- **Resilience.** Wrap the AI bridge (and other cross-service calls) in circuit breakers
  (Resilience4j) with bulkheads and fallbacks; the AI path is the slowest and most failure-prone.
- **AI scaling.** Run `ai-service` as its own horizontally-scaled pool (GPU/CPU separate from JVM
  services); add a queue for long-running agent jobs; cache RAG retrievals; consider streaming
  responses (SSE/WebSocket) through the gateway.
- **Data layer.** Add read replicas per service DB as read load grows; introduce a dedicated
  vector database (pgvector/Qdrant/Pinecone) for RAG rather than co-locating with relational data.
- **Cloud topology (AWS).** EC2/ECS or EKS for services, RDS per service DB, ElastiCache for
  Redis, S3 for resume files, CloudWatch for logs/metrics, IAM least-privilege roles per service,
  secrets in SSM/Secrets Manager. Per-service CI/CD pipelines via GitHub Actions.
- **Observability.** Standardize on the existing correlation-id filter; export Prometheus metrics
  (already enabled in `application-common.yml`) and ship traces (OpenTelemetry) to a collector.
