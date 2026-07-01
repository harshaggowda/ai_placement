from typing import Dict, Any, List, Optional
from pydantic import BaseModel, Field

class ToolParameter(BaseModel):
    type: str = Field(..., description="The type of the parameter, e.g., 'string', 'number', 'boolean', 'object'")
    description: str = Field(..., description="A description of what the parameter does.")
    default: Optional[Any] = None
    required: bool = True
    enum: Optional[List[Any]] = None

class ToolInputSchema(BaseModel):
    type: str = "object"
    properties: Dict[str, Any] = Field(default_factory=dict, description="Dictionary mapping parameter name to its JSON schema")
    required: List[str] = Field(default_factory=list, description="List of required parameter names")

class ToolMetadata(BaseModel):
    name: str = Field(..., description="Unique name of the tool")
    description: str = Field(..., description="Description of what the tool does")
    input_schema: ToolInputSchema = Field(..., description="The expected input parameters defined as a JSON schema")
    version: str = Field(default="1.0.0", description="Version of the tool")
    category: str = Field(default="general", description="Category of the tool")
    requires_auth: bool = Field(default=False, description="Whether this tool requires an authenticated user context")

class ExecutionResult(BaseModel):
    status: str = Field(..., description="Execution status: 'success' or 'error'")
    data: Optional[Any] = Field(default=None, description="The output data if successful")
    error: Optional[str] = Field(default=None, description="The error message if failed")
    execution_time_ms: float = Field(default=0.0, description="Time taken to execute in milliseconds")
    metadata: Dict[str, Any] = Field(default_factory=dict, description="Any additional execution metadata")

class ErrorResponse(BaseModel):
    error: str
    details: Optional[str] = None
