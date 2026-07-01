import operator
from typing import TypedDict, Annotated, List, Dict, Any, Optional
from langchain_core.messages import BaseMessage

class AgentState(TypedDict):
    """
    State representing the context within the LangGraph execution.
    Attributes:
        messages: List of messages. We use `operator.add` to append new messages automatically.
        next_agent: The name of the next agent to execute, determined by the supervisor.
        user_context: Context specific to the user (e.g., user_id, preferences).
        metadata: Execution metadata (e.g., latency, token_usage).
    """
    messages: Annotated[List[BaseMessage], operator.add]
    next_agent: Optional[str]
    user_context: Dict[str, Any]
    metadata: Dict[str, Any]
