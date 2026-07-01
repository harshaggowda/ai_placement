from typing import Dict, Any
from langchain_core.messages import AIMessage, ToolMessage

from app.langgraph.agents.base_agent import BaseAgent
from app.langgraph.state.graph_state import AgentState
from app.rag.pipelines.rag_pipeline import RAGPipeline
from app.logging.logger import get_logger

logger = get_logger(__name__)

class RAGRetrievalAgent(BaseAgent):
    @property
    def name(self) -> str:
        return "rag_agent"
        
    @property
    def description(self) -> str:
        return "Retrieves internal placement documents, guidelines, and career knowledge."
        
    @property
    def capabilities(self) -> list[str]:
        return ["document_search", "guideline_retrieval"]
        
    async def invoke(self, state: AgentState) -> Dict[str, Any]:
        logger.info("Executing RAG Agent")
        
        # In a real workflow, the supervisor passes the specific query inside the state
        # For now we'll just extract the last message text as the query
        last_message = state["messages"][-1]
        query = last_message.content if hasattr(last_message, "content") else str(last_message)
        
        try:
            # Delegate to Phase 3 Enterprise RAG Engine
            context = await RAGPipeline.retrieve_context(
                query=query,
                collection_name="career_knowledge",
                k=3
            )
            response_text = context
        except Exception as e:
            logger.error("RAG execution failed", exc_info=True)
            response_text = f"Error retrieving knowledge: {str(e)}"
        
        response_msg = AIMessage(content=f"[RAG Knowledge] {response_text}")
        
        return {
            "messages": [response_msg],
            "metadata": {"last_agent_executed": self.name}
        }
