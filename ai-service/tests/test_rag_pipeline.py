import pytest
from unittest.mock import patch, MagicMock, AsyncMock
from app.rag.pipelines.rag_pipeline import RAGPipeline
from langchain_core.documents import Document

@pytest.mark.asyncio
@patch("app.rag.pipelines.rag_pipeline.DocumentLoaderFactory")
@patch("app.rag.pipelines.rag_pipeline.TextChunker")
@patch("app.rag.pipelines.rag_pipeline.CollectionManager")
async def test_index_file(mock_collection, mock_chunker_cls, mock_factory):
    # Setup load
    mock_factory.load_file.return_value = [Document(page_content="loaded")]
    
    # Setup chunk
    mock_chunker = MagicMock()
    mock_chunker.chunk_documents.return_value = [Document(page_content="chunk1"), Document(page_content="chunk2")]
    mock_chunker_cls.return_value = mock_chunker
    
    # Setup store
    mock_store = MagicMock()
    mock_store.add_documents = AsyncMock(return_value=["id1", "id2"])
    mock_collection.get_store.return_value = mock_store
    
    result = await RAGPipeline.index_file("fake.txt", "test_collection")
    
    assert result.collection == "test_collection"
    assert result.documents_indexed == 1
    assert result.chunks_created == 2
    assert result.vector_ids == ["id1", "id2"]

@pytest.mark.asyncio
@patch("app.rag.pipelines.rag_pipeline.CollectionManager")
async def test_retrieve_context(mock_collection):
    mock_store = MagicMock()
    mock_store.similarity_search = AsyncMock(return_value=[
        Document(page_content="chunk1", metadata={"source": "doc1"}),
        Document(page_content="chunk2", metadata={"source": "doc2"})
    ])
    mock_collection.get_store.return_value = mock_store
    
    context = await RAGPipeline.retrieve_context("query", "test_collection")
    
    assert "chunk1" in context
    assert "chunk2" in context
    assert "doc1" in context
    assert "doc2" in context
