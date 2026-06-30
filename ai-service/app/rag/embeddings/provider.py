from typing import List
from langchain_core.embeddings import Embeddings
from langchain_openai import OpenAIEmbeddings

from app.config.settings import get_settings
from app.core.models_registry import ModelType, ModelRegistry
from app.logging.logger import get_logger

logger = get_logger(__name__)

class EmbeddingProvider:
    """
    Provides access to embedding models.
    Currently wraps OpenAI's embeddings, but exposes standard Langchain Embeddings interface
    so it can be easily swapped for HuggingFace, Cohere, etc.
    """

    @staticmethod
    def get_embeddings() -> Embeddings:
        """
        Returns an instance of LangChain's Embeddings interface.
        """
        settings = get_settings()
        model_name = ModelRegistry.get_model_name(ModelType.EMBEDDING)
        
        logger.debug("Initializing embedding model", model=model_name)
        
        # We use standard OpenAIEmbeddings from langchain-openai
        embeddings = OpenAIEmbeddings(
            api_key=settings.OPENAI_API_KEY,
            model=model_name,
            max_retries=settings.OPENAI_MAX_RETRIES,
            timeout=settings.OPENAI_TIMEOUT_SECONDS
        )
        
        return embeddings
