from typing import Optional
from pydantic import BaseModel
from app.logging.logger import get_logger

logger = get_logger(__name__)

class TokenUsage(BaseModel):
    prompt_tokens: int = 0
    completion_tokens: int = 0
    total_tokens: int = 0
    estimated_cost_usd: float = 0.0

class RequestTelemetry(BaseModel):
    model: str
    duration_ms: float
    usage: TokenUsage
    request_id: Optional[str] = None
    user_id: Optional[str] = None

class TelemetryService:
    """
    Handles logging and accounting for LLM execution metrics.
    Future: Could emit metrics to Prometheus/Datadog or save to DB.
    """
    
    @staticmethod
    def calculate_cost(model: str, prompt_tokens: int, completion_tokens: int) -> float:
        """
        Placeholder cost calculator. 
        In production, this would look up pricing rates dynamically.
        """
        rates = {
            "gpt-4o": {"prompt": 0.005 / 1000, "completion": 0.015 / 1000},
            "gpt-4o-mini": {"prompt": 0.00015 / 1000, "completion": 0.0006 / 1000},
            "o1": {"prompt": 0.015 / 1000, "completion": 0.060 / 1000},
        }
        
        rate = rates.get(model, {"prompt": 0.0, "completion": 0.0})
        return (prompt_tokens * rate["prompt"]) + (completion_tokens * rate["completion"])

    @staticmethod
    def record_execution(telemetry: RequestTelemetry):
        """
        Records the execution metrics via structured logging.
        """
        logger.info(
            "LLM Execution",
            model=telemetry.model,
            duration_ms=round(telemetry.duration_ms, 2),
            prompt_tokens=telemetry.usage.prompt_tokens,
            completion_tokens=telemetry.usage.completion_tokens,
            total_tokens=telemetry.usage.total_tokens,
            estimated_cost=round(telemetry.usage.estimated_cost_usd, 6),
            request_id=telemetry.request_id,
            user_id=telemetry.user_id
        )
