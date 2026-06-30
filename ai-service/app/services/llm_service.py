import time
from typing import Any, Dict, List, Optional, AsyncGenerator
from pydantic import BaseModel
import json

from app.clients.openai_client import OpenAIClientWrapper
from app.core.models_registry import ModelRegistry, ModelType
from app.core.telemetry import TelemetryService, RequestTelemetry, TokenUsage
from app.exceptions.errors import ProviderAPIError
from app.logging.logger import get_logger

logger = get_logger(__name__)

class LLMService:
    """
    Core LLM orchestration layer. Handles text generation, structured JSON output,
    streaming, telemetry, and fallback logic.
    """
    
    def __init__(self):
        self.client = OpenAIClientWrapper()
        
    async def generate_text(
        self, 
        messages: List[Dict[str, str]], 
        model_type: ModelType = ModelType.DEFAULT,
        temperature: float = 0.7,
        max_tokens: Optional[int] = None,
        stop: Optional[List[str]] = None,
        user_id: Optional[str] = None
    ) -> str:
        """Generates standard text completion."""
        model = ModelRegistry.get_model_name(model_type)
        start_time = time.perf_counter()
        
        response = await self.client.chat_completion(
            messages=messages,
            model=model,
            temperature=temperature,
            max_tokens=max_tokens,
            stop=stop
        )
        
        duration = (time.perf_counter() - start_time) * 1000
        
        if response.usage:
            usage = TokenUsage(
                prompt_tokens=response.usage.prompt_tokens,
                completion_tokens=response.usage.completion_tokens,
                total_tokens=response.usage.total_tokens,
                estimated_cost_usd=TelemetryService.calculate_cost(
                    model, response.usage.prompt_tokens, response.usage.completion_tokens
                )
            )
            TelemetryService.record_execution(RequestTelemetry(
                model=model,
                duration_ms=duration,
                usage=usage,
                user_id=user_id
            ))
            
        return response.choices[0].message.content

    async def generate_json(
        self,
        messages: List[Dict[str, str]],
        response_model: type[BaseModel],
        model_type: ModelType = ModelType.DEFAULT,
        temperature: float = 0.0,
        user_id: Optional[str] = None
    ) -> BaseModel:
        """
        Generates structured JSON mapped to a Pydantic model.
        Uses OpenAI's json_schema integration (if supported) or JSON mode fallback.
        """
        model = ModelRegistry.get_model_name(model_type)
        start_time = time.perf_counter()
        
        # We can pass the schema directly for OpenAI structured outputs
        # Note: Instructor is better for complex cases, but this works natively for simple ones
        schema = response_model.model_json_schema()
        
        response = await self.client.chat_completion(
            messages=messages,
            model=model,
            temperature=temperature,
            response_format={
                "type": "json_schema",
                "json_schema": {
                    "name": response_model.__name__,
                    "schema": schema,
                    "strict": True
                }
            }
        )
        
        duration = (time.perf_counter() - start_time) * 1000
        content = response.choices[0].message.content
        
        if response.usage:
            usage = TokenUsage(
                prompt_tokens=response.usage.prompt_tokens,
                completion_tokens=response.usage.completion_tokens,
                total_tokens=response.usage.total_tokens,
                estimated_cost_usd=TelemetryService.calculate_cost(
                    model, response.usage.prompt_tokens, response.usage.completion_tokens
                )
            )
            TelemetryService.record_execution(RequestTelemetry(
                model=model,
                duration_ms=duration,
                usage=usage,
                user_id=user_id
            ))
            
        try:
            parsed_data = json.loads(content)
            return response_model.model_validate(parsed_data)
        except Exception as e:
            logger.error("Failed to parse or validate JSON response", error=str(e), content=content)
            raise ProviderAPIError("openai", f"Malformed JSON output: {str(e)}")

    async def stream_text(
        self,
        messages: List[Dict[str, str]],
        model_type: ModelType = ModelType.DEFAULT,
        temperature: float = 0.7,
        user_id: Optional[str] = None
    ) -> AsyncGenerator[str, None]:
        """
        Yields text chunks as they arrive from the model.
        """
        model = ModelRegistry.get_model_name(model_type)
        start_time = time.perf_counter()
        
        # Note: stream=True requires us to use the client directly or update the wrapper
        response_stream = await self.client.client.chat.completions.create(
            model=model,
            messages=messages,
            temperature=temperature,
            stream=True,
            stream_options={"include_usage": True}
        )
        
        prompt_tokens = 0
        completion_tokens = 0
        
        async for chunk in response_stream:
            if chunk.choices and chunk.choices[0].delta.content:
                yield chunk.choices[0].delta.content
                
            if chunk.usage:
                prompt_tokens = chunk.usage.prompt_tokens
                completion_tokens = chunk.usage.completion_tokens
                
        duration = (time.perf_counter() - start_time) * 1000
        
        if prompt_tokens > 0:
            usage = TokenUsage(
                prompt_tokens=prompt_tokens,
                completion_tokens=completion_tokens,
                total_tokens=prompt_tokens + completion_tokens,
                estimated_cost_usd=TelemetryService.calculate_cost(
                    model, prompt_tokens, completion_tokens
                )
            )
            TelemetryService.record_execution(RequestTelemetry(
                model=model,
                duration_ms=duration,
                usage=usage,
                user_id=user_id
            ))
