from typing import Dict, List, Optional
from app.mcp.core.base import BaseTool
from app.mcp.schemas.tool import ToolMetadata
from app.logging.logger import get_logger

logger = get_logger(__name__)

class ToolRegistry:
    """
    Singleton registry for all available MCP tools.
    """
    _instance = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ToolRegistry, cls).__new__(cls)
            cls._instance._tools = {}
        return cls._instance
        
    def __init__(self):
        # Prevent re-initialization if __init__ is called again
        if not hasattr(self, '_tools'):
            self._tools: Dict[str, BaseTool] = {}
            
    def register(self, tool: BaseTool) -> None:
        """Register a tool instance."""
        if tool.name in self._tools:
            logger.warning("Overwriting existing tool in registry", tool_name=tool.name)
        
        self._tools[tool.name] = tool
        logger.debug("Registered tool", tool_name=tool.name)
        
    def get_tool(self, name: str) -> Optional[BaseTool]:
        """Retrieve a tool by name."""
        return self._tools.get(name)
        
    def list_tools(self) -> List[ToolMetadata]:
        """List metadata for all registered tools."""
        return [tool.metadata for tool in self._tools.values()]
        
    def clear(self) -> None:
        """Clear the registry (useful for testing)."""
        self._tools.clear()

# Global registry instance
registry = ToolRegistry()
