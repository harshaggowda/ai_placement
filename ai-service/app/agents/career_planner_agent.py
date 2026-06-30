"""Career Planner Agent — long-horizon career planning and goal decomposition."""
from __future__ import annotations

from typing import Any

from app.agents.base_agent import BaseAgent


class CareerPlannerAgent(BaseAgent):
    """Career Planner Agent — long-horizon career planning and goal decomposition.

    Skeleton — capability not implemented yet.
    """

    name = "career_planner"
    description = "Career Planner Agent — long-horizon career planning and goal decomposition."

    async def run(self, payload: dict[str, Any]) -> dict[str, Any]:  # noqa: D102
        raise NotImplementedError("CareerPlannerAgent is a placeholder; AI logic is not implemented yet.")
