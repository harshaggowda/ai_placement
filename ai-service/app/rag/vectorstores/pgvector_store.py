from typing import List, Optional, Any
from langchain_core.documents import Document
from langchain_postgres import PGVector
from sqlalchemy.ext.asyncio import create_async_engine

from app.config.settings import get_settings
from app.rag.embeddings.provider import EmbeddingProvider
from app.logging.logger import get_logger

logger = get_logger(__name__)

class VectorStoreManager:
    """
    Manages interactions with pgvector via Langchain.
    Handles adding documents and obtaining retrievers.
    """

    def __init__(self, collection_name: str):
        self.settings = get_settings()
        self.collection_name = collection_name
        self.embeddings = EmbeddingProvider.get_embeddings()
        
        # Use connection string from settings (assumes postgresql+asyncpg:// format)
        # However, Langchain's PGVector currently prefers psycopg3 for sync/async.
        # If DATABASE_URL is asyncpg, we might need to adapt it, but PGVector in langchain_postgres
        # supports asyncpg engines or string URLs.
        
        connection_string = self.settings.DATABASE_URL
        
        # Ensure we have a valid postgres URL
        if not connection_string:
            logger.warning("DATABASE_URL is missing. Using placeholder for testing.")
            connection_string = "postgresql+asyncpg://user:pass@localhost/db"
            
        self.connection_string = connection_string

    def _get_vector_store(self) -> PGVector:
        """
        Initializes the PGVector store object.
        """
        # This initializes the table automatically if it doesn't exist
        return PGVector(
            embeddings=self.embeddings,
            collection_name=self.collection_name,
            connection=self.connection_string,
            use_jsonb=True,
        )

    async def add_documents(self, documents: List[Document], ids: Optional[List[str]] = None) -> List[str]:
        """
        Adds a list of chunked documents to the pgvector store.
        """
        logger.info("Adding documents to vector store", collection=self.collection_name, count=len(documents))
        store = self._get_vector_store()
        
        # PGVector supports async methods now
        returned_ids = await store.aadd_documents(documents, ids=ids)
        return returned_ids

    async def delete_documents(self, ids: List[str]):
        """
        Deletes documents by their vector IDs.
        """
        logger.info("Deleting documents from vector store", collection=self.collection_name, count=len(ids))
        store = self._get_vector_store()
        await store.adelete(ids=ids)

    def get_retriever(self, k: int = 4, filter: Optional[dict] = None) -> Any:
        """
        Returns a Langchain Retriever connected to this collection.
        """
        store = self._get_vector_store()
        search_kwargs = {"k": k}
        if filter:
            search_kwargs["filter"] = filter
            
        return store.as_retriever(search_kwargs=search_kwargs)

    async def similarity_search(self, query: str, k: int = 4, filter: Optional[dict] = None) -> List[Document]:
        """
        Directly executes a similarity search against the vector store.
        """
        logger.debug("Executing similarity search", collection=self.collection_name, query=query, k=k)
        store = self._get_vector_store()
        docs = await store.asimilarity_search(query, k=k, filter=filter)
        return docs
