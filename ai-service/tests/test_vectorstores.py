import pytest
from unittest.mock import patch, MagicMock, AsyncMock
from app.rag.vectorstores.pgvector_store import VectorStoreManager
from langchain_core.documents import Document

@pytest.fixture
def mock_embedding_provider():
    with patch("app.rag.vectorstores.pgvector_store.EmbeddingProvider") as mock:
        yield mock

@pytest.fixture
def mock_pgvector():
    with patch("app.rag.vectorstores.pgvector_store.PGVector") as mock:
        yield mock

@pytest.mark.asyncio
async def test_add_documents(mock_embedding_provider, mock_pgvector):
    # Setup mock
    store_instance = MagicMock()
    mock_pgvector.return_value = store_instance
    
    store_instance.aadd_documents = AsyncMock(return_value=["id1", "id2"])
    
    manager = VectorStoreManager("test_collection")
    docs = [Document(page_content="test")]
    ids = await manager.add_documents(docs)
    
    assert ids == ["id1", "id2"]
    store_instance.aadd_documents.assert_called_once_with(docs, ids=None)

@pytest.mark.asyncio
async def test_similarity_search(mock_embedding_provider, mock_pgvector):
    store_instance = MagicMock()
    mock_pgvector.return_value = store_instance
    
    store_instance.asimilarity_search = AsyncMock(return_value=[Document(page_content="match")])
    
    manager = VectorStoreManager("test_collection")
    results = await manager.similarity_search("query", k=1)
    
    assert len(results) == 1
    assert results[0].page_content == "match"
    store_instance.asimilarity_search.assert_called_once_with("query", k=1, filter=None)
