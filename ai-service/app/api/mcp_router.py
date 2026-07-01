from typing import Dict, Any, List, Optional
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel

from app.mcp.registry.tool_registry import registry
from app.mcp.execution.executor import ToolExecutor
from app.mcp.schemas.tool import ToolMetadata, ExecutionResult

# Ensure tools are registered
import app.mcp.tools

router = APIRouter(prefix="/mcp", tags=["MCP Interoperability"])

class ExecuteRequest(BaseModel):
    tool_name: str
    params: Dict[str, Any]
    context: Optional[Dict[str, Any]] = None

@router.get("/tools", response_model=List[ToolMetadata])
async def list_tools():
    """
    Returns the metadata and schemas for all registered MCP tools.
    """
    return registry.list_tools()

@router.post("/execute", response_model=ExecutionResult)
async def execute_tool(request: ExecuteRequest):
    """
    Executes a specific tool securely via the MCP Execution Pipeline.
    """
    try:
        # Context extraction logic would normally inject user_id from headers/auth here
        # For internal MCP, we rely on the caller providing valid context
        result = await ToolExecutor.execute(
            tool_name=request.tool_name,
            params=request.params,
            context=request.context
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/health")
async def mcp_health():
    """
    Health check endpoint for the MCP framework.
    """
    return {
        "status": "ok",
        "service": "mcp",
        "registered_tools_count": len(registry.list_tools())
    }

@router.get("/metrics")
async def mcp_metrics():
    """
    Returns simple metrics on tool execution (for observability).
    """
    # This is a stub for future prometheus/telemetry metrics
    return {
        "status": "ok",
        "metrics": "not_implemented"
    }
