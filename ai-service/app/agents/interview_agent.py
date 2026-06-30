"""Interview Agent — generates and evaluates mock interview questions and answers."""
from __future__ import annotations

from typing import Any

from app.agents.base_agent import BaseAgent


class InterviewAgent(BaseAgent):
    """Interview Agent — generates and evaluates mock interview questions and answers.

    Skeleton — capability not implemented yet.
    """

    name = "interview"
    description = "Interview Agent — generates and evaluates mock interview questions and answers."

    async def run(self, payload: dict[str, Any]) -> dict[str, Any]:  # noqa: D102
        raise NotImplementedError("InterviewAgent is a placeholder; AI logic is not implemented yet.")
