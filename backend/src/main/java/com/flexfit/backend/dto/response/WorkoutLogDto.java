package com.flexfit.backend.dto.response;

import java.time.LocalDateTime;

public class WorkoutLogDto {
    private Long id;
    private String title;
    private String workoutType;
    private Integer durationMin;
    private Integer caloriesBurned;
    private LocalDateTime completedAt;
    private String notes;

    public WorkoutLogDto() {
    }

    public WorkoutLogDto(Long id, String title, String workoutType, Integer durationMin,
                         Integer caloriesBurned, LocalDateTime completedAt, String notes) {
        this.id = id;
        this.title = title;
        this.workoutType = workoutType;
        this.durationMin = durationMin;
        this.caloriesBurned = caloriesBurned;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
