"""
FlexFit Machine Learning Service (FastAPI)
Phase 1: Foundation & Health Diagnostic Service
"""

import os
import sys
import platform
from datetime import datetime, timezone
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, Any

app = FastAPI(
    title="FlexFit Machine Learning Service",
    description="Python microservice providing ML workout recommendations, predictive health analytics, and AI assistance.",
    version="1.0.0-SNAPSHOT"
)

# Configure CORS origins via environment variable (no wildcard with credentials)
_raw_origins = os.getenv("ML_CORS_ALLOWED_ORIGINS", "http://localhost:8080,http://127.0.0.1:8080")
ALLOWED_ORIGINS = [
    origin.strip()
    for origin in _raw_origins.split(",")
    if origin.strip() and origin.strip() != "*"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=False,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type"],
)


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
    timestamp: str
    model_status: str
    system_info: Dict[str, Any]


@app.get("/health", response_model=HealthResponse)
def get_health():
    """
    Health check endpoint reporting ML microservice operational status.
    """
    return HealthResponse(
        status="UP",
        service="flexfit-ml-service",
        version="1.0.0-SNAPSHOT",
        timestamp=datetime.now(timezone.utc).isoformat(),
        model_status="PENDING_PHASE_2 (Architecture Ready, No Training in Phase 1)",
        system_info={
            "python_version": sys.version.split()[0],
            "platform": platform.platform(),
            "scikit_learn_ready": True
        }
    )


@app.get("/api/v1/models/info")
def get_models_info():
    """
    Returns planned ML model architecture and metadata for subsequent phases.
    """
    return {
        "models": [
            {
                "id": "workout-recommendation-classifier",
                "framework": "scikit-learn",
                "status": "SCHEDULED_PHASE_2",
                "description": "Multi-output classifier for personalized workout routines"
            },
            {
                "id": "calorie-expenditure-regressor",
                "framework": "scikit-learn",
                "status": "SCHEDULED_PHASE_2",
                "description": "Biometric regression model predicting calorie burn"
            },
            {
                "id": "gemini-coach-assistant",
                "framework": "google-genai",
                "status": "SCHEDULED_PHASE_2",
                "description": "Multimodal conversational AI fitness coach"
            }
        ]
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
