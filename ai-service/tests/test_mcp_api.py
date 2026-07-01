import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, AsyncMock

from app.main import app
from app.mcp.schemas.tool import ExecutionResult

client = TestClient(app)

def test_list_tools_endpoint():
    # Ensure default tools are loaded for the test, since other tests might have cleared the registry
    from app.mcp.registry.tool_registry import registry
    if len(registry.list_tools()) == 0:
        import importlib
        import app.mcp.tools
        importlib.reload(app.mcp.tools)

    response = client.get("/api/v1/mcp/tools")
    assert response.status_code == 200
    tools = response.json()
    assert isinstance(tools, list)
    # the 6 default tools should be registered
    assert len(tools) >= 6
    names = [t["name"] for t in tools]
    assert "web_search" in names
    assert "github" in names

@patch("app.api.mcp_router.ToolExecutor.execute", new_callable=AsyncMock)
def test_execute_tool_endpoint(mock_execute):
    mock_execute.return_value = ExecutionResult(
        status="success",
        data={"mock": "data"},
        execution_time_ms=10.0
    )
    
    response = client.post("/api/v1/mcp/execute", json={
        "tool_name": "web_search",
        "params": {"query": "test"}
    })
    
    assert response.status_code == 200
    assert response.json()["status"] == "success"
    assert response.json()["data"] == {"mock": "data"}

def test_health_endpoint():
    response = client.get("/api/v1/mcp/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"

def test_metrics_endpoint():
    response = client.get("/api/v1/mcp/metrics")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"
