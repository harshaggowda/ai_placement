from typing import Dict, Any

from app.langgraph.state.graph_state import AgentState
from app.langgraph.supervisor.supervisor import Supervisor
from app.langgraph.agents.resume_agent import ResumeAgent
from app.langgraph.agents.rag_retrieval_agent import RAGRetrievalAgent
from app.langgraph.agents.mcp_tool_agent import MCPToolAgent
from app.langgraph.agents.domain_agents import (
    InterviewAgent, RoadmapAgent, DSAMentorAgent, SQLMentorAgent,
    BehavioralHRAgent, CompanyResearchAgent, ProgressAnalysisAgent
)

# Instantiate the objects once
supervisor_instance = Supervisor()

resume_agent_instance = ResumeAgent()
rag_agent_instance = RAGRetrievalAgent()
mcp_agent_instance = MCPToolAgent()
interview_agent_instance = InterviewAgent()
roadmap_agent_instance = RoadmapAgent()
dsa_mentor_agent_instance = DSAMentorAgent()
sql_mentor_agent_instance = SQLMentorAgent()
behavioral_hr_agent_instance = BehavioralHRAgent()
company_research_agent_instance = CompanyResearchAgent()
progress_analysis_agent_instance = ProgressAnalysisAgent()

# Node functions for the graph
async def supervisor_node(state: AgentState) -> Dict[str, Any]:
    return await supervisor_instance.invoke(state)

async def resume_agent_node(state: AgentState) -> Dict[str, Any]:
    return await resume_agent_instance.invoke(state)
    
async def rag_agent_node(state: AgentState) -> Dict[str, Any]:
    return await rag_agent_instance.invoke(state)
    
async def mcp_agent_node(state: AgentState) -> Dict[str, Any]:
    return await mcp_agent_instance.invoke(state)

async def interview_agent_node(state: AgentState) -> Dict[str, Any]:
    return await interview_agent_instance.invoke(state)

async def roadmap_agent_node(state: AgentState) -> Dict[str, Any]:
    return await roadmap_agent_instance.invoke(state)

async def dsa_mentor_agent_node(state: AgentState) -> Dict[str, Any]:
    return await dsa_mentor_agent_instance.invoke(state)

async def sql_mentor_agent_node(state: AgentState) -> Dict[str, Any]:
    return await sql_mentor_agent_instance.invoke(state)

async def behavioral_hr_agent_node(state: AgentState) -> Dict[str, Any]:
    return await behavioral_hr_agent_instance.invoke(state)

async def company_research_agent_node(state: AgentState) -> Dict[str, Any]:
    return await company_research_agent_instance.invoke(state)

async def progress_analysis_agent_node(state: AgentState) -> Dict[str, Any]:
    return await progress_analysis_agent_instance.invoke(state)

# Map node names to their functions for dynamic building
NODE_MAP = {
    "supervisor": supervisor_node,
    "resume_agent": resume_agent_node,
    "rag_agent": rag_agent_node,
    "mcp_agent": mcp_agent_node,
    "interview_agent": interview_agent_node,
    "roadmap_agent": roadmap_agent_node,
    "dsa_mentor_agent": dsa_mentor_agent_node,
    "sql_mentor_agent": sql_mentor_agent_node,
    "behavioral_hr_agent": behavioral_hr_agent_node,
    "company_research_agent": company_research_agent_node,
    "progress_analysis_agent": progress_analysis_agent_node
}
