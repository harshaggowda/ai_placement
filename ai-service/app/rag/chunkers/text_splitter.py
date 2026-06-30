from typing import List
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter, TokenTextSplitter

from app.logging.logger import get_logger

logger = get_logger(__name__)

class TextChunker:
    """
    Handles chunking of documents using multiple strategies.
    Default uses RecursiveCharacterTextSplitter to respect paragraphs/sentences,
    falling back to TokenTextSplitter for strict token limits.
    """

    def __init__(self, chunk_size: int = 1000, chunk_overlap: int = 200):
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap

    def chunk_documents(self, documents: List[Document]) -> List[Document]:
        """
        Splits a list of documents into smaller chunks.
        """
        logger.debug("Chunking documents", count=len(documents), chunk_size=self.chunk_size, chunk_overlap=self.chunk_overlap)
        
        # Recursive character splitter is usually best for semantic coherence
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=self.chunk_size,
            chunk_overlap=self.chunk_overlap,
            length_function=len,
            is_separator_regex=False,
        )
        
        chunked_docs = text_splitter.split_documents(documents)
        logger.debug("Finished chunking documents", original_count=len(documents), chunked_count=len(chunked_docs))
        
        return chunked_docs

    def chunk_by_tokens(self, documents: List[Document], model_name: str = "gpt-4o") -> List[Document]:
        """
        Splits text based on token limits for strict context window management.
        """
        logger.debug("Chunking documents by tokens", count=len(documents), chunk_size=self.chunk_size)
        
        token_splitter = TokenTextSplitter(
            chunk_size=self.chunk_size,
            chunk_overlap=self.chunk_overlap,
            model_name=model_name
        )
        
        chunked_docs = token_splitter.split_documents(documents)
        return chunked_docs
