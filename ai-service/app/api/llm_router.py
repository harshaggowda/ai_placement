from typing import Any, Dict, List, Optional
from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from app.services.llm_service import LLMService
from app.core.models_registry import ModelType

router = APIRouter(prefix="/llm", tags=["LLM Internal"])

# We instantiate service as a dependency for easier mocking in tests if needed
def get_llm_service() -> LLMService:
    return LLMService()

class ChatRequest(BaseModel):
    messages: List[Dict[str, str]] = Field(..., description="List of OpenAI formatted messages")
    model_type: ModelType = Field(default=ModelType.DEFAULT)
    temperature: float = Field(default=0.7)
    
class ChatResponse(BaseModel):
    content: str

class JsonRequest(BaseModel):
    messages: List[Dict[str, str]]
    model_type: ModelType = Field(default=ModelType.DEFAULT)
    
class JsonResponseSchema(BaseModel):
    """Generic JSON response model for testing."""
    key: str
    value: str

@router.post("/chat", response_model=ChatResponse)
async def chat_completion(
    request: ChatRequest,
    llm_service: LLMService = Depends(get_llm_service)
):
    """
    Internal testing endpoint for standard text completion.
    """
    content = await llm_service.generate_text(
        messages=request.messages,
        model_type=request.model_type,
        temperature=request.temperature
    )
    return ChatResponse(content=content)

@router.post("/json", response_model=JsonResponseSchema)
async def json_completion(
    request: JsonRequest,
    llm_service: LLMService = Depends(get_llm_service)
):
    """
    Internal testing endpoint for structured JSON output.
    Uses a hardcoded JsonResponseSchema for demonstration/testing.
    """
    response_obj = await llm_service.generate_json(
        messages=request.messages,
        response_model=JsonResponseSchema,
        model_type=request.model_type
    )
    return response_obj

@router.post("/stream")
async def stream_completion(
    request: ChatRequest,
    llm_service: LLMService = Depends(get_llm_service)
):
    """
    Internal testing endpoint for streaming text generation.
    """
    generator = llm_service.stream_text(
        messages=request.messages,
        model_type=request.model_type,
        temperature=request.temperature
    )
    return StreamingResponse(generator, media_type="text/event-stream")

@router.get("/models")
async def list_models():
    """
    Returns the current environment configuration for models.
    """
    from app.core.models_registry import ModelRegistry
    return {
        "DEFAULT": ModelRegistry.get_model_name(ModelType.DEFAULT),
        "FAST": ModelRegistry.get_model_name(ModelType.FAST),
        "REASONING": ModelRegistry.get_model_name(ModelType.REASONING),
        "EMBEDDING": ModelRegistry.get_model_name(ModelType.EMBEDDING),
    }

@router.get("/health")
async def llm_health():
    """
    Health check specifically for the LLM infrastructure.
    """
    return {"status": "ok", "service": "llm"}
