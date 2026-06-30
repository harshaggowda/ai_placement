import time
import uuid
from fastapi import Request
from starlette.middleware.base import BaseHTTPMiddleware
from app.logging.logger import get_logger
import structlog

logger = get_logger(__name__)

class RequestLoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        # Generate correlation ID
        correlation_id = request.headers.get("X-Correlation-ID", str(uuid.uuid4()))
        
        # Bind correlation ID to the structlog context for this request
        structlog.contextvars.bind_contextvars(
            correlation_id=correlation_id,
            request_path=request.url.path,
            method=request.method,
            client_ip=request.client.host if request.client else None
        )
        
        start_time = time.perf_counter()
        
        try:
            response = await call_next(request)
            process_time = (time.perf_counter() - start_time) * 1000
            
            logger.info(
                "Request processed",
                status_code=response.status_code,
                duration_ms=round(process_time, 2)
            )
            
            response.headers["X-Correlation-ID"] = correlation_id
            return response
            
        except Exception as e:
            process_time = (time.perf_counter() - start_time) * 1000
            logger.exception("Request failed", duration_ms=round(process_time, 2))
            raise
        finally:
            structlog.contextvars.clear_contextvars()
