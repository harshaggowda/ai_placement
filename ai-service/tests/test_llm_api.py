import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, AsyncMock
from app.main import app

client = TestClient(app)

@pytest.fixture
def mock_llm_service():
    with patch("app.api.llm_router.LLMService") as mock:
        yield mock

def test_chat_endpoint(mock_llm_service):
    service_instance = mock_llm_service.return_value
    service_instance.generate_text = AsyncMock(return_value="Hello API")
    
    response = client.post("/api/v1/llm/chat", json={
        "messages": [{"role": "user", "content": "Hi"}],
        "model_type": "default"
    })
    
    assert response.status_code == 200
    assert response.json()["content"] == "Hello API"

def test_json_endpoint(mock_llm_service):
    from app.api.llm_router import JsonResponseSchema
    service_instance = mock_llm_service.return_value
    service_instance.generate_json = AsyncMock(return_value=JsonResponseSchema(key="greeting", value="hello"))
    
    response = client.post("/api/v1/llm/json", json={
        "messages": [{"role": "user", "content": "JSON"}]
    })
    
    assert response.status_code == 200
    assert response.json()["key"] == "greeting"
    assert response.json()["value"] == "hello"

def test_models_endpoint():
    response = client.get("/api/v1/llm/models")
    assert response.status_code == 200
    assert "DEFAULT" in response.json()
