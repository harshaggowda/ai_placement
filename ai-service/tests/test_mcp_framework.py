import pytest
from typing import Dict, Any, Optional

from app.mcp.core.base import BaseTool
from app.mcp.schemas.tool import ToolMetadata, ToolInputSchema
from app.mcp.registry.tool_registry import ToolRegistry
from app.mcp.execution.executor import ToolExecutor

class MockTool(BaseTool):
    def __init__(self):
        super().__init__(
            metadata=ToolMetadata(
                name="mock_tool",
                description="A mock tool for testing.",
                input_schema=ToolInputSchema(
                    properties={"echo": {"type": "string"}},
                    required=["echo"]
                )
            )
        )
        
    async def _run(self, params: Dict[str, Any], context: Optional[Dict[str, Any]] = None) -> Any:
        return {"echoed": params.get("echo")}

class MockAuthTool(BaseTool):
    def __init__(self):
        super().__init__(
            metadata=ToolMetadata(
                name="mock_auth_tool",
                description="Requires auth.",
                input_schema=ToolInputSchema(properties={}),
                requires_auth=True
            )
        )
        
    async def _run(self, params: Dict[str, Any], context: Optional[Dict[str, Any]] = None) -> Any:
        return {"user_id": context.get("user_id")}

@pytest.fixture
def clean_registry():
    from app.mcp.registry.tool_registry import registry
    registry.clear()
    yield registry
    registry.clear()

def test_registry_register_and_get(clean_registry):
    tool = MockTool()
    clean_registry.register(tool)
    
    fetched = clean_registry.get_tool("mock_tool")
    assert fetched is not None
    assert fetched.name == "mock_tool"
    
def test_registry_list_tools(clean_registry):
    clean_registry.register(MockTool())
    clean_registry.register(MockAuthTool())
    
    tools = clean_registry.list_tools()
    assert len(tools) == 2
    names = [t.name for t in tools]
    assert "mock_tool" in names
    assert "mock_auth_tool" in names

@pytest.mark.asyncio
async def test_tool_execution_success(clean_registry):
    clean_registry.register(MockTool())
    
    result = await ToolExecutor.execute("mock_tool", params={"echo": "hello"})
    assert result.status == "success"
    assert result.data["echoed"] == "hello"

@pytest.mark.asyncio
async def test_tool_execution_not_found(clean_registry):
    result = await ToolExecutor.execute("unknown_tool", params={})
    assert result.status == "error"
    assert "not found" in result.error.lower()

@pytest.mark.asyncio
async def test_tool_execution_auth_missing(clean_registry):
    clean_registry.register(MockAuthTool())
    
    # Execute without context
    result = await ToolExecutor.execute("mock_auth_tool", params={})
    assert result.status == "error"
    assert "authentication required" in result.error.lower()
    
@pytest.mark.asyncio
async def test_tool_execution_auth_provided(clean_registry):
    clean_registry.register(MockAuthTool())
    
    # Execute with context
    result = await ToolExecutor.execute("mock_auth_tool", params={}, context={"user_id": "user123"})
    assert result.status == "success"
    assert result.data["user_id"] == "user123"
