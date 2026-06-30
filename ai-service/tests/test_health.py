import pytest
from fastapi.testclient import TestClient
import os
from unittest.mock import patch

# Set required environment variables for settings before importing app
os.environ["API_KEY"] = "test-api-key"
os.environ["JWT_SECRET"] = "test-jwt-secret"
os.environ["DATABASE_URL"] = "postgres://test"
os.environ["REDIS_URL"] = "redis://test"
os.environ["OPENAI_API_KEY"] = "sk-test"

from app.main import app

client = TestClient(app)

def test_health_endpoint():
    response = client.get("/api/v1/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"

def test_readiness_endpoint():
    response = client.get("/api/v1/ready")
    assert response.status_code == 200
    assert response.json()["status"] == "ready"

def test_liveness_endpoint():
    response = client.get("/api/v1/live")
    assert response.status_code == 200
    assert response.json()["status"] == "alive"
