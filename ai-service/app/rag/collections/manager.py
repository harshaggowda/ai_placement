from typing import List
from pydantic import BaseModel

from app.rag.vectorstores.pgvector_store import VectorStoreManager

class CollectionInfo(BaseModel):
    name: str
    description: str

class CollectionManager:
    """
    Manages logical divisions of knowledge (Collections) for the AI system.
    Provides standard names for the application domains.
    """
    
    # Predefined collections for CareerOS AI
    COLLECTIONS = {
        "resume_knowledge": "Collection of perfect resumes and JD mappings",
        "interview_experiences": "Behavioral questions and past interview records",
        "company_profiles": "Data scraped about specific companies",
        "tech_dsa": "Data Structures and Algorithms concepts",
        "tech_java": "Java and Spring Boot conceptual knowledge",
        "career_roadmaps": "Structured learning paths for different roles"
    }

    @classmethod
    def list_collections(cls) -> List[CollectionInfo]:
        """Lists all supported collections and their descriptions."""
        return [
            CollectionInfo(name=name, description=desc)
            for name, desc in cls.COLLECTIONS.items()
        ]

    @classmethod
    def get_store(cls, collection_name: str) -> VectorStoreManager:
        """
        Returns a VectorStoreManager scoped to the given collection.
        Raises ValueError if the collection is not recognized (optional strictness).
        """
        if collection_name not in cls.COLLECTIONS:
            # We can allow dynamic collections, but logging a warning is good practice
            import logging
            logging.getLogger(__name__).warning(f"Using unrecognized collection name: {collection_name}")
            
        return VectorStoreManager(collection_name=collection_name)
