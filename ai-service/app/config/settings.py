from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field
from functools import lru_cache

class Settings(BaseSettings):
    """
    Application settings, loaded from environment variables and .env file.
    """
    
    # Environment profile (dev, staging, prod)
    ENV: str = Field("dev", env="ENV")
    
    # Application identifiers
    APP_NAME: str = "CareerOS AI Platform"
    APP_VERSION: str = "1.0.0"
    
    # Security / Auth
    API_KEY: str = Field(..., env="API_KEY")
    JWT_SECRET: str = Field(..., env="JWT_SECRET")
    
    # Server configuration
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    
    # Logging
    LOG_LEVEL: str = Field("INFO", env="LOG_LEVEL")
    
    # Database (PostgreSQL)
    DATABASE_URL: str = Field(..., env="DATABASE_URL")
    
    # Caching / Memory (Redis)
    REDIS_URL: str = Field(..., env="REDIS_URL")
    
    # AI Providers
    OPENAI_API_KEY: str = Field(..., env="OPENAI_API_KEY")
    OPENAI_TIMEOUT_SECONDS: int = Field(60, env="OPENAI_TIMEOUT_SECONDS")
    OPENAI_MAX_RETRIES: int = Field(3, env="OPENAI_MAX_RETRIES")
    
    # Model configuration
    DEFAULT_MODEL: str = Field("gpt-4o", env="DEFAULT_MODEL")
    FAST_MODEL: str = Field("gpt-4o-mini", env="FAST_MODEL")
    REASONING_MODEL: str = Field("o1", env="REASONING_MODEL")
    EMBEDDING_MODEL: str = Field("text-embedding-3-small", env="EMBEDDING_MODEL")
    DEFAULT_TEMPERATURE: float = Field(0.7, env="DEFAULT_TEMPERATURE")
    
    # Cross-Origin Resource Sharing
    ALLOWED_ORIGINS: list[str] = ["*"]
    
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )

@lru_cache()
def get_settings() -> Settings:
    """
    Returns a cached instance of the settings object to prevent 
    re-reading the .env file on every dependency injection.
    """
    return Settings()
