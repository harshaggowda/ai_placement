import httpx
from openai import AsyncOpenAI
from app.config.settings import get_settings
from app.logging.logger import get_logger
from app.exceptions.errors import ProviderTimeoutError, ProviderAPIError

logger = get_logger(__name__)

class OpenAIClientWrapper:
    """
    A specialized wrapper around the OpenAI SDK to handle timeouts, 
    retries, logging, and metrics hooks (e.g., token usage tracking).
    """
    def __init__(self):
        settings = get_settings()
        # Ensure we use an async httpx client with timeouts
        http_client = httpx.AsyncClient(
            timeout=httpx.Timeout(settings.OPENAI_TIMEOUT_SECONDS)
        )
        self.client = AsyncOpenAI(
            api_key=settings.OPENAI_API_KEY,
            http_client=http_client,
            max_retries=settings.OPENAI_MAX_RETRIES
        )
    
    async def chat_completion(self, messages: list[dict], model: str = "gpt-4o", **kwargs):
        """
        Wrapper around chat completions.
        Hooks for logging and token tracking can be added here.
        """
        logger.debug("Requesting chat completion", model=model, message_count=len(messages))
        try:
            # Pop unsupported kwargs for reasoning models if needed
            if "o1" in model:
                kwargs.pop("temperature", None)
                
            response = await self.client.chat.completions.create(
                model=model,
                messages=messages,
                **kwargs
            )
            # Future: Log token usage here
            if response.usage:
                logger.debug("Token usage", prompt=response.usage.prompt_tokens, completion=response.usage.completion_tokens)
                
            return response
        except httpx.TimeoutException:
            logger.error("OpenAI timeout")
            raise ProviderTimeoutError("openai")
        except Exception as e:
            logger.error("OpenAI API error", details=str(e))
            raise ProviderAPIError("openai", str(e))
