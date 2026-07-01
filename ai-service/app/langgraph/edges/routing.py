from typing import Literal

from langgraph.graph import END
from app.langgraph.state.graph_state import AgentState
from app.logging.logger import get_logger

logger = get_logger(__name__)

def supervisor_router(state: AgentState) -> str:
    """
    Reads the 'next_agent' key from the state and determines the next node to execute.
    If 'FINISH' is specified, it routes to END.
    """
    next_agent = state.get("next_agent")
    
    if next_agent == "FINISH" or not next_agent:
        logger.debug("Supervisor routing to END")
        return END
        
    logger.debug(f"Supervisor routing to {next_agent}")
    return next_agent
