import json
from typing import Dict, Any
from langchain_core.messages import AIMessage

from app.langgraph.agents.base_agent import BaseAgent
from app.langgraph.state.graph_state import AgentState
from app.mcp.execution.executor import ToolExecutor
from app.logging.logger import get_logger

logger = get_logger(__name__)

class MCPToolAgent(BaseAgent):
    @property
    def name(self) -> str:
        return "mcp_agent"
        
    @property
    def description(self) -> str:
        return "Executes external tools like database queries, GitHub actions, and service APIs."
        
    @property
    def capabilities(self) -> list[str]:
        return ["tool_execution", "external_api"]
        
    async def invoke(self, state: AgentState) -> Dict[str, Any]:
        logger.info("Executing MCP Tool Agent")
        
        # In a real LangGraph flow, this agent would be invoked with a ToolCall message.
        # We simulate the extraction of tool arguments for orchestration purposes.
        tool_name = "mock_tool"
        params = {}
        
        try:
            # Delegate to Phase 4 MCP Framework
            result = await ToolExecutor.execute(
                tool_name=tool_name,
                params=params,
                context=state.get("user_context")
            )
            response_text = json.dumps(result.model_dump())
        except Exception as e:
            logger.error("MCP Tool execution failed", exc_info=True)
            response_text = f"Tool execution error: {str(e)}"
            
        response_msg = AIMessage(content=f"[MCP Output] {response_text}")
        
        return {
            "messages": [response_msg],
            "metadata": {"last_agent_executed": self.name}
        }
