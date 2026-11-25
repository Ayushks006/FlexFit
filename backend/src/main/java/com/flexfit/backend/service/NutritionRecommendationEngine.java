package com.flexfit.backend.service;

import com.flexfit.backend.dto.response.FoodRecommendationDto;
import com.flexfit.backend.dto.response.NutritionTargetDto;
import com.flexfit.backend.entity.FitnessProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule-based energy, macronutrient, and food recommendation engine.
 * Uses Mifflin-St Jeor for BMR estimation and deterministic allergy/diet filtering.
 */
@Component
public class NutritionRecommendationEngine {

    private static final String DISCLAIMER =
            "Calorie and macronutrient targets are rule-based estimates for general wellness planning, not medical advice.";

    private record FoodCandidate(
            String name,
            String category,
            String mealSlot,
            String servingSize,
            int calories,
            int proteinG,
            int carbsG,
            int fatsG,
            Set<String> tags
    ) {
    }

    private static final List<FoodCandidate> FOOD_CATALOG = List.of(
            new FoodCandidate("Grilled Paneer & Herb Bowl", "Vegetarian", "Lunch / Dinner", "150 g paneer",
                    360, 24, 8, 26, Set.of("dairy", "milk", "paneer", "lactose", "vegetarian")),
            new FoodCandidate("Moong Dal & Brown Rice", "Vegetarian", "Lunch / Dinner", "1 bowl (300 g)",
                    340, 15, 58, 4, Set.of("dal", "lentils", "legumes", "vegetarian", "vegan")),
            new FoodCandidate("Spiced Chickpea (Chana) Salad", "Vegetarian", "Lunch / Snacks", "1 bowl (220 g)",
                    290, 14, 44, 6, Set.of("chickpeas", "chana", "legumes", "vegetarian", "vegan")),
            new FoodCandidate("Sprouted Moong & Veg Chaat", "Vegetarian", "Snacks / Breakfast", "1 bowl (180 g)",
                    195, 13, 30, 2, Set.of("sprouts", "moong", "legumes", "vegetarian", "vegan")),
            new FoodCandidate("Plain Curd (Dahi) & Chia Bowl", "Vegetarian", "Breakfast / Snacks", "200 g curd",
                    175, 11, 14, 8, Set.of("dairy", "milk", "curd", "dahi", "yogurt", "lactose", "vegetarian")),
            new FoodCandidate("Low-Fat Milk & Rolled Oats", "Vegetarian", "Breakfast", "250 ml milk + 45 g oats",
                    285, 14, 42, 6, Set.of("dairy", "milk", "oats", "gluten", "lactose", "vegetarian")),
            new FoodCandidate("Stir-Fried Soy Chunks & Greens", "Vegetarian", "Dinner / Lunch", "60 g dry soy chunks",
                    250, 28, 20, 4, Set.of("soy", "soya", "tofu", "vegetarian", "vegan")),
            new FoodCandidate("Boiled Eggs & Whole-Grain Toast", "Non-Vegetarian", "Breakfast", "3 whole eggs + 1 slice",
                    310, 21, 16, 17, Set.of("egg", "eggs", "gluten", "wheat", "non-vegetarian")),
            new FoodCandidate("Grilled Chicken Breast & Veggies", "Non-Vegetarian", "Lunch / Dinner", "160 g chicken breast",
                    290, 38, 10, 9, Set.of("chicken", "poultry", "meat", "non-vegetarian")),
            new FoodCandidate("Baked Fish Fillet & Quinoa", "Non-Vegetarian", "Dinner", "150 g fish + 100 g quinoa",
                    340, 32, 24, 12, Set.of("fish", "seafood", "non-vegetarian"))
    );

    public NutritionTargetDto calculateTargets(FitnessProfile profile, long recentWorkoutsCount) {
        NutritionTargetDto target = new NutritionTargetDto();
        target.setDisclaimer(DISCLAIMER);

        List<String> missing = new ArrayList<>();
        if (profile == null) {
            missing.add("complete fitness profile");
        } else {
            if (profile.getAge() == null || profile.getAge() <= 0) missing.add("age");
            if (profile.getGender() == null || profile.getGender().isBlank()) missing.add("gender");
            if (profile.getHeightCm() == null || profile.getHeightCm().doubleValue() <= 0) missing.add("height");
            if (profile.getCurrentWeightKg() == null || profile.getCurrentWeightKg().doubleValue() <= 0) missing.add("current weight");
            if (profile.getFitnessGoal() == null || profile.getFitnessGoal().isBlank()) missing.add("fitness goal");
        }

        if (!missing.isEmpty()) {
            target.setCalculable(false);
            target.setMissingProfileFieldsMessage(
                    "To calculate your personalized calorie and macro targets, please update your profile with: "
                            + String.join(", ", missing) + ".");
            return target;
        }

        double weightKg = profile.getCurrentWeightKg().doubleValue();
        double heightCm = profile.getHeightCm().doubleValue();
        int age = profile.getAge();
        String gender = profile.getGender().trim().toLowerCase(Locale.ROOT);
        String goal = profile.getFitnessGoal().trim().toLowerCase(Locale.ROOT);

        // Mifflin-St Jeor BMR
        double baseBmr = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age);
        double genderOffset;
        if (gender.equals("male")) {
            genderOffset = 5.0;
        } else if (gender.equals("female")) {
            genderOffset = -161.0;
        } else {
            genderOffset = -78.0; // Midpoint when gender is non-binary or undisclosed
        }
        int bmr = (int) Math.round(baseBmr + genderOffset);

        double multiplier = recentWorkoutsCount >= 3 ? 1.55 : 1.375;
        String activityNote = recentWorkoutsCount >= 3
                ? "Moderate activity multiplier (1.55x BMR based on " + recentWorkoutsCount + " workouts in the last 7 days)"
                : "Conservative light-activity default multiplier (1.375x BMR)";
        int tdee = (int) Math.round(bmr * multiplier);

        int safeFloor = gender.equals("male") ? 1400 : 1200;
        int adjustedCalories;
        String goalNote;
        double proteinPerKg;

        if (goal.contains("weight loss") || goal.contains("fat")) {
            adjustedCalories = Math.max(safeFloor, tdee - 400);
            goalNote = "Moderate ~400 kcal deficit from estimated TDEE with a safe minimum floor of " + safeFloor + " kcal.";
            proteinPerKg = 1.8;
        } else if (goal.contains("muscle")) {
            adjustedCalories = Math.min(4500, tdee + 300);
            goalNote = "Controlled ~300 kcal surplus above estimated TDEE to support lean tissue growth.";
            proteinPerKg = 1.9;
        } else if (goal.contains("strength")) {
            adjustedCalories = Math.min(4500, tdee + 150);
            goalNote = "Slight ~150 kcal surplus above estimated TDEE to support strength recovery.";
            proteinPerKg = 1.8;
        } else if (goal.contains("endurance")) {
            adjustedCalories = Math.min(4500, tdee + 200);
            goalNote = "Modest ~200 kcal surplus to fuel sustained aerobic sessions.";
            proteinPerKg = 1.5;
        } else {
            adjustedCalories = Math.max(safeFloor, tdee);
            goalNote = "Maintenance intake aligned with estimated daily energy expenditure.";
            proteinPerKg = 1.5;
        }

        // Macronutrients consistent with targetCalories
        int proteinG = (int) Math.round(weightKg * proteinPerKg);
        int maxProteinG = (int) Math.floor((adjustedCalories * 0.40) / 4.0);
        proteinG = Math.max(45, Math.min(proteinG, maxProteinG));

        int fatsG = Math.max(40, (int) Math.round((adjustedCalories * 0.27) / 9.0));
        int remainingForCarbs = adjustedCalories - (proteinG * 4) - (fatsG * 9);
        if (remainingForCarbs < 80) {
            fatsG = Math.max(30, (adjustedCalories - (proteinG * 4) - 80) / 9);
            remainingForCarbs = Math.max(20, adjustedCalories - (proteinG * 4) - (fatsG * 9));
        }
        int carbsG = (int) Math.round(remainingForCarbs / 4.0);

        target.setCalculable(true);
        target.setBmrCalories(bmr);
        target.setEstimatedTdeeCalories(tdee);
        target.setActivityAssumption(activityNote);
        target.setTargetCalories(adjustedCalories);
        target.setProteinTargetG(proteinG);
        target.setCarbsTargetG(carbsG);
        target.setFatsTargetG(fatsG);
        target.setGoalAdjustmentNote(goalNote);
        return target;
    }

    public List<FoodRecommendationDto> recommendFoods(FitnessProfile profile,
                                                      int remainingCalories,
                                                      BigDecimal remainingProteinG) {
        boolean vegetarianOnly = profile != null
                && profile.getDietPreference() != null
                && profile.getDietPreference().trim().equalsIgnoreCase("Vegetarian");

        Set<String> excludedTerms = parseExclusionTokens(
                profile != null ? profile.getFoodAllergies() : null,
                profile != null ? profile.getDietaryRestrictions() : null
        );

        String goal = profile != null && profile.getFitnessGoal() != null
                ? profile.getFitnessGoal().toLowerCase(Locale.ROOT)
                : "general fitness";

        double proteinNeeded = remainingProteinG != null ? remainingProteinG.doubleValue() : 40.0;

        List<FoodCandidate> eligible = FOOD_CATALOG.stream()
                .filter(f -> !vegetarianOnly || "Vegetarian".equalsIgnoreCase(f.category()))
                .filter(f -> !matchesExcluded(f, excludedTerms))
                .sorted(Comparator.comparingInt((FoodCandidate f) -> scoreCandidate(f, remainingCalories, proteinNeeded, goal)).reversed())
                .limit(4)
                .toList();

        List<FoodRecommendationDto> results = new ArrayList<>();
        for (FoodCandidate f : eligible) {
            String reason = buildReason(f, remainingCalories, proteinNeeded, vegetarianOnly);
            results.add(new FoodRecommendationDto(
                    f.name(), f.category(), f.mealSlot(), f.servingSize(),
                    f.calories(), f.proteinG(), f.carbsG(), f.fatsG(), reason
            ));
        }
        return results;
    }

    private int scoreCandidate(FoodCandidate f, int remainingCalories, double proteinNeeded, String goal) {
        int score = 0;
        if (proteinNeeded > 15) {
            score += f.proteinG() * 3;
        } else {
            score += f.proteinG();
        }
        if (remainingCalories > 0 && f.calories() <= remainingCalories + 50) {
            score += 40;
        } else if (remainingCalories <= 0 && f.calories() <= 220) {
            score += 20;
        }
        if (goal.contains("weight loss") && f.calories() <= 300) {
            score += 15;
        }
        if (goal.contains("muscle") && f.proteinG() >= 20) {
            score += 20;
        }
        return score;
    }

    private String buildReason(FoodCandidate f, int remainingCalories, double proteinNeeded, boolean vegetarianOnly) {
        StringBuilder sb = new StringBuilder("Rule-based ");
        sb.append(vegetarianOnly ? "vegetarian " : "");
        sb.append("match");
        if (proteinNeeded > 10) {
            sb.append(": provides ~").append(f.proteinG()).append("g protein");
        }
        if (remainingCalories > 0 && f.calories() <= remainingCalories) {
            sb.append(" within your remaining ").append(remainingCalories).append(" kcal");
        }
        sb.append(".");
        return sb.toString();
    }

    private Set<String> parseExclusionTokens(String allergies, String restrictions) {
        String combined = ((allergies != null ? allergies : "") + "," + (restrictions != null ? restrictions : ""))
                .toLowerCase(Locale.ROOT);
        return Arrays.stream(combined.split("[,;/\\s]+"))
                .map(String::trim)
                .filter(s -> s.length() >= 3 && !s.equals("none") && !s.equals("and") && !s.equals("free"))
                .collect(Collectors.toSet());
    }

    private boolean matchesExcluded(FoodCandidate food, Set<String> excludedTerms) {
        if (excludedTerms.isEmpty()) {
            return false;
        }
        String lowerName = food.name().toLowerCase(Locale.ROOT);
        for (String term : excludedTerms) {
            if (lowerName.contains(term)) {
                return true;
            }
            for (String tag : food.tags()) {
                if (tag.contains(term) || term.contains(tag)) {
                    return true;
                }
            }
        }
        return false;
    }
}
