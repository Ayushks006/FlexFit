package com.flexfit.backend.dto.response;

import java.time.LocalDate;

public class WeeklyDayActivityDto {
    private String dayLabel;
    private LocalDate date;
    private int sessions;
    private int totalMinutes;
    private int totalCalories;
    private int heightPercent;

    public WeeklyDayActivityDto() {
    }

    public WeeklyDayActivityDto(String dayLabel, LocalDate date, int sessions,
                                int totalMinutes, int totalCalories, int heightPercent) {
        this.dayLabel = dayLabel;
        this.date = date;
        this.sessions = sessions;
        this.totalMinutes = totalMinutes;
        this.totalCalories = totalCalories;
        this.heightPercent = heightPercent;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getSessions() {
        return sessions;
    }

    public void setSessions(int sessions) {
        this.sessions = sessions;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }

    public int getHeightPercent() {
        return heightPercent;
    }

    public void setHeightPercent(int heightPercent) {
        this.heightPercent = heightPercent;
    }
}
