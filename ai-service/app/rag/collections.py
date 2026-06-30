"""Knowledge collections for Retrieval-Augmented Generation.

Defines the *taxonomy* of the RAG knowledge base. Each collection is an isolated namespace in the
(future) vector store. No documents are ingested and no embeddings are produced yet — this only
fixes the contract so ingestion and retrieval can be built against stable names.
"""
from __future__ import annotations

from enum import Enum


class KnowledgeCollection(str, Enum):
    """Canonical RAG collections. Values are stable namespace keys for the vector store."""

    JAVA = "java"
    SPRING_BOOT = "spring_boot"
    AWS = "aws"
    SYSTEM_DESIGN = "system_design"
    DSA = "dsa"
    SQL = "sql"
    INTERVIEW_EXPERIENCES = "interview_experiences"
    RESUME_KNOWLEDGE = "resume_knowledge"
    BEHAVIORAL_QUESTIONS = "behavioral_questions"
