import pytest
from langchain_core.documents import Document
from app.rag.chunkers.text_splitter import TextChunker

@pytest.fixture
def sample_document():
    # 50 words
    text = "Word " * 50
    return Document(page_content=text, metadata={"source": "test"})

def test_recursive_character_chunker(sample_document):
    # Set chunk size to 50 characters, expecting multiple chunks
    chunker = TextChunker(chunk_size=50, chunk_overlap=10)
    chunks = chunker.chunk_documents([sample_document])
    
    assert len(chunks) > 1
    # Check if metadata was preserved
    assert chunks[0].metadata["source"] == "test"
    assert chunks[-1].metadata["source"] == "test"
    assert len(chunks[0].page_content) <= 50

def test_token_chunker(sample_document):
    chunker = TextChunker(chunk_size=10, chunk_overlap=2)
    chunks = chunker.chunk_by_tokens([sample_document])
    
    assert len(chunks) > 1
    assert chunks[0].metadata["source"] == "test"
