"""Vector-store abstraction — architecture placeholder.

Defines the provider-agnostic interface the RAG layer depends on, so the concrete backend
(pgvector, Pinecone, Chroma, OpenSearch, ...) can be chosen later without touching retrievers or
agents. No implementation and no embeddings here.
"""
from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any

from app.rag.collections import KnowledgeCollection


class VectorStore(ABC):
    """Minimal contract for a vector store backing the knowledge collections."""

    @abstractmethod
    async def upsert(self, collection: KnowledgeCollection, documents: list[dict[str, Any]]) -> None:
        """Index documents (with embeddings) into a collection. Deferred."""
        raise NotImplementedError

    @abstractmethod
    async def similarity_search(
        self, collection: KnowledgeCollection, query: str, k: int = 5
    ) -> list[dict[str, Any]]:
        """Return the top-k most relevant chunks for a query. Deferred."""
        raise NotImplementedError
