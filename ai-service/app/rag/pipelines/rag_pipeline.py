from typing import List, Optional
from pydantic import BaseModel, Field

from app.rag.collections.manager import CollectionManager
from app.rag.document_loaders.factory import DocumentLoaderFactory
from app.rag.chunkers.text_splitter import TextChunker
from app.logging.logger import get_logger

logger = get_logger(__name__)

class IndexingResult(BaseModel):
    collection: str
    documents_indexed: int
    chunks_created: int
    vector_ids: List[str]

class RAGPipeline:
    """
    Orchestrates the complete flow of data:
    1. Loading -> Chunking -> Embedding -> Indexing (Write Path)
    2. Querying -> Retrieving (Read Path)
    """

    @staticmethod
    async def index_file(
        file_path: str,
        collection_name: str,
        metadata: Optional[dict] = None,
        chunk_size: int = 1000,
        chunk_overlap: int = 200
    ) -> IndexingResult:
        """
        Full write pipeline: Loads a file, chunks it, and indexes it into the vector store.
        """
        logger.info("Starting indexing pipeline", file=file_path, collection=collection_name)
        
        # 1. Load
        documents = DocumentLoaderFactory.load_file(file_path, metadata=metadata)
        doc_count = len(documents)
        
        # 2. Chunk
        chunker = TextChunker(chunk_size=chunk_size, chunk_overlap=chunk_overlap)
        chunks = chunker.chunk_documents(documents)
        chunk_count = len(chunks)
        
        # 3. Store
        store = CollectionManager.get_store(collection_name)
        ids = await store.add_documents(chunks)
        
        logger.info("Finished indexing pipeline", chunks=chunk_count, ids_count=len(ids))
        
        return IndexingResult(
            collection=collection_name,
            documents_indexed=doc_count,
            chunks_created=chunk_count,
            vector_ids=ids
        )
        
    @staticmethod
    async def retrieve_context(
        query: str,
        collection_name: str,
        k: int = 4,
        filter: Optional[dict] = None
    ) -> str:
        """
        Read pipeline: Fetches the top K matching chunks and concatenates them into a single 
        context string for injection into an LLM prompt.
        """
        store = CollectionManager.get_store(collection_name)
        docs = await store.similarity_search(query, k=k, filter=filter)
        
        # Assemble context
        context_parts = []
        for i, doc in enumerate(docs):
            source = doc.metadata.get("source", "Unknown")
            context_parts.append(f"--- Document {i+1} (Source: {source}) ---\n{doc.page_content}")
            
        full_context = "\n\n".join(context_parts)
        return full_context
