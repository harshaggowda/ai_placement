from abc import ABC, abstractmethod
from typing import Dict, Any, List

from app.langgraph.state.graph_state import AgentState

class BaseAgent(ABC):
    """
    Abstract interface for all specialized agents in the LangGraph multi-agent system.
    """
    
    @property
    @abstractmethod
    def name(self) -> str:
        """The unique name of the agent (e.g., 'resume_agent')."""
        pass
        
    @property
    @abstractmethod
    def description(self) -> str:
        """A description of what the agent does, used by the Supervisor for routing."""
        pass
        
    @property
    def capabilities(self) -> List[str]:
        """A list of capabilities this agent possesses."""
        return []

    @abstractmethod
    async def invoke(self, state: AgentState) -> Dict[str, Any]:
        """
        Executes the agent's logic given the current state.
        Returns a dictionary containing the updates to the state (e.g., {"messages": [...]}).
        """
        pass
