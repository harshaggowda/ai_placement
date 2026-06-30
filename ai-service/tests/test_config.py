import os
from unittest.mock import patch
from app.config.settings import Settings

def test_settings_load_from_env():
    # Setup environment
    env_vars = {
        "ENV": "prod",
        "API_KEY": "test-key",
        "JWT_SECRET": "test-secret",
        "DATABASE_URL": "postgresql+asyncpg://user:pass@localhost:5432/db",
        "REDIS_URL": "redis://localhost:6379",
        "OPENAI_API_KEY": "sk-test",
    }
    
    with patch.dict(os.environ, env_vars, clear=True):
        settings = Settings()
        assert settings.ENV == "prod"
        assert settings.API_KEY == "test-key"
        assert settings.OPENAI_API_KEY == "sk-test"
