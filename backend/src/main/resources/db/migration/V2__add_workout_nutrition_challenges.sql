-- ===================================================================
-- FLEXFIT STAGE 1: WORKOUTS, NUTRITION, CHALLENGES & PARTICIPATION
-- Additive migration only. Does not alter V1 tables or existing data.
-- ===================================================================

-- 1. Workout Logs Table
CREATE TABLE IF NOT EXISTS workout_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    workout_type VARCHAR(50) NOT NULL,
    duration_min INT NOT NULL,
    calories_burned INT NOT NULL DEFAULT 0,
    completed_at TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workout_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_workout_logs_user_id ON workout_logs(user_id);
CREATE INDEX idx_workout_logs_completed_at ON workout_logs(completed_at);
CREATE INDEX idx_workout_logs_user_completed ON workout_logs(user_id, completed_at);

-- 2. Nutrition Logs Table
CREATE TABLE IF NOT EXISTS nutrition_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    meal_name VARCHAR(120) NOT NULL,
    meal_type VARCHAR(30) NOT NULL,
    calories INT NOT NULL,
    protein_g DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    carbs_g DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    fats_g DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    logged_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_nutrition_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_nutrition_logs_user_id ON nutrition_logs(user_id);
CREATE INDEX idx_nutrition_logs_logged_date ON nutrition_logs(logged_date);
CREATE INDEX idx_nutrition_logs_user_date ON nutrition_logs(user_id, logged_date);

-- 3. Challenges Catalog Table
CREATE TABLE IF NOT EXISTS challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    target_count INT NOT NULL,
    points_reward INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_challenges_title UNIQUE (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_challenges_active ON challenges(active);

-- 4. User Challenge Participation Table
CREATE TABLE IF NOT EXISTS user_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    progress_count INT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,

    CONSTRAINT uk_user_challenges_user_challenge UNIQUE (user_id, challenge_id),
    CONSTRAINT fk_user_challenges_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_challenges_challenge FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_challenges_user_id ON user_challenges(user_id);
CREATE INDEX idx_user_challenges_challenge_id ON user_challenges(challenge_id);
CREATE INDEX idx_user_challenges_user_completed ON user_challenges(user_id, completed);

-- 5. Seed Initial Active Fitness Challenges
INSERT IGNORE INTO challenges (title, description, category, target_count, points_reward, active) VALUES
('7-Day Consistency Streak', 'Complete 7 structured workout sessions to build a reliable weekly training habit.', 'Consistency', 7, 150, TRUE),
('Cardio Endurance Push', 'Log 5 cardio or conditioning sessions of at least 20 minutes each.', 'Cardio', 5, 200, TRUE),
('Strength Foundation', 'Finish 12 strength-focused workouts aligned with your equipment setup.', 'Strength', 12, 250, TRUE),
('Balanced Macro Week', 'Log your daily meals for 7 days to stay aware of calories and protein.', 'Nutrition', 7, 180, TRUE),
('Mobility & Recovery', 'Complete 4 mobility, stretching, or active recovery sessions.', 'Flexibility', 4, 120, TRUE);
