"""Common contract for all CareerOS agents.

Defines the minimal interface the orchestrator depends on. Concrete agents implement `run`;
the LangGraph orchestrator treats each agent as a node. No model calls happen here.
"""
from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any


class BaseAgent(ABC):
    """Abstract base class for a single-capability agent."""

    #: Stable identifier used by the orchestrator and for routing/telemetry.
    name: str = "base"
    #: Human-readable description of the agent's capability.
    description: str = "Abstract base agent."

    @abstractmethod
    async def run(self, payload: dict[str, Any]) -> dict[str, Any]:
        """Execute the agent against a validated request payload and return a structured result.

        Concrete implementations will build a LangChain chain / LangGraph subgraph here. Not
        implemented in the skeleton.
        """
        raise NotImplementedError
