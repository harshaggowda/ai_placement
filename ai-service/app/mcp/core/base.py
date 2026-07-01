from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
import time

from app.mcp.schemas.tool import ToolMetadata, ExecutionResult
from app.logging.logger import get_logger

logger = get_logger(__name__)

class BaseTool(ABC):
    """
    Abstract base class for all MCP tools.
    """
    
    def __init__(self, metadata: ToolMetadata):
        self.metadata = metadata
        
    @property
    def name(self) -> str:
        return self.metadata.name
        
    @property
    def requires_auth(self) -> bool:
        return self.metadata.requires_auth
        
    async def execute(self, params: Dict[str, Any], context: Optional[Dict[str, Any]] = None) -> ExecutionResult:
        """
        Execute the tool with the given parameters and context.
        Provides a wrapper around the abstract _run method for unified telemetry and error handling.
        """
        start_time = time.time()
        
        try:
            logger.info("Executing tool", tool_name=self.name, params=params)
            
            # Subclasses implement the actual logic
            result_data = await self._run(params, context)
            
            execution_time_ms = (time.time() - start_time) * 1000
            
            return ExecutionResult(
                status="success",
                data=result_data,
                execution_time_ms=execution_time_ms
            )
            
        except Exception as e:
            execution_time_ms = (time.time() - start_time) * 1000
            logger.error("Tool execution failed", tool_name=self.name, error=str(e), exc_info=True)
            
            return ExecutionResult(
                status="error",
                error=str(e),
                execution_time_ms=execution_time_ms
            )
            
    @abstractmethod
    async def _run(self, params: Dict[str, Any], context: Optional[Dict[str, Any]] = None) -> Any:
        """
        The concrete implementation of the tool logic.
        """
        pass
