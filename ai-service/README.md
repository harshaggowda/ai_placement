# CareerOS AI Service

FastAPI multi-agent service (LangChain + LangGraph + MCP + RAG). **Architecture skeleton** —
the HTTP boundary, settings, package layout, and agent/RAG/MCP contracts exist; no model calls,
embeddings, or tools are implemented yet.

## Layout

```
app/
  agents/      Agent contracts + the LangGraph orchestrator (Resume, Interview, Roadmap, RAG, Career Planner)
  rag/         Knowledge-collection taxonomy and vector-store interface (no embeddings yet)
  mcp/         Model Context Protocol tool placeholders (web search, GitHub, LeetCode, Postgres, calendar, resume)
  chains/      LangChain composition (placeholder)
  memory/      Conversation / agent memory (placeholder)
  prompts/     Prompt templates (placeholder)
  embeddings/  Embedding providers (placeholder)
  models/      Pydantic request/response schemas (placeholder)
  services/    Application services invoked by routers (placeholder)
  routers/     FastAPI routers — only `health` is wired today
  config/      Typed settings (pydantic-settings)
  utils/       Cross-cutting helpers (placeholder)
```

## Run locally

```bash
cp .env.example .env
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
# OpenAPI docs at http://localhost:8000/docs
```

## Run via Docker

Built and orchestrated by `infrastructure/docker/docker-compose.yml` as the `ai-service` container.
It is reached only by the `ai-orchestrator-service`, never directly by the public API gateway.
