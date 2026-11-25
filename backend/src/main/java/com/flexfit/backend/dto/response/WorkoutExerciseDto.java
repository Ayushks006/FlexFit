package com.flexfit.backend.dto.response;

public class WorkoutExerciseDto {
    private String name;
    private String setsReps;
    private String restPeriod;
    private String notes;

    public WorkoutExerciseDto() {
    }

    public WorkoutExerciseDto(String name, String setsReps, String restPeriod, String notes) {
        this.name = name;
        this.setsReps = setsReps;
        this.restPeriod = restPeriod;
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSetsReps() {
        return setsReps;
    }

    public void setSetsReps(String setsReps) {
        this.setsReps = setsReps;
    }

    public String getRestPeriod() {
        return restPeriod;
    }

    public void setRestPeriod(String restPeriod) {
        this.restPeriod = restPeriod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
