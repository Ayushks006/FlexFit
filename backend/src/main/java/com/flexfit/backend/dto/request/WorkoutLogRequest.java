package com.flexfit.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WorkoutLogRequest {

    @NotBlank(message = "Workout name is required")
    @Size(max = 120, message = "Workout name must not exceed 120 characters")
    private String title;

    @NotBlank(message = "Workout category is required")
    @Size(max = 50, message = "Workout category must not exceed 50 characters")
    private String workoutType;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 360, message = "Duration must not exceed 360 minutes")
    private Integer durationMin;

    @Min(value = 0, message = "Calories burned cannot be negative")
    @Max(value = 5000, message = "Calories burned must not exceed 5000")
    private Integer caloriesBurned;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    public WorkoutLogRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public Integer getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(Integer durationMin) {
        this.durationMin = durationMin;
    }

    public Integer getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(Integer caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
