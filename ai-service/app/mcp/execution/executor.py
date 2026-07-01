from typing import Dict, Any, Optional
import time

from app.mcp.registry.tool_registry import registry
from app.mcp.schemas.tool import ExecutionResult, ErrorResponse
from app.logging.logger import get_logger

logger = get_logger(__name__)

class ToolExecutor:
    """
    Handles the execution pipeline for MCP tools.
    Responsible for validation, security, and orchestrating execution.
    """
    
    @staticmethod
    async def execute(
        tool_name: str, 
        params: Dict[str, Any], 
        context: Optional[Dict[str, Any]] = None
    ) -> ExecutionResult:
        """
        Execute a tool by name, applying the full pipeline.
        """
        start_time = time.time()
        
        # 1. Tool Selection
        tool = registry.get_tool(tool_name)
        if not tool:
            logger.error("Tool not found", tool_name=tool_name)
            return ExecutionResult(
                status="error",
                error=f"Tool '{tool_name}' not found in registry",
                execution_time_ms=(time.time() - start_time) * 1000
            )
            
        # 2. Security Validation
        if tool.requires_auth:
            if not context or not context.get("user_id"):
                logger.error("Tool requires authentication, but no user context provided", tool_name=tool_name)
                return ExecutionResult(
                    status="error",
                    error="Authentication required for this tool",
                    execution_time_ms=(time.time() - start_time) * 1000
                )
                
        # 3. Execution (which handles its own internal telemetry and error catching)
        logger.debug("Executing tool via pipeline", tool_name=tool_name)
        result = await tool.execute(params, context)
        
        return result
