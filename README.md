# FlexFit — Intelligent Fitness & Wellness Platform

**FlexFit** is a full-stack fitness intelligence platform built with **Java 21**, **Spring Boot 3.3.4**, **Thymeleaf**, **MySQL**, **Flyway**, **JavaFX**, and a companion **Python FastAPI ML microservice**. It unifies biometric onboarding, rule-based workout programming, Mifflin-St Jeor nutrition planning, and database-backed challenges and leaderboards.

---

## Key Features

- **Authentication & Dual-Mode Security**:
  - Multi-step (6-stage) user & biometric registration.
  - BCrypt password hashing and JWT authentication.
  - Web UI authenticated via `HttpOnly`, `SameSite=Lax` cookies with CSRF protection.
  - Stateless `Authorization: Bearer <token>` REST API for desktop and external clients.
- **Biometric Profile & Analytics Dashboard**:
  - Real-time BMI calculation, target weight delta, and 16-field profile completion tracking.
  - Live workout, nutrition, and challenge summaries backed by MySQL.
- **Workout Intelligence (`/workouts`)**:
  - Deterministic, profile-aware workout recommendations tailored to fitness goal, experience level, location (Home/Gym/Hybrid), available equipment, and injury/limitation notes.
  - Session logging, approximate MET-based calorie estimation, 7-day activity chart, and ownership-protected history.
- **Nutrition Intelligence (`/nutrition`)**:
  - Mifflin-St Jeor BMR and goal-adjusted daily calorie/macronutrient targets with activity assumptions and safety floors.
  - Meal logging by category (Breakfast, Lunch, Dinner, Snacks), date-filtered history, and rule-based food suggestions respecting dietary preferences and allergies.
- **Challenges & Leaderboard (`/challenges`, `/leaderboard`)**:
  - Active challenge catalog with automatic progress sync from real workout and nutrition logs.
  - Idempotent point awarding and Global, State, and District rankings.

---

## Technology Stack

- **Backend & Web**: Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA, Hibernate, Thymeleaf, Flyway, JJWT (`0.12.6`)
- **Database**: MySQL 8.0+ (H2 in-memory for tests)
- **Desktop Client**: JavaFX 21
- **ML Microservice**: Python 3.12+, FastAPI, Uvicorn, Pydantic, scikit-learn
- **Build & Test**: Maven Wrapper (`mvnw` / `mvnw.cmd`), JUnit 5, MockMvc, Pytest

---

## Project Structure

```text
FlexFit/
├── .env.example                 # Environment variable template (no real secrets)
├── .gitignore                   # Excludes builds, secrets, .env, venv, and dumps
├── pom.xml                      # Multi-module Maven parent (Java 21)
├── mvnw / mvnw.cmd              # Maven Wrapper
├── start_all.bat / start_all.ps1# Windows launcher (checks health, prompts DB_PASSWORD)
├── backend/                     # Spring Boot REST API + Thymeleaf Web App
│   ├── src/main/java/com/flexfit/backend/
│   │   ├── config/              # CORS configuration
│   │   ├── controller/          # REST (/api/v1/**) and Web (@Controller) endpoints
│   │   ├── dto/                 # Request and response DTOs with Bean Validation
│   │   ├── entity/              # JPA entities (User, FitnessProfile, WorkoutLog, NutritionLog, Challenge, UserChallenge)
│   │   ├── exception/           # Scoped REST exception handling
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── security/            # Dual API/Web SecurityFilterChains & JWT provider
│   │   └── service/             # Auth, Profile, Workout, Nutrition, Challenge, Leaderboard services
│   └── src/main/resources/
│       ├── application.yml
│       ├── db/migration/        # Flyway V1 & V2 SQL migrations
│       ├── static/              # CSS, JS, and local images
│       └── templates/           # Thymeleaf fragments and pages
├── desktop-app/                 # JavaFX Desktop Application
└── ml-service/                  # Python FastAPI ML Service
```

---

## Prerequisites

- **Java 21 LTS** (e.g., Eclipse Temurin 21)
- **MySQL Server 8.0+**
- **Python 3.12+** *(optional, for `ml-service`)*
- Maven is included via `./mvnw` (`mvnw.cmd` on Windows).

---

## Installation & MySQL Setup

1. **Create the MySQL database and user** (replace `your_strong_password` with a secure password):
   ```sql
   CREATE DATABASE IF NOT EXISTS flexfit_db;
   CREATE USER IF NOT EXISTS 'flexfit_user'@'localhost' IDENTIFIED BY 'your_strong_password';
   GRANT ALL PRIVILEGES ON flexfit_db.* TO 'flexfit_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Configure environment variables**:
   Copy `.env.example` to `.env` (or export variables in your shell):
   ```powershell
   Copy-Item .env.example .env
   ```
   Key variables:
   | Variable | Default | Description |
   | :--- | :--- | :--- |
   | `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `3306` / `flexfit_db` | MySQL connection target |
   | `DB_USER` | `flexfit_user` | MySQL username |
   | `DB_PASSWORD` | *(empty)* | MySQL password (set via env or launcher prompt) |
   | `JWT_SECRET` | *(dev fallback)* | **Required in production**: 256-bit random HMAC key |
   | `COOKIE_SECURE` | `false` | Set `true` behind HTTPS in production |

---

## Running the Application

### Option 1: Windows One-Click Launcher
Double-click `start_all.bat` in the project root. It checks `http://localhost:8080/api/v1/health`, launches the backend in a separate PowerShell window with a secure `DB_PASSWORD` prompt if not running, waits for `UP`, and opens `http://localhost:8080/`.

### Option 2: Manual Startup
- **Spring Boot Web & REST Backend**:
  ```powershell
  $env:DB_PASSWORD="your_strong_password"
  .\mvnw.cmd -pl backend spring-boot:run
  ```
  Open `http://localhost:8080/` in your browser.
- **JavaFX Desktop Application**:
  ```powershell
  .\mvnw.cmd -pl desktop-app javafx:run
  ```
- **Python ML Service**:
  ```powershell
  cd ml-service
  python -m uvicorn app:app --host 127.0.0.1 --port 8000
  ```

---

## Running Tests

- **Full Maven Reactor (Backend + Desktop App)**:
  ```powershell
  .\mvnw.cmd test
  ```
- **Python ML Service**:
  ```powershell
  cd ml-service
  python -m pytest
  ```

---

## Screenshots

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/09a50add-6de5-4526-aa2b-24d7170763de" />
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/d4183de2-5473-40c5-b5cb-b75cf06bc049" />
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/df70b818-b76d-456b-b5c4-e3e77a75d64a" />
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/aaab6382-5db2-441e-80ed-cfa64af0ee78" />
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/b9679d5a-c305-40f4-b445-ae90b251b0bd" />
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/e707d9b2-aaab-4be9-be55-af104e4f88ba" />


---

## Security Notes

- Never commit `.env` files, database dumps, or production `JWT_SECRET` values.
- Always override `JWT_SECRET` and `DB_PASSWORD` and set `COOKIE_SECURE=true` in production HTTPS deployments.
- Calorie and workout burn figures are rule-based estimates for general wellness planning, not medical advice.
