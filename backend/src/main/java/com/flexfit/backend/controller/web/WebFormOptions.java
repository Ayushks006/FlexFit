package com.flexfit.backend.controller.web;

import java.util.List;

/**
 * Shared form option constants matching the FlexFit JavaFX client and backend DTO validation rules.
 */
public final class WebFormOptions {

    private WebFormOptions() {
    }

    public static final List<String> GENDERS = List.of(
            "Male", "Female", "Non-binary", "Prefer not to say"
    );

    public static final List<String> FITNESS_GOALS = List.of(
            "Weight loss",
            "Muscle gain",
            "Weight maintenance",
            "Strength improvement",
            "General fitness"
    );

    public static final List<String> EXPERIENCE_LEVELS = List.of(
            "Beginner", "Intermediate", "Advanced"
    );

    public static final List<String> WORKOUT_LOCATIONS = List.of(
            "Home", "Gym", "Both / Hybrid"
    );

    public static final List<String> EQUIPMENT_OPTIONS = List.of(
            "Dumbbells",
            "Barbell",
            "Resistance Bands",
            "Pull-up Bar",
            "Bench",
            "Cardio Machine",
            "None"
    );

    public static final List<Integer> WORKOUT_DURATIONS = List.of(
            15, 30, 45, 60, 90, 120
    );

    public static final List<String> DIET_PREFERENCES = List.of(
            "Vegetarian", "Non-vegetarian"
    );
}
