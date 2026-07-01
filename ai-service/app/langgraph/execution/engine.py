import time
from typing import Dict, Any, Optional
from langchain_core.messages import HumanMessage, AIMessage

from app.langgraph.graphs.builder import orchestrator_graph
from app.langgraph.state.graph_state import AgentState
from app.logging.logger import get_logger

logger = get_logger(__name__)

class ExecutionEngine:
    """
    The main entry point for executing requests through the Multi-Agent Platform.
    """
    
    @staticmethod
    async def run(
        query: str, 
        user_context: Optional[Dict[str, Any]] = None,
        thread_id: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Executes the LangGraph pipeline for a given query.
        """
        start_time = time.time()
        
        # 1. Initialize State
        initial_state = {
            "messages": [HumanMessage(content=query)],
            "next_agent": None,
            "user_context": user_context or {},
            "metadata": {}
        }
        
        # 2. Config for memory (Checkpointer routing)
        config = {"configurable": {"thread_id": thread_id or "default_thread"}}
        
        logger.info(f"Starting execution graph for thread {config['configurable']['thread_id']}")
        
        try:
            # 3. Execute Graph
            final_state = await orchestrator_graph.ainvoke(initial_state, config=config)
            
            # 4. Extract Response
            messages = final_state.get("messages", [])
            response_text = ""
            if messages:
                # Get the last AI message
                for msg in reversed(messages):
                    if isinstance(msg, AIMessage):
                        response_text = msg.content
                        break
            
            execution_time_ms = (time.time() - start_time) * 1000
            
            return {
                "status": "success",
                "response": response_text,
                "execution_time_ms": execution_time_ms,
                "metadata": final_state.get("metadata", {})
            }
            
        except Exception as e:
            logger.error("Graph execution failed", exc_info=True)
            execution_time_ms = (time.time() - start_time) * 1000
            
            return {
                "status": "error",
                "error": str(e),
                "execution_time_ms": execution_time_ms
            }
