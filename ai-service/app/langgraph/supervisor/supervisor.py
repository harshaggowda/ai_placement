import os
from typing import Dict, Any, List
from pydantic import BaseModel, Field
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.messages import SystemMessage
from langchain_openai import ChatOpenAI

from app.langgraph.state.graph_state import AgentState
from app.langgraph.agents import __all__ as ALL_AGENTS
from app.logging.logger import get_logger

logger = get_logger(__name__)

class RouteDecision(BaseModel):
    next_agent: str = Field(description="The name of the next agent to route to, or 'FINISH' if complete.")
    reasoning: str = Field(description="Reasoning for selecting this agent.")

class Supervisor:
    def __init__(self):
        # Retrieve all agent names dynamically from __init__.py exports (excluding BaseAgent)
        self.available_agents = [a for a in ALL_AGENTS if a != "BaseAgent"]
        # Convert class names to snake_case names for routing (e.g., ResumeAgent -> resume_agent)
        # For simplicity, we hardcode the known routing names
        self.agent_names = [
            "resume_agent", "rag_agent", "mcp_agent", "interview_agent",
            "roadmap_agent", "dsa_mentor_agent", "sql_mentor_agent",
            "behavioral_hr_agent", "company_research_agent", "progress_analysis_agent"
        ]
        
        self.system_prompt = (
            "You are a Supervisor in a Multi-Agent AI System.\n"
            "Your job is to read the conversation and route the user's request to the correct specialized agent.\n"
            "If the request is fulfilled, route to 'FINISH'.\n"
            "Available agents: {agents}\n"
        )
        
        # We assume OPENAI_API_KEY is available in the environment
        # Fallback to a mock key if running in tests
        self.llm = ChatOpenAI(model="gpt-4o-mini", api_key=os.getenv("OPENAI_API_KEY", "mock"))
        
    async def invoke(self, state: AgentState) -> Dict[str, Any]:
        logger.info("Executing Supervisor")
        
        messages = state.get("messages", [])
        if not messages:
            return {"next_agent": "FINISH"}
            
        prompt = ChatPromptTemplate.from_messages([
            ("system", self.system_prompt),
            ("placeholder", "{messages}")
        ])
        
        # For production orchestration we use structured output
        chain = prompt | self.llm.with_structured_output(RouteDecision)
        
        try:
            decision = await chain.ainvoke({
                "agents": ", ".join(self.agent_names),
                "messages": messages
            })
            next_agent = decision.next_agent
            logger.info("Supervisor routing decision", next_agent=next_agent, reasoning=decision.reasoning)
        except Exception as e:
            logger.error("Supervisor LLM call failed", exc_info=True)
            # Fallback for testing/offline mode
            next_agent = self._fallback_routing(messages)
            
        if next_agent not in self.agent_names and next_agent != "FINISH":
            logger.warning(f"Invalid agent selected: {next_agent}, defaulting to FINISH")
            next_agent = "FINISH"
            
        return {"next_agent": next_agent}

    def _fallback_routing(self, messages: List[Any]) -> str:
        """Fallback routing logic for tests without OpenAI API keys."""
        last_msg = str(messages[-1].content).lower() if messages else ""
        if "resume" in last_msg: return "resume_agent"
        if "interview" in last_msg: return "interview_agent"
        if "rag" in last_msg or "document" in last_msg: return "rag_agent"
        if "mcp" in last_msg or "tool" in last_msg: return "mcp_agent"
        return "FINISH"
