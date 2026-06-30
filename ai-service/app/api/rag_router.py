from typing import List, Dict, Any, Optional
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field

from app.rag.pipelines.rag_pipeline import RAGPipeline, IndexingResult
from app.rag.collections.manager import CollectionManager, CollectionInfo
from app.rag.embeddings.provider import EmbeddingProvider

router = APIRouter(prefix="/rag", tags=["RAG Internal"])

class IndexRequest(BaseModel):
    file_path: str = Field(..., description="Absolute path to the file to index")
    collection: str = Field(..., description="Target collection name")
    metadata: Optional[Dict[str, Any]] = None
    chunk_size: int = 1000
    chunk_overlap: int = 200

class SearchRequest(BaseModel):
    query: str
    collection: str
    k: int = 4
    filter: Optional[Dict[str, Any]] = None

class DeleteRequest(BaseModel):
    collection: str
    vector_ids: List[str]

@router.post("/index", response_model=IndexingResult)
async def index_document(request: IndexRequest):
    """
    Indexes a document from the local filesystem into pgvector.
    """
    try:
        result = await RAGPipeline.index_file(
            file_path=request.file_path,
            collection_name=request.collection,
            metadata=request.metadata,
            chunk_size=request.chunk_size,
            chunk_overlap=request.chunk_overlap
        )
        return result
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="File not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/search")
async def search_documents(request: SearchRequest):
    """
    Performs similarity search and returns Langchain documents (content + metadata).
    """
    try:
        store = CollectionManager.get_store(request.collection)
        docs = await store.similarity_search(request.query, request.k, request.filter)
        return [
            {"page_content": doc.page_content, "metadata": doc.metadata}
            for doc in docs
        ]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/query")
async def query_context(request: SearchRequest):
    """
    Helper endpoint that retrieves documents and formats them into a single string
    suitable for injecting into an LLM prompt.
    """
    try:
        context = await RAGPipeline.retrieve_context(
            query=request.query,
            collection_name=request.collection,
            k=request.k,
            filter=request.filter
        )
        return {"context": context}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/delete")
async def delete_documents(request: DeleteRequest):
    """
    Deletes specific chunks from a collection by their vector IDs.
    """
    try:
        store = CollectionManager.get_store(request.collection)
        await store.delete_documents(request.vector_ids)
        return {"status": "success", "deleted_count": len(request.vector_ids)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/collections", response_model=List[CollectionInfo])
async def list_collections():
    """
    Lists the predefined knowledge collections.
    """
    return CollectionManager.list_collections()

@router.get("/health")
async def rag_health():
    """
    Checks if the RAG components (like embeddings and DB connection) can be initialized.
    """
    try:
        # Just initialize the embeddings to ensure API keys are loaded
        EmbeddingProvider.get_embeddings()
        return {"status": "ok", "service": "rag"}
    except Exception as e:
        raise HTTPException(status_code=503, detail=str(e))
