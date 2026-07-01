from app.mcp.tools.web_search import WebSearchTool
from app.mcp.tools.github_tool import GitHubTool
from app.mcp.tools.postgres_tool import PostgreSQLTool
from app.mcp.tools.resume_tool import ResumeTool
from app.mcp.tools.user_progress import UserProgressTool
from app.mcp.tools.company_knowledge import CompanyKnowledgeTool
from app.mcp.registry.tool_registry import registry

# Automatically register all initial tools
registry.register(WebSearchTool())
registry.register(GitHubTool())
registry.register(PostgreSQLTool())
registry.register(ResumeTool())
registry.register(UserProgressTool())
registry.register(CompanyKnowledgeTool())

__all__ = [
    "WebSearchTool",
    "GitHubTool",
    "PostgreSQLTool",
    "ResumeTool",
    "UserProgressTool",
    "CompanyKnowledgeTool"
]
