from langgraph.graph import StateGraph, START, END

from app.langgraph.state.graph_state import AgentState
from app.langgraph.nodes.nodes import NODE_MAP
from app.langgraph.edges.routing import supervisor_router
from app.logging.logger import get_logger

logger = get_logger(__name__)

def build_multi_agent_graph() -> StateGraph:
    """
    Constructs and compiles the Multi-Agent LangGraph.
    """
    logger.info("Building Enterprise Multi-Agent LangGraph")
    
    # 1. Initialize State Graph
    workflow = StateGraph(AgentState)
    
    # 2. Add all nodes
    for name, func in NODE_MAP.items():
        workflow.add_node(name, func)
        
    # 3. Define Entry Point
    workflow.add_edge(START, "supervisor")
    
    # 4. Define Routing Edges from Supervisor
    # The supervisor router dynamically decides the next agent or END.
    agent_names = [n for n in NODE_MAP.keys() if n != "supervisor"]
    route_map = {name: name for name in agent_names}
    route_map[END] = END
    
    workflow.add_conditional_edges(
        "supervisor",
        supervisor_router,
        route_map
    )
    
    # 5. Define Return Edges
    # After any agent completes its task, it must return control to the supervisor
    for agent_name in agent_names:
        workflow.add_edge(agent_name, "supervisor")
        
    # 6. Compile Graph
    # In a fully productionized setup, we would inject a checkpointer here for memory persistence
    app = workflow.compile()
    
    return app

# Singleton compiled graph instance
orchestrator_graph = build_multi_agent_graph()
