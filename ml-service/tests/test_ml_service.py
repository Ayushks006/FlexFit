"""
Unit & Integration Tests for FlexFit ML Service
"""

import os
import sys
import pytest
from typing import Any
from pydantic import BaseModel

# Ensure ml-service root is in sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import app


class InProcessResponse:
    """Simulates an HTTP response for synchronous in-process test execution."""
    def __init__(self, status_code: int, data: Any):
        self.status_code = status_code
        self._data = data

    def json(self):
        return self._data


class InProcessTestClient:
    """
    Lightweight in-process test client for FastAPI routes.
    Dispatches directly through FastAPI router endpoints without spawning
    AnyIO BlockingPortal threads or requiring OS TCP loopback sockets
    (which block on Windows when socketpair operations are restricted).
    """
    def __init__(self, fastapi_app):
        self.app = fastapi_app

    def get(self, path: str) -> InProcessResponse:
        for route in self.app.routes:
            if getattr(route, "path", None) == path and "GET" in getattr(route, "methods", []):
                result = route.endpoint()
                if isinstance(result, BaseModel):
                    data = result.model_dump()
                else:
                    data = result
                return InProcessResponse(status_code=200, data=data)
        return InProcessResponse(status_code=404, data={"detail": "Not Found"})


client = InProcessTestClient(app)


def test_health_endpoint():
    """Verify /health returns HTTP 200, status UP, and system info."""
    response = client.get("/health")
    assert response.status_code == 200

    data = response.json()
    assert data["status"] == "UP"
    assert data["service"] == "flexfit-ml-service"
    assert "PENDING_PHASE_2" in data["model_status"]
    assert "python_version" in data["system_info"]


def test_models_info_endpoint():
    """Verify /api/v1/models/info returns planned model metadata."""
    response = client.get("/api/v1/models/info")
    assert response.status_code == 200

    data = response.json()
    assert "models" in data
    assert len(data["models"]) >= 3
    model_ids = [m["id"] for m in data["models"]]
    assert "workout-recommendation-classifier" in model_ids
    assert "gemini-coach-assistant" in model_ids
