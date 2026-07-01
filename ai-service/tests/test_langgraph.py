import pytest
from langchain_core.messages import HumanMessage, AIMessage
from langgraph.graph import END

from app.langgraph.state.graph_state import AgentState
from app.langgraph.supervisor.supervisor import Supervisor
from app.langgraph.edges.routing import supervisor_router
from app.langgraph.execution.engine import ExecutionEngine
from app.langgraph.graphs.builder import orchestrator_graph
from app.langgraph.agents import ResumeAgent, DSAMentorAgent

def test_supervisor_fallback_routing():
    supervisor = Supervisor()
    
    # Test routing to resume agent
    decision = supervisor._fallback_routing([HumanMessage(content="Please review my resume")])
    assert decision == "resume_agent"
    
    # Test routing to interview agent
    decision = supervisor._fallback_routing([HumanMessage(content="I need interview prep")])
    assert decision == "interview_agent"
    
    # Test routing to FINISH
    decision = supervisor._fallback_routing([HumanMessage(content="Hello world")])
    assert decision == "FINISH"

def test_supervisor_router_edge():
    # Route to END
    state: AgentState = {"messages": [], "next_agent": "FINISH", "user_context": {}, "metadata": {}}
    assert supervisor_router(state) == END
    
    # Route to a specific agent
    state["next_agent"] = "resume_agent"
    assert supervisor_router(state) == "resume_agent"

@pytest.mark.asyncio
async def test_agent_invoke():
    agent = ResumeAgent()
    state: AgentState = {"messages": [HumanMessage(content="Review this")], "next_agent": "resume_agent", "user_context": {}, "metadata": {}}
    
    result = await agent.invoke(state)
    assert "messages" in result
    assert len(result["messages"]) == 1
    assert isinstance(result["messages"][0], AIMessage)
    assert result["metadata"]["last_agent_executed"] == "resume_agent"

@pytest.mark.asyncio
async def test_graph_execution_engine():
    # This acts as an integration test for the entire graph
    # We use a mocked LLM setup if API keys are absent, but for the fallback logic it will hit "FINISH"
    # Wait, the ExecutionEngine will invoke the graph. If it hits FINISH immediately, it returns the user message? No, if it hits FINISH, there are no AIMessages.
    
    # We test it with a query that triggers the resume agent fallback routing.
    # We must patch the Supervisor LLM invocation to just use the fallback for deterministic tests.
    with pytest.MonkeyPatch.context() as m:
        # Patch the supervisor's invoke to just use fallback to avoid OpenAI calls during tests
        invocation_count = {"count": 0}
        
        async def mock_supervisor_invoke(state):
            invocation_count["count"] += 1
            if invocation_count["count"] > 1:
                return {"next_agent": "FINISH"}
                
            from app.langgraph.supervisor.supervisor import Supervisor
            s = Supervisor()
            next_agent = s._fallback_routing(state.get("messages", []))
            return {"next_agent": next_agent}
            
        m.setattr("app.langgraph.nodes.nodes.supervisor_instance.invoke", mock_supervisor_invoke)
        
        # Test 1: Resume routing
        result = await ExecutionEngine.run("Please review my resume")
        assert result["status"] == "success"
        assert "I have reviewed your request regarding your resume" in result["response"]
        
        # Test 2: Unmatched routing -> FINISH
        invocation_count["count"] = 0
        result_finish = await ExecutionEngine.run("Hello there")
        assert result_finish["status"] == "success"
        assert result_finish["response"] == "" # Because no agent was executed, only supervisor ran and routed to END
