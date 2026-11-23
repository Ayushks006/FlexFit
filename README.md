# FlexFit • Next-Generation AI Fitness Application

**FlexFit** is an AI-powered fitness desktop application providing personalized workout plans, diet recommendations, scheduling, tracking, daily challenges, points, achievements, and competitive gaming-inspired leaderboards.

---

## Architecture Overview (Phase 1)

```
FlexFit/
├── .gitignore
├── README.md
├── pom.xml                               # Root Multi-Module Maven Parent (Java 21)
├── mvnw / mvnw.cmd                       # Maven 3.9+ Wrapper
├── .mvn/wrapper/
│   └── maven-wrapper.properties
├── backend/                              # Spring Boot REST API & Business Logic
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/flexfit/backend/
│       │   │   ├── FlexFitBackendApplication.java
│       │   │   ├── config/
│       │   │   │   └── CorsConfig.java
│       │   │   ├── controller/
│       │   │   │   └── HealthController.java
│       │   │   ├── dto/
│       │   │   │   └── HealthResponse.java
│       │   │   └── service/
│       │   │       └── HealthService.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/java/com/flexfit/backend/
│           └── HealthControllerTest.java
├── desktop-app/                          # JavaFX Desktop Application & User Interface
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/flexfit/desktop/
│       │   │   ├── FlexFitDesktopApp.java
│       │   │   ├── controller/
│       │   │   │   └── WelcomeController.java
│       │   │   ├── model/
│       │   │   │   └── BackendHealth.java
│       │   │   └── service/
│       │   │       └── BackendClient.java
│       │   └── resources/com/flexfit/desktop/
│       │       ├── css/
│       │       │   └── dark-theme.css    # Dark charcoal & Electric Blue theme
│       │       └── views/
│       │           └── welcome.fxml      # Welcome Dashboard View
│       └── test/java/com/flexfit/desktop/
│           └── BackendClientTest.java
└── ml-service/                           # Python Machine Learning Microservice
    ├── requirements.txt
    ├── app.py                            # FastAPI Service & Model Metadata
    ├── pytest.ini
    ├── README.md
    └── tests/
        ├── conftest.py                   # Pytest execution timeout watchdog
        └── test_ml_service.py            # Unit & Integration Tests
```

---

## Visual Identity & Design System

The application strictly implements the FlexFit design direction:
- **Base Palette**: Obsidian & Deep Charcoal (`#0A0D14`, `#121620`, `#1E2638`).
- **Accent Palette**: Neon Electric Blue (`#00E5FF`, `#00B0FF`, `#2979FF`).
- **Interactive UI Cards**: Modern cards with subtle glowing borders, drop-shadow elevations, and hover transitions.
- **Biometric Progress Indicators**: High-tech circular progress rings showing daily target completion (78%), active calories, duration, and hydration.
- **Leaderboard**: Gaming-inspired rankings with player tiers (Diamond Rank #4), XP progress bars, and glowing status pills.
- **REST Status Indicator**: Real-time asynchronous status badge reflecting Spring Boot backend connectivity with ping latency diagnostics.

---

## Environment Prerequisites & Installation Guide

### Inspection Summary

| Component | Required Version | Local Status | Action Needed |
| :--- | :--- | :--- | :--- |
| **Java** | 21 LTS | Not in PATH | Install OpenJDK / Eclipse Temurin 21 |
| **Maven** | 3.9+ | Managed via `mvnw` | Wrapper included in repo |
| **Python** | 3.12+ | Installed (3.12.10) | Ready |
| **MySQL** | 8.0+ | Not in PATH | Install MySQL Community Server |

### How to Install Missing Tools

#### 1. Java 21 LTS (OpenJDK / Eclipse Temurin)
- **Option A (Manual)**: Download Eclipse Temurin 21 (LTS) from [adoptium.net](https://adoptium.net/) and run the installer. Ensure "Set JAVA_HOME variable" and "Add to PATH" are checked.
- **Option B (Windows Command)**:
  ```powershell
  winget install EclipseAdoptium.Temurin.21.JDK
  ```
- **Verify**:
  ```powershell
  java -version
  ```

#### 2. MySQL Community Server 8.0+
- **Option A (Manual)**: Download the MySQL Community Installer from [dev.mysql.com/downloads/installer/](https://dev.mysql.com/downloads/installer/).
- **Option B (Windows Command)**:
  ```powershell
  winget install Oracle.MySQL
  ```
- **Database Initialization**:
  ```sql
  CREATE DATABASE IF NOT EXISTS flexfit_db;
  ```

---

## Configuration & Environment Variables

FlexFit strictly enforces security by avoiding hardcoded credentials. All sensitive parameters are resolved through environment variables with sensible local defaults:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Spring Boot backend HTTP port |
| `DB_HOST` | `localhost` | MySQL host address |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `flexfit_db` | MySQL database name |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | *(empty string)* | Database password |
| `BACKEND_URL` | `http://localhost:8080` | Desktop app target backend URL |
| `ML_SERVICE_URL`| `http://localhost:8000` | Target URL for ML microservice |

---

## Instructions to Run Each Component

### 1. Spring Boot Backend
Ensure JDK 21 is installed. From the project root:
```bash
# Using the included Maven wrapper:
./mvnw clean spring-boot:run -pl backend

# Or on Windows Command Prompt:
mvnw.cmd clean spring-boot:run -pl backend
```
- **Health Endpoint**: `http://localhost:8080/api/v1/health`
- **Ping Endpoint**: `http://localhost:8080/api/v1/ping`

### 2. JavaFX Desktop Application
From the project root:
```bash
./mvnw javafx:run -pl desktop-app

# Or on Windows Command Prompt:
mvnw.cmd javafx:run -pl desktop-app
```
The desktop application opens with the FlexFit welcome dashboard, connects asynchronously to the backend REST API, and displays live status and diagnostics.

### 3. Python ML Service
```bash
cd ml-service
# Run test suite:
python -m pytest

# Start development server:
python -m uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```
- **Health Endpoint**: `http://localhost:8000/health`
- **Interactive Swagger Docs**: `http://localhost:8000/docs`
