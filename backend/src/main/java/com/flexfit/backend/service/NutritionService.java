package com.flexfit.backend.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutritionService {

    public static final List<String> MEAL_CATEGORIES = List.of("Breakfast", "Lunch", "Dinner", "Snacks");

    private final UserRepository userRepository;
    private final FitnessProfileRepository fitnessProfileRepository;
    private final NutritionLogRepository nutritionLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final NutritionRecommendationEngine recommendationEngine;

    public NutritionService(UserRepository userRepository,
                            FitnessProfileRepository fitnessProfileRepository,
                            NutritionLogRepository nutritionLogRepository,
                            WorkoutLogRepository workoutLogRepository,
                            NutritionRecommendationEngine recommendationEngine) {
        this.userRepository = userRepository;
        this.fitnessProfileRepository = fitnessProfileRepository;
        this.nutritionLogRepository = nutritionLogRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.recommendationEngine = recommendationEngine;
    }

    @Transactional
    public NutritionLogDto logMeal(String username, NutritionLogRequest request) {
        User user = findUser(username);

        NutritionLog log = new NutritionLog();
        log.setUser(user);
        log.setMealName(request.getMealName().trim());
        log.setMealType(normalizeMealType(request.getMealType()));
        log.setCalories(request.getCalories());
        log.setProteinG(request.getProteinG().setScale(2, RoundingMode.HALF_UP));
        log.setCarbsG(request.getCarbsG().setScale(2, RoundingMode.HALF_UP));
        log.setFatsG(request.getFatsG().setScale(2, RoundingMode.HALF_UP));
        log.setLoggedDate(request.getLoggedDate() != null ? request.getLoggedDate() : LocalDate.now());

        NutritionLog saved = nutritionLogRepository.save(log);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteMeal(String username, Long mealId) {
        User user = findUser(username);
        NutritionLog log = nutritionLogRepository.findByIdAndUserId(mealId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Nutrition log not found"));
        nutritionLogRepository.delete(log);
    }

    @Transactional(readOnly = true)
    public NutritionSummaryDto getDailySummary(String username, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        User user = findUser(username);
        FitnessProfile profile = fitnessProfileRepository.findByUserId(user.getId()).orElse(null);

        LocalDateTime weekAgo = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime nowEnd = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);
        long recentWorkouts = workoutLogRepository.countByUserIdAndCompletedAtBetween(user.getId(), weekAgo, nowEnd);

        NutritionTargetDto targets = recommendationEngine.calculateTargets(profile, recentWorkouts);
        List<NutritionLog> dayLogs = nutritionLogRepository
                .findByUserIdAndLoggedDateOrderByCreatedAtDesc(user.getId(), targetDate);

        int caloriesConsumed = dayLogs.stream().mapToInt(NutritionLog::getCalories).sum();
        BigDecimal proteinConsumed = dayLogs.stream()
                .map(NutritionLog::getProteinG)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);
        BigDecimal carbsConsumed = dayLogs.stream()
                .map(NutritionLog::getCarbsG)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);
        BigDecimal fatsConsumed = dayLogs.stream()
                .map(NutritionLog::getFatsG)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);

        Map<String, List<NutritionLogDto>> grouped = new LinkedHashMap<>();
        for (String cat : MEAL_CATEGORIES) {
            grouped.put(cat, new ArrayList<>());
        }
        for (NutritionLog log : dayLogs) {
            String key = normalizeMealType(log.getMealType());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(mapToDto(log));
        }

        NutritionSummaryDto summary = new NutritionSummaryDto();
        summary.setSelectedDate(targetDate);
        summary.setTarget(targets);
        summary.setCaloriesConsumed(caloriesConsumed);
        summary.setProteinConsumedG(proteinConsumed);
        summary.setCarbsConsumedG(carbsConsumed);
        summary.setFatsConsumedG(fatsConsumed);
        summary.setMealsLoggedCount(dayLogs.size());
        summary.setMealsByType(grouped);

        if (targets.isCalculable()) {
            summary.setRemainingCalories(Math.max(0, targets.getTargetCalories() - caloriesConsumed));
            summary.setCalorieProgressPercent(clampPercent(caloriesConsumed, targets.getTargetCalories()));

            summary.setRemainingProteinG(BigDecimal.valueOf(targets.getProteinTargetG())
                    .subtract(proteinConsumed).max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP));
            summary.setProteinProgressPercent(clampPercent(proteinConsumed.doubleValue(), targets.getProteinTargetG()));

            summary.setRemainingCarbsG(BigDecimal.valueOf(targets.getCarbsTargetG())
                    .subtract(carbsConsumed).max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP));
            summary.setCarbsProgressPercent(clampPercent(carbsConsumed.doubleValue(), targets.getCarbsTargetG()));

            summary.setRemainingFatsG(BigDecimal.valueOf(targets.getFatsTargetG())
                    .subtract(fatsConsumed).max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP));
            summary.setFatsProgressPercent(clampPercent(fatsConsumed.doubleValue(), targets.getFatsTargetG()));
        } else {
            summary.setRemainingCalories(0);
            summary.setRemainingProteinG(BigDecimal.ZERO);
            summary.setRemainingCarbsG(BigDecimal.ZERO);
            summary.setRemainingFatsG(BigDecimal.ZERO);
        }

        return summary;
    }

    @Transactional(readOnly = true)
    public List<NutritionLogDto> getNutritionHistory(String username, LocalDate filterDate) {
        User user = findUser(username);
        List<NutritionLog> logs = (filterDate != null)
                ? nutritionLogRepository.findByUserIdAndLoggedDateOrderByCreatedAtDesc(user.getId(), filterDate)
                : nutritionLogRepository.findByUserIdOrderByLoggedDateDescCreatedAtDesc(user.getId());
        return logs.stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<FoodRecommendationDto> getFoodRecommendations(String username, LocalDate date) {
        User user = findUser(username);
        FitnessProfile profile = fitnessProfileRepository.findByUserId(user.getId()).orElse(null);
        NutritionSummaryDto summary = getDailySummary(username, date);
        int remainingCal = summary.getTarget().isCalculable() ? summary.getRemainingCalories() : 500;
        BigDecimal remainingProtein = summary.getTarget().isCalculable()
                ? summary.getRemainingProteinG()
                : BigDecimal.valueOf(40);
        return recommendationEngine.recommendFoods(profile, remainingCal, remainingProtein);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private String normalizeMealType(String raw) {
        if (raw == null) {
            return "Snacks";
        }
        String trimmed = raw.trim();
        for (String cat : MEAL_CATEGORIES) {
            if (cat.equalsIgnoreCase(trimmed)) {
                return cat;
            }
        }
        return "Snacks";
    }

    private int clampPercent(double consumed, int target) {
        if (target <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round((consumed * 100.0) / target));
    }

    private NutritionLogDto mapToDto(NutritionLog log) {
        return new NutritionLogDto(
                log.getId(),
                log.getMealName(),
                log.getMealType(),
                log.getCalories(),
                log.getProteinG(),
                log.getCarbsG(),
                log.getFatsG(),
                log.getLoggedDate(),
                log.getCreatedAt()
        );
    }
}
