from typing import Any, List, Optional
from langchain_core.documents import Document
from app.rag.collections.manager import CollectionManager

class BaseRetrieverWrapper:
    """
    A wrapper around Langchain's base retriever that integrates with our CollectionManager.
    This simplifies getting a fully configured retriever for LangGraph/Agents.
    """
    
    def __init__(self, collection_name: str, k: int = 4, filter: Optional[dict] = None):
        self.collection_name = collection_name
        self.k = k
        self.filter = filter
        self._store = CollectionManager.get_store(collection_name)
        self.retriever = self._store.get_retriever(k=k, filter=filter)

    async def aget_relevant_documents(self, query: str) -> List[Document]:
        """Async retrieval wrapper."""
        # Use underlying Langchain retriever interface
        return await self.retriever.ainvoke(query)

    def get_relevant_documents(self, query: str) -> List[Document]:
        """Sync retrieval wrapper."""
        return self.retriever.invoke(query)
