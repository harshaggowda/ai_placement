import os
from typing import List, Dict, Any, Optional
from langchain_core.documents import Document

# Loaders
from langchain_community.document_loaders import (
    TextLoader,
    PDFPlumberLoader,
    Docx2txtLoader,
    UnstructuredHTMLLoader,
    JSONLoader,
    CSVLoader
)

from app.logging.logger import get_logger
from app.exceptions.errors import ConfigurationError

logger = get_logger(__name__)

class DocumentLoaderFactory:
    """
    Factory for instantiating the correct document loader based on file extension
    or content type.
    """

    @staticmethod
    def load_file(file_path: str, metadata: Optional[Dict[str, Any]] = None) -> List[Document]:
        """
        Loads a file from the filesystem and returns a list of Langchain Documents.
        Automatically attaches any custom metadata provided.
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"File not found: {file_path}")

        ext = os.path.splitext(file_path)[1].lower()
        logger.debug("Loading document", file_path=file_path, extension=ext)

        docs = []
        try:
            if ext in [".txt", ".md"]:
                loader = TextLoader(file_path, encoding="utf-8")
                docs = loader.load()
            elif ext == ".pdf":
                loader = PDFPlumberLoader(file_path)
                docs = loader.load()
            elif ext == ".docx":
                loader = Docx2txtLoader(file_path)
                docs = loader.load()
            elif ext == ".html":
                loader = UnstructuredHTMLLoader(file_path)
                docs = loader.load()
            elif ext == ".json":
                # Assuming simple JSON loading for now
                loader = JSONLoader(file_path=file_path, jq_schema=".", text_content=False)
                docs = loader.load()
            elif ext == ".csv":
                loader = CSVLoader(file_path=file_path)
                docs = loader.load()
            else:
                raise ConfigurationError(f"Unsupported file extension for RAG ingestion: {ext}")
                
            # Append custom metadata to all extracted chunks/pages
            if metadata:
                for doc in docs:
                    doc.metadata.update(metadata)
                    
            return docs
            
        except Exception as e:
            logger.error("Failed to load document", file_path=file_path, error=str(e))
            raise e
