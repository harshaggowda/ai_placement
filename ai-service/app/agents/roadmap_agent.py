"""Roadmap Agent — builds personalized learning and career roadmaps."""
from __future__ import annotations

from typing import Any

from app.agents.base_agent import BaseAgent


class RoadmapAgent(BaseAgent):
    """Roadmap Agent — builds personalized learning and career roadmaps.

    Skeleton — capability not implemented yet.
    """

    name = "roadmap"
    description = "Roadmap Agent — builds personalized learning and career roadmaps."

    async def run(self, payload: dict[str, Any]) -> dict[str, Any]:  # noqa: D102
        raise NotImplementedError("RoadmapAgent is a placeholder; AI logic is not implemented yet.")
