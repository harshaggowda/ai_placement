import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, AsyncMock
from app.main import app

client = TestClient(app)

@pytest.fixture
def mock_pipeline():
    with patch("app.api.rag_router.RAGPipeline") as mock:
        yield mock

@pytest.fixture
def mock_collection():
    with patch("app.api.rag_router.CollectionManager") as mock:
        yield mock

def test_index_endpoint(mock_pipeline):
    from app.rag.pipelines.rag_pipeline import IndexingResult
    mock_pipeline.index_file = AsyncMock(return_value=IndexingResult(
        collection="test",
        documents_indexed=1,
        chunks_created=2,
        vector_ids=["id1", "id2"]
    ))
    
    response = client.post("/api/v1/rag/index", json={
        "file_path": "fake.txt",
        "collection": "test"
    })
    
    assert response.status_code == 200
    assert response.json()["chunks_created"] == 2

def test_query_endpoint(mock_pipeline):
    mock_pipeline.retrieve_context = AsyncMock(return_value="mock context")
    
    response = client.post("/api/v1/rag/query", json={
        "query": "hello",
        "collection": "test"
    })
    
    assert response.status_code == 200
    assert response.json()["context"] == "mock context"

def test_collections_endpoint():
    response = client.get("/api/v1/rag/collections")
    assert response.status_code == 200
    assert isinstance(response.json(), list)

@patch("app.api.rag_router.EmbeddingProvider")
def test_health_endpoint(mock_embedding):
    mock_embedding.get_embeddings.return_value = "mock_embedding_obj"
    response = client.get("/api/v1/rag/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"
