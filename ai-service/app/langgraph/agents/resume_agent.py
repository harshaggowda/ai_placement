from typing import Dict, Any
from langchain_core.messages import AIMessage

from app.langgraph.agents.base_agent import BaseAgent
from app.langgraph.state.graph_state import AgentState
from app.logging.logger import get_logger

logger = get_logger(__name__)

class ResumeAgent(BaseAgent):
    @property
    def name(self) -> str:
        return "resume_agent"
        
    @property
    def description(self) -> str:
        return "Expert at analyzing, reviewing, and improving resumes."
        
    @property
    def capabilities(self) -> list[str]:
        return ["resume_review", "resume_creation", "resume_formatting"]
        
    async def invoke(self, state: AgentState) -> Dict[str, Any]:
        logger.info("Executing Resume Agent")
        
        # In a real implementation, this would call an LLM with specific resume prompts.
        # For the orchestration framework, we just return a stubbed response.
        response_msg = AIMessage(content="[Resume Agent] I have reviewed your request regarding your resume.")
        
        return {
            "messages": [response_msg],
            "metadata": {"last_agent_executed": self.name}
        }
