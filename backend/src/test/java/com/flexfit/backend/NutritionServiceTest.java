package com.flexfit.backend;

import com.flexfit.backend.dto.request.NutritionLogRequest;
import com.flexfit.backend.dto.response.FoodRecommendationDto;
import com.flexfit.backend.dto.response.NutritionLogDto;
import com.flexfit.backend.dto.response.NutritionSummaryDto;
import com.flexfit.backend.dto.response.NutritionTargetDto;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.NutritionLog;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.NutritionLogRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.repository.WorkoutLogRepository;
import com.flexfit.backend.service.NutritionRecommendationEngine;
import com.flexfit.backend.service.NutritionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FitnessProfileRepository fitnessProfileRepository;

    @Mock
    private NutritionLogRepository nutritionLogRepository;

    @Mock
    private WorkoutLogRepository workoutLogRepository;

    private NutritionRecommendationEngine engine;
    private NutritionService nutritionService;
    private User user;
    private FitnessProfile profile;

    @BeforeEach
    void setUp() {
        engine = new NutritionRecommendationEngine();
        nutritionService = new NutritionService(
                userRepository, fitnessProfileRepository, nutritionLogRepository, workoutLogRepository, engine);

        user = new User("Nutrition Tester", "nutriuser", "nutri@flexfit.test", "hash");
        user.setId(20L);

        profile = new FitnessProfile();
        profile.setUser(user);
        profile.setAge(28);
        profile.setGender("Male");
        profile.setHeightCm(new BigDecimal("178.0"));
        profile.setCurrentWeightKg(new BigDecimal("78.0"));
        profile.setTargetWeightKg(new BigDecimal("74.0"));
        profile.setFitnessGoal("Weight loss");
        profile.setDietPreference("Vegetarian");
        profile.setFoodAllergies("Peanuts, Dairy");
    }

    @Test
    @DisplayName("Mifflin-St Jeor target calculation handles valid profiles and missing profile fields")
    void testTargetCalculationAndMissingFields() {
        NutritionTargetDto valid = engine.calculateTargets(profile, 2L);
        assertTrue(valid.isCalculable());
        assertNotNull(valid.getBmrCalories());
        assertTrue(valid.getTargetCalories() >= 1400);
        assertTrue(valid.getProteinTargetG() > 0);
        assertNotNull(valid.getDisclaimer());

        // Verify macro calories are reasonably consistent with targetCalories
        int macroCalories = (valid.getProteinTargetG() * 4)
                + (valid.getCarbsTargetG() * 4)
                + (valid.getFatsTargetG() * 9);
        assertTrue(Math.abs(macroCalories - valid.getTargetCalories()) <= 25);

        FitnessProfile incomplete = new FitnessProfile();
        incomplete.setAge(25);
        NutritionTargetDto missing = engine.calculateTargets(incomplete, 0L);
        assertFalse(missing.isCalculable());
        assertTrue(missing.getMissingProfileFieldsMessage().contains("gender"));
        assertTrue(missing.getMissingProfileFieldsMessage().contains("height"));
    }

    @Test
    @DisplayName("Food recommendations respect vegetarian preference and never suggest listed allergies")
    void testFoodRecommendationsRespectDietAndAllergies() {
        List<FoodRecommendationDto> recs = engine.recommendFoods(profile, 600, BigDecimal.valueOf(45));
        assertFalse(recs.isEmpty());
        for (FoodRecommendationDto rec : recs) {
            assertEquals("Vegetarian", rec.getCategory());
            String lower = rec.getName().toLowerCase();
            assertFalse(lower.contains("paneer"));
            assertFalse(lower.contains("curd"));
            assertFalse(lower.contains("milk"));
            assertFalse(lower.contains("chicken"));
            assertFalse(lower.contains("fish"));
        }
    }

    @Test
    @DisplayName("Daily summary aggregates meals by category and computes remaining macros")
    void testDailySummaryAggregation() {
        LocalDate today = LocalDate.now();
        when(userRepository.findByUsername("nutriuser")).thenReturn(Optional.of(user));
        when(fitnessProfileRepository.findByUserId(20L)).thenReturn(Optional.of(profile));
        when(workoutLogRepository.countByUserIdAndCompletedAtBetween(eq(20L), any(), any())).thenReturn(1L);

        NutritionLog log = new NutritionLog();
        log.setId(1L);
        log.setMealName("Soy Stir-Fry");
        log.setMealType("Dinner");
        log.setCalories(450);
        log.setProteinG(new BigDecimal("30.00"));
        log.setCarbsG(new BigDecimal("42.00"));
        log.setFatsG(new BigDecimal("12.00"));
        log.setLoggedDate(today);

        when(nutritionLogRepository.findByUserIdAndLoggedDateOrderByCreatedAtDesc(20L, today))
                .thenReturn(List.of(log));

        NutritionSummaryDto summary = nutritionService.getDailySummary("nutriuser", today);
        assertEquals(450, summary.getCaloriesConsumed());
        assertEquals(1, summary.getMealsLoggedCount());
        assertEquals(1, summary.getMealsByType().get("Dinner").size());
        assertEquals(0, summary.getMealsByType().get("Breakfast").size());
        assertTrue(summary.getRemainingCalories() < summary.getTarget().getTargetCalories());
    }

    @Test
    @DisplayName("Logging a meal persists with normalized scale and delete enforces ownership")
    void testLogAndOwnershipEnforcement() {
        when(userRepository.findByUsername("nutriuser")).thenReturn(Optional.of(user));
        when(nutritionLogRepository.save(any(NutritionLog.class))).thenAnswer(inv -> {
            NutritionLog saved = inv.getArgument(0);
            saved.setId(77L);
            return saved;
        });

        NutritionLogRequest req = new NutritionLogRequest();
        req.setMealName("Moong Dal Bowl");
        req.setMealType("Lunch");
        req.setCalories(380);
        req.setProteinG(new BigDecimal("16.5"));
        req.setCarbsG(new BigDecimal("54"));
        req.setFatsG(new BigDecimal("6.25"));
        req.setLoggedDate(LocalDate.now());

        NutritionLogDto created = nutritionService.logMeal("nutriuser", req);
        assertEquals(77L, created.getId());
        assertEquals("Lunch", created.getMealType());

        when(nutritionLogRepository.findByIdAndUserId(999L, 20L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> nutritionService.deleteMeal("nutriuser", 999L));
        verify(nutritionLogRepository, never()).delete(any());
    }
}
