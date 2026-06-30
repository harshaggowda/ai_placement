import pytest
from app.services.prompt_service import PromptService
from app.exceptions.errors import ConfigurationError
import os

@pytest.fixture
def prompt_service(tmp_path):
    # Create a temporary directory for prompts
    prompt_dir = tmp_path / "prompts"
    prompt_dir.mkdir()
    
    # Create a dummy template
    template_file = prompt_dir / "test_prompt.jinja2"
    template_file.write_text("Hello {{ name }}, your role is {{ role }}.")
    
    return PromptService(templates_dir=str(prompt_dir))

def test_render_success(prompt_service):
    rendered = prompt_service.render("test_prompt", name="Alice", role="Developer")
    assert rendered == "Hello Alice, your role is Developer."

def test_render_missing_variables(prompt_service):
    with pytest.raises(ConfigurationError) as exc:
        prompt_service.render("test_prompt", name="Alice")
    assert "Missing required variables" in str(exc.value)
