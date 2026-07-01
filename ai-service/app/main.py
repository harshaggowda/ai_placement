from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config.settings import get_settings
from app.logging.logger import setup_logging, get_logger
from app.exceptions.handlers import add_exception_handlers
from app.middleware.logging_middleware import RequestLoggingMiddleware
from app.monitoring.health import router as health_router

logger = get_logger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Starting AI Platform Foundation...")
    # Initialize DB pools, Redis connections here
    yield
    # Shutdown
    logger.info("Shutting down AI Platform Foundation...")
    # Close connections here

def create_app() -> FastAPI:
    # 1. Initialize Settings and Logging
    setup_logging()
    settings = get_settings()

    # 2. Create FastAPI Application
    app = FastAPI(
        title=settings.APP_NAME,
        version=settings.APP_VERSION,
        description="CareerOS AI Platform REST API",
        lifespan=lifespan,
        docs_url="/docs" if settings.ENV != "prod" else None,
        redoc_url="/redoc" if settings.ENV != "prod" else None,
    )

    # 3. Add Middleware
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.ALLOWED_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    app.add_middleware(RequestLoggingMiddleware)

    # 4. Add Exception Handlers
    add_exception_handlers(app)

    # 5. Include Routers
    app.include_router(health_router, prefix="/api/v1")
    
    from app.api.llm_router import router as llm_router
    from app.api.rag_router import router as rag_router
    from app.api.mcp_router import router as mcp_router

    # Register routers
    app.include_router(llm_router, prefix="/api/v1")
    app.include_router(rag_router, prefix="/api/v1")
    app.include_router(mcp_router, prefix="/api/v1")

    return app

app = create_app()
