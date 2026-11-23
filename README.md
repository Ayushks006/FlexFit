# FlexFit • Next-Generation AI Fitness Application

**FlexFit** is an AI-powered fitness desktop application providing personalized workout plans, diet recommendations, scheduling, tracking, daily challenges, points, achievements, and competitive gaming-inspired leaderboards.

---

## Architecture Overview (Phase 1 & Phase 2)

```
FlexFit/
├── .gitignore
├── README.md
├── pom.xml                               # Root Multi-Module Maven Parent (Java 21)
├── mvnw / mvnw.cmd                       # Maven 3.9+ Wrapper
├── .mvn/wrapper/
│   └── maven-wrapper.properties
├── backend/                              # Spring Boot REST API, Security & Profile Logic
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/flexfit/backend/
│       │   │   ├── FlexFitBackendApplication.java
│       │   │   ├── config/CorsConfig.java
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── HealthController.java
│       │   │   │   └── ProfileController.java
│       │   │   ├── dto/
│       │   │   │   ├── request/ (RegisterRequest, LoginRequest, ProfileUpdateRequest)
│       │   │   │   └── response/ (ApiResponse, AuthResponse, UserDto, FitnessProfileDto)
│       │   │   ├── entity/ (User, FitnessProfile)
│       │   │   ├── exception/ (GlobalExceptionHandler, DuplicateResourceException, etc.)
│       │   │   ├── repository/ (UserRepository, FitnessProfileRepository)
│       │   │   ├── security/ (SecurityConfig, JwtTokenProvider, JwtAuthFilter, UserPrincipal)
│       │   │   └── service/ (AuthService, ProfileService, HealthService)
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       │           └── V1__init_auth_and_profile_schema.sql
│       └── test/
│           ├── java/com/flexfit/backend/
│           │   ├── AuthControllerTest.java
│           │   ├── AuthServiceTest.java
│           │   ├── HealthControllerTest.java
│           │   ├── JwtTokenProviderTest.java
│           │   ├── PersistenceTest.java
│           │   ├── ProfileControllerTest.java
│           │   └── ProfileServiceTest.java
│           └── resources/application-test.yml
├── desktop-app/                          # JavaFX Desktop Application & User Interface
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/flexfit/desktop/
│       │   │   ├── FlexFitDesktopApp.java
│       │   │   ├── controller/
│       │   │   │   ├── WelcomeController.java
│       │   │   │   ├── LoginController.java
│       │   │   │   ├── RegistrationController.java
│       │   │   │   ├── RegistrationSuccessController.java
│       │   │   │   ├── DashboardController.java
│       │   │   │   ├── ProfileController.java
│       │   │   │   ├── EditProfileController.java
│       │   │   │   ├── SettingsController.java
│       │   │   │   └── PlaceholderController.java
│       │   │   ├── model/ (UserSession, FitnessProfileModel, RegistrationData, BackendHealth)
│       │   │   └── service/ (BackendClient, SessionManager, ScreenManager)
│       │   └── resources/com/flexfit/desktop/
│       │       ├── css/dark-theme.css    # Electric Blue & Charcoal Theme
│       │       └── views/
│       │           ├── welcome.fxml
│       │           ├── login.fxml
│       │           ├── registration.fxml
│       │           ├── registration_success.fxml
│       │           ├── dashboard.fxml
│       │           ├── my_profile.fxml
│       │           ├── edit_profile.fxml
│       │           ├── settings.fxml
│       │           └── placeholder.fxml
│       └── test/java/com/flexfit/desktop/
│           ├── BackendClientTest.java
│           ├── DesktopBackendClientAuthTest.java
│           ├── RegistrationValidationTest.java
│           └── SessionManagerTest.java
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
- **Biometric Progress Indicators**: Circular progress rings showing daily target completion, active calories, duration, and hydration.
- **Form Controls & Modals**: Dark charcoal inputs with electric-blue focus rings, password visibility toggles, step-by-step progress bars, and alert banners.
- **Authenticated Dashboard**: Personalized greeting, live biometric summary, dynamic profile completion indicator, and responsive sidebar navigation.
- **Polished Module Placeholders**: Elegant coming-soon placeholders for modules scheduled in subsequent phases (AI Coach, Workout Planner, Diet Planner, Challenges, Leaderboard, Progress Tracker, Achievements) without synthetic data or fake rankings.

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

#### 1. Java 21 LTS (Eclipse Temurin / OpenJDK)
- **Option A (Manual)**: Download Eclipse Temurin 21 (LTS) from [adoptium.net](https://adoptium.net/) and run the installer. Ensure "Set JAVA_HOME variable" and "Add to PATH" are checked.
- **Option B (Windows Command)**:
  ```powershell
  winget install EclipseAdoptium.Temurin.21.JDK
  ```

#### 2. MySQL Community Server 8.0+
- **Option A (Manual)**: Download the MySQL Community Installer from [dev.mysql.com/downloads/installer/](https://dev.mysql.com/downloads/installer/).
- **Option B (Windows Command)**:
  ```powershell
  winget install Oracle.MySQL
  ```
- **Database Setup**:
  ```sql
  CREATE DATABASE IF NOT EXISTS flexfit_db;
  CREATE USER IF NOT EXISTS 'flexfit_user'@'localhost' IDENTIFIED BY 'your_password';
  GRANT ALL PRIVILEGES ON flexfit_db.* TO 'flexfit_user'@'localhost';
  FLUSH PRIVILEGES;
  ```

---

## Configuration & Environment Variables

FlexFit enforces security by avoiding hardcoded credentials. All sensitive parameters are resolved through environment variables:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Spring Boot backend HTTP port |
| `DB_HOST` | `localhost` | MySQL host address |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `flexfit_db` | MySQL database name |
| `DB_USER` | `flexfit_user` | Database username |
| `DB_PASSWORD` | *(empty string)* | Database password |
| `JWT_SECRET` | *(secure 256-bit dev default)* | Secret key for signing HMAC-SHA256 JWTs |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | JWT access token validity duration |
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
- **Register Endpoint**: `POST http://localhost:8080/api/v1/auth/register`
- **Login Endpoint**: `POST http://localhost:8080/api/v1/auth/login`
- **Profile Endpoint**: `GET / PUT http://localhost:8080/api/v1/users/me/profile`

### 2. JavaFX Desktop Application
From the project root:
```bash
./mvnw javafx:run -pl desktop-app

# Or on Windows Command Prompt:
mvnw.cmd javafx:run -pl desktop-app
```
The desktop application opens with the FlexFit welcome dashboard, providing options to sign in, create a 6-step fitness profile, view the dashboard, update profile details, and adjust settings.

### 3. Python ML Service
```bash
cd ml-service
python -m pytest
python -m uvicorn app:app --host 0.0.0.0 --port 8000 --reload
```
- **Health Endpoint**: `http://localhost:8000/health`
- **Interactive Swagger Docs**: `http://localhost:8000/docs`
