from typing import Dict, Any
from langchain_core.messages import AIMessage

from app.langgraph.agents.base_agent import BaseAgent
from app.langgraph.state.graph_state import AgentState

def _create_mock_response(agent_name: str) -> Dict[str, Any]:
    return {
        "messages": [AIMessage(content=f"[{agent_name}] Processed request.")],
        "metadata": {"last_agent_executed": agent_name}
    }

class InterviewAgent(BaseAgent):
    @property
    def name(self) -> str: return "interview_agent"
    @property
    def description(self) -> str: return "Conducts mock interviews and provides feedback."
    async def invoke(self, state: AgentState) -> Dict[str, Any]: return _create_mock_response(self.name)

class RoadmapAgent(BaseAgent):
    @property
    def name(self) -> str: return "roadmap_agent"
    @property
    def description(self) -> str: return "Generates personalized career roadmaps."
    async def invoke(self, state: AgentState) -> Dict[str, Any]: return _create_mock_response(self.name)

class DSAMentorAgent(BaseAgent):
    @property
    def name(self) -> str: return "dsa_mentor_agent"
    @property
    def description(self) -> str: return "Helps with Data Structures and Algorithms."
    async def invoke(self, state: AgentState) -> Dict[str, Any]: return _create_mock_response(self.name)

class SQLMentorAgent(BaseAgent):
    @property
    def name(self) -> str: return "sql_mentor_agent"
    @property
    def description(self) -> str: return "Helps with SQL and Database questions."
    async def invoke(self, state: AgentState) -> Dict[str, Any]: return _create_mock_response(self.name)

class BehavioralHRAgent(BaseAgent):
    @property
    def name(self) -> str: return "behavioral_hr_agent"
    @property
    def description(self) -> str: return "Simulates behavioral and HR interview rounds."
    async def invoke(self, state: AgentState) -> Dict[str, Any]: return _create_mock_response(self.name)

class CompanyResearchAgent(BaseAgent):
    @property
    def name(self) -> str: return "company_research_agent"
    @property
    def description(self) -> str: return "Provides company profiles and hiring patterns."
    async def invoke(self, state: AgentState) -> Dict[str, Any]: return _create_mock_response(self.name)

class ProgressAnalysisAgent(BaseAgent):
    @property
    def name(self) -> str: return "progress_analysis_agent"
    @property
    def description(self) -> str: return "Analyzes user statistics and study sessions."
    async def invoke(self, state: AgentState) -> Dict[str, Any]: return _create_mock_response(self.name)
