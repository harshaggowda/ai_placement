import pytest
from app.rag.document_loaders.factory import DocumentLoaderFactory
from app.exceptions.errors import ConfigurationError
import os

@pytest.fixture
def sample_text_file(tmp_path):
    file_path = tmp_path / "test.txt"
    file_path.write_text("This is a sample document for testing RAG chunking.")
    return str(file_path)

def test_load_text_file(sample_text_file):
    metadata = {"source": "unit_test", "author": "AI"}
    docs = DocumentLoaderFactory.load_file(sample_text_file, metadata=metadata)
    
    assert len(docs) == 1
    assert "This is a sample document" in docs[0].page_content
    assert docs[0].metadata["source"] == "unit_test"
    assert docs[0].metadata["author"] == "AI"

def test_load_file_not_found():
    with pytest.raises(FileNotFoundError):
        DocumentLoaderFactory.load_file("nonexistent.txt")

def test_unsupported_extension(tmp_path):
    file_path = tmp_path / "test.unknown"
    file_path.write_text("data")
    with pytest.raises(ConfigurationError):
        DocumentLoaderFactory.load_file(str(file_path))
