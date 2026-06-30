# AI Orchestrator Service — Domain Model & Database Design

Database: **`careeros_ai`**. Base package: `com.careeros.aiorchestrator`. Conventions per
[`00-overview-erd.md`](00-overview-erd.md).

> **This service stores orchestration METADATA only.** Embeddings and vector data live in the
> FastAPI `ai-service` vector store, **never** here. These tables capture *what was run, by whom,
> with which model, at what cost* — for observability, cost accounting, and conversation continuity.

## Entities (8)

| Table | Base | Soft-del | Key columns | Unique | Indexes | FKs |
|---|---|---|---|---|---|---|
| `conversations` | SoftDeletable | ✓ | `user_id`(NN), title(varchar200), `agent_type`(enum,NN), `status`(enum,NN), started_at(ts), last_message_at(ts) | — | user_id, status | — |
| `conversation_messages` | Auditable | — | `conversation_id`(NN), `role`(enum,NN), content(text), token_count(int), sequence(int,NN) | `uk_conversation_message_seq`(conversation_id,sequence) | conversation_id | →conversations |
| `agent_executions` | Auditable | — | `user_id`(NN), `conversation_id`(null), `agent_type`(enum,NN), `status`(enum,NN), model(varchar80), input(jsonb), output(jsonb), correlation_id(varchar64), started_at, completed_at, duration_ms(bigint), error(text) | — | user_id, status, correlation_id | →conversations |
| `tool_executions` | Auditable | — | `agent_execution_id`(NN), tool_name(varchar120,NN), `status`(enum,NN), input(jsonb), output(jsonb), started_at, completed_at, duration_ms(bigint), error(text) | — | agent_execution_id | →agent_executions |
| `token_usage` | Auditable | — | `agent_execution_id`(NN), prompt_tokens(int,NN), completion_tokens(int,NN), total_tokens(int,NN), model(varchar80,NN), `provider`(enum,NN), cost_amount(num19,6), currency(char3) | `uk_token_usage_execution`(agent_execution_id) | model | →agent_executions |
| `prompt_history` | Auditable | — | `user_id`(NN), `agent_execution_id`(null), `agent_type`(enum), prompt_template(varchar160), rendered_prompt(text), variables(jsonb) | — | user_id, agent_type | →agent_executions |
| `model_configurations` | SoftDeletable | ✓ | config_key(varchar120,NN), `provider`(enum,NN), model(varchar80,NN), temperature(num3,2), max_tokens(int), top_p(num3,2), system_prompt(text), `status`(enum,NN), metadata(jsonb) | `uk_model_config_key`(config_key) | status | — |
| `ai_request_logs` | Auditable | — | `user_id`(null), `agent_execution_id`(null), `direction`(enum,NN), endpoint(varchar200), http_status(int), latency_ms(int), request_id(varchar64), error(text) | — | request_id, created_at | — |

**Enums:** `agent_type`(RESUME,INTERVIEW,ROADMAP,RAG,CAREER_PLANNER), conversation/message `status`/
`role`(ACTIVE,ARCHIVED / SYSTEM,USER,ASSISTANT,TOOL), execution `status`(PENDING,RUNNING,SUCCEEDED,
FAILED,TIMEOUT), `provider`(OPENAI,ANTHROPIC,AZURE_OPENAI), config `status`(ACTIVE,DISABLED),
log `direction`(INBOUND,OUTBOUND).

## Relationships & JPA

```
conversations 1───* conversation_messages          (ordered by unique sequence)
agent_executions 1───* tool_executions
agent_executions 1───1 token_usage                 (unique FK)
agent_executions *───0..1 conversations            prompt_history *───0..1 agent_executions
model_configurations  (standalone reference)        ai_request_logs (standalone observability)
```
- `conversation_messages→conversations`, `tool_executions→agent_executions`,
  `token_usage→agent_executions` (1-1, `unique`), `agent_executions→conversations`(null),
  `prompt_history→agent_executions`(null) — all `@ManyToOne(LAZY)`.
- `correlation_id` / `request_id` carry the gateway's `X-Trace-Id`, tying AI work back to the
  originating HTTP request across services.

## DTOs

- Full `Create/Update/Response/Summary/Search`: **ModelConfiguration** (admin-tunable),
  **Conversation**.
- `Create/Response`: **ConversationMessage**.
- `Response/Summary/Search` (read-only observability): **AgentExecution**, **ToolExecution**,
  **TokenUsage**, **PromptHistory**, **AIRequestLog**.

## Repositories (8) — contracts only

`ConversationRepository` (`findAllByUserId`, `findByUserIdAndStatus`) ·
`ConversationMessageRepository` (`findAllByConversationIdOrderBySequence`) ·
`AgentExecutionRepository` (`findAllByUserId`, `findByCorrelationId`, `findByUserIdAndStatus`) ·
`ToolExecutionRepository` (`findAllByAgentExecutionId`) · `TokenUsageRepository`
(`findByAgentExecutionId`, `sumTotalTokensByModel`) · `PromptHistoryRepository`
(`findAllByUserIdAndAgentType`) · `ModelConfigurationRepository` (`findByConfigKey`,
`findAllByStatus`) · `AiRequestLogRepository` (`findByRequestId`, `findAllByUserId`).
All `JpaRepository<E, UUID>`.

## API contracts (controllers not implemented)

- **Conversations** `/api/ai/conversations` (+`/{id}/messages`): `GET/POST/PATCH/DELETE`, `POST /search`.
- **Agent executions** `/api/ai/executions`: `GET`, `GET /{id}`, `POST /search` (read-only history).
- **Token usage** `/api/ai/usage`: `GET` (self/admin aggregates).
- **Model configurations** `/api/ai/model-configurations`: admin CRUD (`GET/POST/PUT/PATCH/DELETE`).
- **Request logs** `/api/ai/request-logs`: `GET` (admin observability).

> The orchestrator's *primary* runtime job is forwarding requests to the FastAPI AI service (see
> `ai-orchestrator-service` RestClient/WebClient/Feign config). These tables/endpoints are the
> persistence + observability layer around that bridge.

## Key justifications

- **Metadata only, no embeddings** — vectors belong to the vector store in `ai-service`; duplicating
  them here would couple the orchestrator to the AI runtime and bloat the relational DB. This service
  records orchestration facts, not model internals.
- **`token_usage` 1-1 with `agent_execution`** — precise per-call cost accounting (`cost_amount
  NUMERIC(19,6)` for sub-cent precision) that rolls up to per-user/-model billing and quota.
- **`correlation_id`/`request_id` for end-to-end tracing** — the same id minted by the API gateway
  flows through to AI executions, making a single request traceable across gateway → orchestrator →
  FastAPI.
- **`model_configurations` as tunable reference data** — prompts/temperature/model per `config_key`
  (e.g. `resume.default`) live in the DB, so model behaviour is changed by config, not redeploys.
- **`conversation_messages.sequence` unique per conversation** — deterministic ordering independent
  of timestamps/inserts.
- **Append-only logs/executions** (`AuditableEntity`, not soft-delete) — observability records are
  immutable history; only `conversations` and `model_configurations` are soft-deletable.
