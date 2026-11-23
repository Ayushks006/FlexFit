-- ===================================================================
-- FLEXFIT PHASE 2: AUTHENTICATION & FITNESS PROFILE DATABASE SCHEMA
-- ===================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Fitness Profiles Table
CREATE TABLE IF NOT EXISTS fitness_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    
    -- Personal Information
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    district VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    
    -- Body Information
    height_cm DECIMAL(5,2) NOT NULL,
    current_weight_kg DECIMAL(5,2) NOT NULL,
    target_weight_kg DECIMAL(5,2) NOT NULL,
    
    -- Fitness Information
    fitness_goal VARCHAR(50) NOT NULL,
    experience_level VARCHAR(30) NOT NULL,
    
    -- Workout and Diet Preferences
    workout_location VARCHAR(30) NOT NULL,
    available_equipment TEXT,
    preferred_workout_duration_min INT NOT NULL,
    diet_preference VARCHAR(30) NOT NULL,
    
    -- Safety Information
    food_allergies TEXT,
    dietary_restrictions TEXT,
    injuries_limitations TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_fitness_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_fitness_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for efficient querying
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_fitness_profiles_user_id ON fitness_profiles(user_id);
