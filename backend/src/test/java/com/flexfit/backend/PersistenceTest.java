package com.flexfit.backend;

import com.flexfit.backend.entity.Challenge;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.NutritionLog;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.entity.UserChallenge;
import com.flexfit.backend.entity.WorkoutLog;
import com.flexfit.backend.repository.ChallengeRepository;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.NutritionLogRepository;
import com.flexfit.backend.repository.UserChallengeRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.repository.WorkoutLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FitnessProfileRepository fitnessProfileRepository;

    @Autowired
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private NutritionLogRepository nutritionLogRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserChallengeRepository userChallengeRepository;

    @Test
    @DisplayName("User and linked FitnessProfile persist and retrieve successfully")
    void testUserAndProfilePersistence() {
        User user = new User("Fitness Member", "fituser1", "fituser1@flexfit.test", "hashed_password");
        User savedUser = userRepository.saveAndFlush(user);

        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());

        FitnessProfile profile = new FitnessProfile();
        profile.setUser(savedUser);
        profile.setAge(25);
        profile.setGender("Female");
        profile.setDistrict("Downtown");
        profile.setState("Metro");
        profile.setHeightCm(new BigDecimal("168.0"));
        profile.setCurrentWeightKg(new BigDecimal("58.0"));
        profile.setTargetWeightKg(new BigDecimal("55.0"));
        profile.setFitnessGoal("General fitness");
        profile.setExperienceLevel("Beginner");
        profile.setWorkoutLocation("Home");
        profile.setPreferredWorkoutDurationMin(30);
        profile.setDietPreference("Vegetarian");

        FitnessProfile savedProfile = fitnessProfileRepository.saveAndFlush(profile);
        assertNotNull(savedProfile.getId());
        assertEquals(savedUser.getId(), savedProfile.getUser().getId());

        Optional<FitnessProfile> retrieved = fitnessProfileRepository.findByUserId(savedUser.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("General fitness", retrieved.get().getFitnessGoal());
    }

    @Test
    @DisplayName("Persisting duplicate username violates unique constraint")
    void testDuplicateUsernameConstraint() {
        User user1 = new User("Member One", "duplicate_user", "email1@flexfit.test", "hash1");
        userRepository.saveAndFlush(user1);

        User user2 = new User("Member Two", "duplicate_user", "email2@flexfit.test", "hash2");
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }

    @Test
    @DisplayName("Persisting duplicate email violates unique constraint")
    void testDuplicateEmailConstraint() {
        User user1 = new User("Member One", "user1", "duplicate@flexfit.test", "hash1");
        userRepository.saveAndFlush(user1);

        User user2 = new User("Member Two", "user2", "duplicate@flexfit.test", "hash2");
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }

    @Test
    @DisplayName("WorkoutLog, NutritionLog, Challenge, and UserChallenge persist and query correctly")
    void testStage1EntitiesAndRepositories() {
        User user = userRepository.saveAndFlush(
                new User("Stage One Athlete", "stage1_user", "stage1@flexfit.test", "hash"));

        LocalDateTime now = LocalDateTime.now();
        WorkoutLog workout = new WorkoutLog();
        workout.setUser(user);
        workout.setTitle("Upper Body Strength");
        workout.setWorkoutType("Strength");
        workout.setDurationMin(45);
        workout.setCaloriesBurned(320);
        workout.setCompletedAt(now);
        workout.setNotes("Focused on compound lifts");
        workoutLogRepository.saveAndFlush(workout);

        List<WorkoutLog> workouts = workoutLogRepository.findByUserIdOrderByCompletedAtDesc(user.getId());
        assertEquals(1, workouts.size());
        assertEquals("Upper Body Strength", workouts.get(0).getTitle());
        assertEquals(1L, workoutLogRepository.countByUserIdAndCompletedAtBetween(
                user.getId(), now.minusDays(1), now.plusDays(1)));

        LocalDate today = LocalDate.now();
        NutritionLog meal = new NutritionLog();
        meal.setUser(user);
        meal.setMealName("Oatmeal & Berries");
        meal.setMealType("Breakfast");
        meal.setCalories(420);
        meal.setProteinG(new BigDecimal("18.50"));
        meal.setCarbsG(new BigDecimal("64.00"));
        meal.setFatsG(new BigDecimal("9.50"));
        meal.setLoggedDate(today);
        nutritionLogRepository.saveAndFlush(meal);

        List<NutritionLog> todaysMeals = nutritionLogRepository
                .findByUserIdAndLoggedDateOrderByCreatedAtDesc(user.getId(), today);
        assertEquals(1, todaysMeals.size());
        assertEquals(420, todaysMeals.get(0).getCalories());

        Challenge challenge = new Challenge();
        challenge.setTitle("7-Day Consistency");
        challenge.setDescription("Complete 7 workouts");
        challenge.setCategory("Consistency");
        challenge.setTargetCount(7);
        challenge.setPointsReward(150);
        challenge.setActive(true);
        Challenge savedChallenge = challengeRepository.saveAndFlush(challenge);

        assertEquals(1, challengeRepository.findByActiveTrueOrderByIdAsc().size());

        UserChallenge participation = new UserChallenge();
        participation.setUser(user);
        participation.setChallenge(savedChallenge);
        participation.setProgressCount(7);
        participation.setCompleted(true);
        participation.setCompletedAt(now);
        userChallengeRepository.saveAndFlush(participation);

        assertTrue(userChallengeRepository.existsByUserIdAndChallengeId(user.getId(), savedChallenge.getId()));
        assertEquals(1L, userChallengeRepository.countByUserIdAndCompletedTrue(user.getId()));
        assertEquals(150L, userChallengeRepository.sumCompletedPointsByUserId(user.getId()));

        UserChallenge duplicate = new UserChallenge();
        duplicate.setUser(user);
        duplicate.setChallenge(savedChallenge);
        assertThrows(DataIntegrityViolationException.class, () -> userChallengeRepository.saveAndFlush(duplicate));
    }
}
