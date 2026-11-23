# FlexFit Machine Learning Service

Python-based microservice for ML-driven workout recommendations, predictive health metrics, and Gemini AI assistant integration.

## Technology Stack
- **Language**: Python 3.12+
- **Framework**: FastAPI + Uvicorn
- **Machine Learning**: Scikit-learn, NumPy, Pandas (Scheduled for Phase 2)
- **AI Integration**: Gemini API via `google-genai` (Scheduled for Phase 2)

## Setup & Running

### 1. Create Virtual Environment
```bash
cd ml-service
python -m venv .venv
# Windows:
.venv\Scripts\activate
# Linux/macOS:
source .venv/bin/activate
```

### 2. Install Dependencies
```bash
pip install -r requirements.txt
```

### 3. Run Tests
```bash
pytest
```

### 4. Start the Service
```bash
uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```
Health endpoint will be accessible at: `http://localhost:8000/health`
Interactive Swagger API docs at: `http://localhost:8000/docs`
