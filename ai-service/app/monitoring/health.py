from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter(tags=["Monitoring"])

class HealthResponse(BaseModel):
    status: str
    version: str

@router.get("/health", response_model=HealthResponse)
async def health_check():
    """
    General health endpoint for liveness probes.
    """
    from app.config.settings import get_settings
    settings = get_settings()
    return HealthResponse(status="ok", version=settings.APP_VERSION)

@router.get("/ready", response_model=HealthResponse)
async def readiness_check():
    """
    Readiness endpoint to check if the app is ready to receive traffic.
    (Placeholder: Should check DB and Redis connections).
    """
    from app.config.settings import get_settings
    settings = get_settings()
    return HealthResponse(status="ready", version=settings.APP_VERSION)

@router.get("/live", response_model=HealthResponse)
async def liveness_check():
    """
    Liveness endpoint. Simply confirms the server loop is running.
    """
    from app.config.settings import get_settings
    settings = get_settings()
    return HealthResponse(status="alive", version=settings.APP_VERSION)
