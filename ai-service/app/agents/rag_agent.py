"""RAG Agent — answers questions grounded in the knowledge collections."""
from __future__ import annotations

from typing import Any

from app.agents.base_agent import BaseAgent


class RagAgent(BaseAgent):
    """RAG Agent — answers questions grounded in the knowledge collections.

    Skeleton — capability not implemented yet.
    """

    name = "rag"
    description = "RAG Agent — answers questions grounded in the knowledge collections."

    async def run(self, payload: dict[str, Any]) -> dict[str, Any]:  # noqa: D102
        raise NotImplementedError("RagAgent is a placeholder; AI logic is not implemented yet.")
