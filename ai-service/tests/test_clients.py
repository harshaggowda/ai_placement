import pytest
from unittest.mock import patch, MagicMock, AsyncMock
from app.clients.openai_client import OpenAIClientWrapper
from app.exceptions.errors import ProviderTimeoutError, ProviderAPIError
import httpx

@pytest.fixture
def mock_settings():
    with patch("app.clients.openai_client.get_settings") as mock:
        settings = MagicMock()
        settings.OPENAI_API_KEY = "sk-test"
        settings.OPENAI_TIMEOUT_SECONDS = 5
        settings.OPENAI_MAX_RETRIES = 1
        mock.return_value = settings
        yield mock

@pytest.mark.asyncio
async def test_openai_client_chat_completion(mock_settings):
    with patch("app.clients.openai_client.AsyncOpenAI") as mock_openai_cls:
        # Setup mock client
        mock_instance = MagicMock()
        mock_openai_cls.return_value = mock_instance
        
        # Setup async return value for chat completions
        mock_response = MagicMock()
        mock_instance.chat.completions.create = AsyncMock(return_value=mock_response)
        
        client = OpenAIClientWrapper()
        response = await client.chat_completion([{"role": "user", "content": "hello"}])
        
        assert response == mock_response
        mock_instance.chat.completions.create.assert_called_once()

@pytest.mark.asyncio
async def test_openai_client_timeout_handling(mock_settings):
    with patch("app.clients.openai_client.AsyncOpenAI") as mock_openai_cls:
        mock_instance = MagicMock()
        mock_openai_cls.return_value = mock_instance
        
        mock_instance.chat.completions.create = AsyncMock(side_effect=httpx.TimeoutException("Timeout"))
        
        client = OpenAIClientWrapper()
        with pytest.raises(ProviderTimeoutError):
            await client.chat_completion([{"role": "user", "content": "hello"}])

@pytest.mark.asyncio
async def test_openai_client_api_error_handling(mock_settings):
    with patch("app.clients.openai_client.AsyncOpenAI") as mock_openai_cls:
        mock_instance = MagicMock()
        mock_openai_cls.return_value = mock_instance
        
        mock_instance.chat.completions.create = AsyncMock(side_effect=Exception("API Error"))
        
        client = OpenAIClientWrapper()
        with pytest.raises(ProviderAPIError):
            await client.chat_completion([{"role": "user", "content": "hello"}])
