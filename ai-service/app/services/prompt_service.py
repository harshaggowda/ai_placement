import os
from jinja2 import Environment, FileSystemLoader, select_autoescape, meta
from app.logging.logger import get_logger
from app.exceptions.errors import ConfigurationError

logger = get_logger(__name__)

class PromptService:
    """
    Loads and renders Jinja2 prompt templates securely.
    """
    def __init__(self, templates_dir: str = "app/prompts"):
        if not os.path.exists(templates_dir):
            logger.warning("Prompts directory not found", path=templates_dir)
            # fallback to create it or just warn, it might not exist yet during tests
            os.makedirs(templates_dir, exist_ok=True)
            
        self.env = Environment(
            loader=FileSystemLoader(templates_dir),
            autoescape=select_autoescape(),
            trim_blocks=True,
            lstrip_blocks=True
        )

    def render(self, template_name: str, **kwargs) -> str:
        """
        Renders a specific prompt template with the provided context variables.
        """
        try:
            template = self.env.get_template(f"{template_name}.jinja2")
            
            # Validate that we passed all required variables
            ast = self.env.parse(open(template.filename).read())
            required_vars = meta.find_undeclared_variables(ast)
            
            missing_vars = [var for var in required_vars if var not in kwargs]
            if missing_vars:
                logger.error("Missing template variables", template=template_name, missing=missing_vars)
                raise ValueError(f"Missing required variables for prompt '{template_name}': {missing_vars}")
                
            rendered = template.render(**kwargs)
            logger.debug("Rendered prompt", template=template_name)
            return rendered
            
        except Exception as e:
            logger.error("Failed to render prompt", template=template_name, error=str(e))
            raise ConfigurationError(f"Failed to render prompt template '{template_name}': {str(e)}")
