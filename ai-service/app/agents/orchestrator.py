"""Multi-agent orchestrator (LangGraph) — architecture placeholder.

The orchestrator will model the agents as nodes in a LangGraph `StateGraph`, routing a request
through the appropriate agent(s), sharing memory, and composing their outputs. For now it only
holds the agent registry so the wiring point and topology are explicit.

Planned graph (illustrative):

    entry ──▶ router ──┬─▶ resume_agent ─────┐
                       ├─▶ interview_agent ──┤
                       ├─▶ roadmap_agent ────┼─▶ aggregator ──▶ end
                       ├─▶ rag_agent ────────┤
                       └─▶ career_planner ───┘

No graph is compiled and no model is invoked in this skeleton.
"""
from __future__ import annotations

from app.agents.base_agent import BaseAgent
from app.agents.career_planner_agent import CareerPlannerAgent
from app.agents.interview_agent import InterviewAgent
from app.agents.rag_agent import RagAgent
from app.agents.resume_agent import ResumeAgent
from app.agents.roadmap_agent import RoadmapAgent


class AgentOrchestrator:
    """Registry + (future) LangGraph coordinator for the agent fleet."""

    def __init__(self) -> None:
        self._agents: dict[str, BaseAgent] = {
            agent.name: agent
            for agent in (
                ResumeAgent(),
                InterviewAgent(),
                RoadmapAgent(),
                RagAgent(),
                CareerPlannerAgent(),
            )
        }

    @property
    def agents(self) -> dict[str, BaseAgent]:
        return self._agents

    def build_graph(self) -> None:
        """Compile the LangGraph StateGraph wiring the agents together. Deferred."""
        raise NotImplementedError("LangGraph orchestration is not implemented yet.")
