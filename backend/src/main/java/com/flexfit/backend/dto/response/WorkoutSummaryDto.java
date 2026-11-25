package com.flexfit.backend.dto.response;

import java.util.List;

public class WorkoutSummaryDto {
    private long totalWorkouts;
    private int weeklyWorkouts;
    private int weeklyDurationMin;
    private int totalDurationMin;
    private int weeklyCaloriesBurned;
    private int totalCaloriesBurned;
    private double weeklyFrequencyPerWeek;
    private List<WeeklyDayActivityDto> weeklyActivity;

    public WorkoutSummaryDto() {
    }

    public long getTotalWorkouts() {
        return totalWorkouts;
    }

    public void setTotalWorkouts(long totalWorkouts) {
        this.totalWorkouts = totalWorkouts;
    }

    public int getWeeklyWorkouts() {
        return weeklyWorkouts;
    }

    public void setWeeklyWorkouts(int weeklyWorkouts) {
        this.weeklyWorkouts = weeklyWorkouts;
    }

    public int getWeeklyDurationMin() {
        return weeklyDurationMin;
    }

    public void setWeeklyDurationMin(int weeklyDurationMin) {
        this.weeklyDurationMin = weeklyDurationMin;
    }

    public int getTotalDurationMin() {
        return totalDurationMin;
    }

    public void setTotalDurationMin(int totalDurationMin) {
        this.totalDurationMin = totalDurationMin;
    }

    public int getWeeklyCaloriesBurned() {
        return weeklyCaloriesBurned;
    }

    public void setWeeklyCaloriesBurned(int weeklyCaloriesBurned) {
        this.weeklyCaloriesBurned = weeklyCaloriesBurned;
    }

    public int getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public void setTotalCaloriesBurned(int totalCaloriesBurned) {
        this.totalCaloriesBurned = totalCaloriesBurned;
    }

    public double getWeeklyFrequencyPerWeek() {
        return weeklyFrequencyPerWeek;
    }

    public void setWeeklyFrequencyPerWeek(double weeklyFrequencyPerWeek) {
        this.weeklyFrequencyPerWeek = weeklyFrequencyPerWeek;
    }

    public List<WeeklyDayActivityDto> getWeeklyActivity() {
        return weeklyActivity;
    }

    public void setWeeklyActivity(List<WeeklyDayActivityDto> weeklyActivity) {
        this.weeklyActivity = weeklyActivity;
    }
}
