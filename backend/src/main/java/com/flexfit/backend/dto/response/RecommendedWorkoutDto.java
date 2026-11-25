package com.flexfit.backend.dto.response;

import java.util.List;

public class RecommendedWorkoutDto {
    private String name;
    private String category;
    private String description;
    private int estimatedDurationMin;
    private String difficulty;
    private int approximateCalories;
    private List<WorkoutExerciseDto> exercises;
    private String safetyNote;

    public RecommendedWorkoutDto() {
    }

    public RecommendedWorkoutDto(String name, String category, String description,
                                 int estimatedDurationMin, String difficulty, int approximateCalories,
                                 List<WorkoutExerciseDto> exercises, String safetyNote) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.estimatedDurationMin = estimatedDurationMin;
        this.difficulty = difficulty;
        this.approximateCalories = approximateCalories;
        this.exercises = exercises;
        this.safetyNote = safetyNote;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(int estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getApproximateCalories() {
        return approximateCalories;
    }

    public void setApproximateCalories(int approximateCalories) {
        this.approximateCalories = approximateCalories;
    }

    public List<WorkoutExerciseDto> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutExerciseDto> exercises) {
        this.exercises = exercises;
    }

    public String getSafetyNote() {
        return safetyNote;
    }

    public void setSafetyNote(String safetyNote) {
        this.safetyNote = safetyNote;
    }
}
