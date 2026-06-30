import pytest
from unittest.mock import patch, MagicMock, AsyncMock
from app.services.llm_service import LLMService
from app.core.models_registry import ModelType
from pydantic import BaseModel

class DummyResponse(BaseModel):
    name: str
    age: int

@pytest.fixture
def mock_openai_client():
    with patch("app.services.llm_service.OpenAIClientWrapper") as mock:
        yield mock

@pytest.fixture
def mock_telemetry():
    with patch("app.services.llm_service.TelemetryService") as mock:
        yield mock

@pytest.mark.asyncio
async def test_generate_text(mock_openai_client, mock_telemetry):
    # Setup mock
    wrapper_instance = MagicMock()
    mock_openai_client.return_value = wrapper_instance
    
    mock_response = MagicMock()
    mock_response.choices = [MagicMock(message=MagicMock(content="Mocked response"))]
    mock_response.usage = MagicMock(prompt_tokens=10, completion_tokens=5, total_tokens=15)
    wrapper_instance.chat_completion = AsyncMock(return_value=mock_response)
    
    service = LLMService()
    result = await service.generate_text([{"role": "user", "content": "Hi"}], model_type=ModelType.FAST)
    
    assert result == "Mocked response"
    wrapper_instance.chat_completion.assert_called_once()
    mock_telemetry.record_execution.assert_called_once()

@pytest.mark.asyncio
async def test_generate_json(mock_openai_client, mock_telemetry):
    wrapper_instance = MagicMock()
    mock_openai_client.return_value = wrapper_instance
    
    mock_response = MagicMock()
    mock_response.choices = [MagicMock(message=MagicMock(content='{"name": "Alice", "age": 30}'))]
    mock_response.usage = None
    wrapper_instance.chat_completion = AsyncMock(return_value=mock_response)
    
    service = LLMService()
    result = await service.generate_json([{"role": "user", "content": "JSON me"}], response_model=DummyResponse)
    
    assert isinstance(result, DummyResponse)
    assert result.name == "Alice"
    assert result.age == 30
