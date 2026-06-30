from fastapi import Security, HTTPException, status
from fastapi.security import APIKeyHeader
from app.config.settings import get_settings

api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)

async def verify_api_key(api_key: str = Security(api_key_header)):
    """
    Validates the X-API-Key header against the configured API_KEY.
    Used to secure internal communications from other microservices.
    """
    settings = get_settings()
    
    if not api_key:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing API Key"
        )
        
    if api_key != settings.API_KEY:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid API Key"
        )
        
    return api_key

# Placeholder for JWT validation when needed
async def verify_jwt_token(authorization: str = Security(APIKeyHeader(name="Authorization", auto_error=False))):
    """
    Placeholder dependency for validating user JWT tokens from the frontend.
    """
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing Bearer token"
        )
    # Token validation logic goes here
    return {"user_id": "placeholder"}
