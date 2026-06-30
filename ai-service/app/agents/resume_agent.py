"""Resume Agent — analyses and improves resumes; extracts structured profile data."""
from __future__ import annotations

from typing import Any

from app.agents.base_agent import BaseAgent


class ResumeAgent(BaseAgent):
    """Resume Agent — analyses and improves resumes; extracts structured profile data.

    Skeleton — capability not implemented yet.
    """

    name = "resume"
    description = "Resume Agent — analyses and improves resumes; extracts structured profile data."

    async def run(self, payload: dict[str, Any]) -> dict[str, Any]:  # noqa: D102
        raise NotImplementedError("ResumeAgent is a placeholder; AI logic is not implemented yet.")
