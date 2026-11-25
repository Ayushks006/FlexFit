package com.flexfit.backend;

import com.flexfit.backend.dto.request.WorkoutLogRequest;
import com.flexfit.backend.dto.response.RecommendedWorkoutDto;
import com.flexfit.backend.dto.response.WorkoutLogDto;
import com.flexfit.backend.dto.response.WorkoutSummaryDto;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.entity.WorkoutLog;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.repository.WorkoutLogRepository;
import com.flexfit.backend.service.WorkoutRecommendationEngine;
import com.flexfit.backend.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FitnessProfileRepository fitnessProfileRepository;

    @Mock
    private WorkoutLogRepository workoutLogRepository;

    private WorkoutRecommendationEngine recommendationEngine;
    private WorkoutService workoutService;

    private User testUser;
    private FitnessProfile profile;

    @BeforeEach
    void setUp() {
        recommendationEngine = new WorkoutRecommendationEngine();
        workoutService = new WorkoutService(userRepository, fitnessProfileRepository, workoutLogRepository, recommendationEngine);

        testUser = new User("Workout Tester", "workuser", "work@flexfit.test", "hash");
        testUser.setId(10L);

        profile = new FitnessProfile();
        profile.setUser(testUser);
        profile.setAge(29);
        profile.setGender("Male");
        profile.setCurrentWeightKg(new BigDecimal("80.0"));
        profile.setTargetWeightKg(new BigDecimal("75.0"));
        profile.setHeightCm(new BigDecimal("178.0"));
        profile.setFitnessGoal("Weight loss");
        profile.setExperienceLevel("Beginner");
        profile.setWorkoutLocation("Home");
        profile.setAvailableEquipment("None");
        profile.setPreferredWorkoutDurationMin(40);
    }

    @Test
    @DisplayName("Recommendation engine adapts to fitness goal, experience level, and equipment")
    void testRecommendationsAdaptToProfile() {
        when(userRepository.findByUsername("workuser")).thenReturn(Optional.of(testUser));
        when(fitnessProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

        List<RecommendedWorkoutDto> beginnerWeightLoss = workoutService.getRecommendationsForUser("workuser");
        assertEquals(3, beginnerWeightLoss.size());
        assertEquals("HIIT & Conditioning", beginnerWeightLoss.get(0).getCategory());
        assertEquals(40, beginnerWeightLoss.get(0).getEstimatedDurationMin());
        assertTrue(beginnerWeightLoss.get(0).getApproximateCalories() > 0);

        profile.setFitnessGoal("Muscle gain");
        profile.setExperienceLevel("Advanced");
        profile.setWorkoutLocation("Gym");
        profile.setAvailableEquipment("Barbell,Dumbbells,Bench");

        List<RecommendedWorkoutDto> advancedMuscle = workoutService.getRecommendationsForUser("workuser");
        assertEquals("Strength", advancedMuscle.get(0).getCategory());
        assertEquals("Advanced", advancedMuscle.get(0).getDifficulty());
        assertTrue(advancedMuscle.get(0).getExercises().stream()
                .anyMatch(e -> e.getName().contains("Barbell") || e.getName().contains("Dumbbell")));
    }

    @Test
    @DisplayName("Recommendation engine filters conflicting exercises for recorded injuries")
    void testInjurySafeRecommendations() {
        profile.setAvailableEquipment("Barbell,Dumbbells");
        profile.setInjuriesLimitations("Left knee pain and lower back sensitivity");

        List<RecommendedWorkoutDto> plans = recommendationEngine.recommend(profile);
        String allExerciseNames = plans.stream()
                .flatMap(p -> p.getExercises().stream())
                .map(e -> e.getName().toLowerCase())
                .reduce("", (a, b) -> a + " " + b);

        assertFalse(allExerciseNames.contains("barbell back squat"));
        assertTrue(plans.get(0).getSafetyNote().contains("Left knee pain"));
    }

    @Test
    @DisplayName("Logging a workout estimates calories when omitted and persists for authenticated user")
    void testLogWorkoutEstimatesCalories() {
        when(userRepository.findByUsername("workuser")).thenReturn(Optional.of(testUser));
        when(fitnessProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));
        when(workoutLogRepository.save(any(WorkoutLog.class))).thenAnswer(inv -> {
            WorkoutLog saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        WorkoutLogRequest req = new WorkoutLogRequest();
        req.setTitle("Evening Run");
        req.setWorkoutType("Cardio");
        req.setDurationMin(30);

        WorkoutLogDto dto = workoutService.logWorkout("workuser", req);
        assertEquals(99L, dto.getId());
        assertEquals("Evening Run", dto.getTitle());
        assertTrue(dto.getCaloriesBurned() > 0);
    }

    @Test
    @DisplayName("Deleting another user's workout throws ResourceNotFoundException")
    void testOwnershipEnforcedOnDelete() {
        when(userRepository.findByUsername("workuser")).thenReturn(Optional.of(testUser));
        when(workoutLogRepository.findByIdAndUserId(555L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> workoutService.deleteWorkout("workuser", 555L));
        verify(workoutLogRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Workout summary aggregates real weekly and total records or returns zero when empty")
    void testWorkoutSummaryCalculation() {
        when(userRepository.findByUsername("workuser")).thenReturn(Optional.of(testUser));
        when(workoutLogRepository.findByUserIdOrderByCompletedAtDesc(10L)).thenReturn(List.of());
        when(workoutLogRepository.findByUserIdAndCompletedAtBetweenOrderByCompletedAtDesc(eq(10L), any(), any()))
                .thenReturn(List.of());

        WorkoutSummaryDto emptySummary = workoutService.getWorkoutSummary("workuser");
        assertEquals(0L, emptySummary.getTotalWorkouts());
        assertEquals(0, emptySummary.getWeeklyWorkouts());
        assertEquals(7, emptySummary.getWeeklyActivity().size());

        WorkoutLog w1 = new WorkoutLog();
        w1.setId(1L);
        w1.setTitle("Upper Body");
        w1.setWorkoutType("Strength");
        w1.setDurationMin(45);
        w1.setCaloriesBurned(300);
        w1.setCompletedAt(LocalDateTime.now());

        when(workoutLogRepository.findByUserIdOrderByCompletedAtDesc(10L)).thenReturn(List.of(w1));
        when(workoutLogRepository.findByUserIdAndCompletedAtBetweenOrderByCompletedAtDesc(eq(10L), any(), any()))
                .thenReturn(List.of(w1));

        WorkoutSummaryDto populated = workoutService.getWorkoutSummary("workuser");
        assertEquals(1L, populated.getTotalWorkouts());
        assertEquals(1, populated.getWeeklyWorkouts());
        assertEquals(45, populated.getWeeklyDurationMin());
        assertEquals(300, populated.getWeeklyCaloriesBurned());
    }
}
