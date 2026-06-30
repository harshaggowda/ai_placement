from enum import Enum
from app.config.settings import get_settings

class ModelType(str, Enum):
    """Enumeration of semantic model types instead of hardcoded model names."""
    DEFAULT = "default"      # Balanced performance/cost (e.g., gpt-4o)
    FAST = "fast"            # Low latency/cost (e.g., gpt-4o-mini)
    REASONING = "reasoning"  # Complex tasks (e.g., o1)
    EMBEDDING = "embedding"  # Text embeddings

class ModelRegistry:
    """
    Provides dynamic resolution of semantic model types to actual provider model names
    based on the current environment configuration.
    """
    
    @staticmethod
    def get_model_name(model_type: ModelType) -> str:
        """
        Returns the specific model string (e.g. 'gpt-4o') configured for the given ModelType.
        """
        settings = get_settings()
        
        mapping = {
            ModelType.DEFAULT: settings.DEFAULT_MODEL,
            ModelType.FAST: settings.FAST_MODEL,
            ModelType.REASONING: settings.REASONING_MODEL,
            ModelType.EMBEDDING: settings.EMBEDDING_MODEL,
        }
        
        return mapping.get(model_type, settings.DEFAULT_MODEL)
