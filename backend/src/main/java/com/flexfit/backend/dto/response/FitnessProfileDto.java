package com.flexfit.backend.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class FitnessProfileDto {
    private Long id;
    private Long userId;

    // Personal Information
    private Integer age;
    private String gender;
    private String district;
    private String state;

    // Body Information
    private BigDecimal heightCm;
    private BigDecimal currentWeightKg;
    private BigDecimal targetWeightKg;
    private BigDecimal bmi;

    // Fitness Information
    private String fitnessGoal;
    private String experienceLevel;

    // Workout and Diet Preferences
    private String workoutLocation;
    private List<String> availableEquipment;
    private Integer preferredWorkoutDurationMin;
    private String dietPreference;

    // Safety Information
    private String foodAllergies;
    private String dietaryRestrictions;
    private String injuriesLimitations;

    private Integer profileCompletionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FitnessProfileDto() {
    }

    public static BigDecimal calculateBmi(BigDecimal heightCm, BigDecimal weightKg) {
        if (heightCm == null || weightKg == null || heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal heightSquared = heightM.multiply(heightM);
        return weightKg.divide(heightSquared, 1, RoundingMode.HALF_UP);
    }

    public static int calculateCompletionPercentage(
            Integer age, String gender, String district, String state,
            BigDecimal heightCm, BigDecimal currentWeightKg, BigDecimal targetWeightKg,
            String fitnessGoal, String experienceLevel, String workoutLocation,
            List<String> availableEquipment, Integer preferredWorkoutDurationMin, String dietPreference,
            String foodAllergies, String dietaryRestrictions, String injuriesLimitations) {
        int total = 16;
        int completed = 0;
        if (age != null && age > 0) completed++;
        if (gender != null && !gender.isBlank()) completed++;
        if (district != null && !district.isBlank()) completed++;
        if (state != null && !state.isBlank()) completed++;
        if (heightCm != null && heightCm.compareTo(BigDecimal.ZERO) > 0) completed++;
        if (currentWeightKg != null && currentWeightKg.compareTo(BigDecimal.ZERO) > 0) completed++;
        if (targetWeightKg != null && targetWeightKg.compareTo(BigDecimal.ZERO) > 0) completed++;
        if (fitnessGoal != null && !fitnessGoal.isBlank()) completed++;
        if (experienceLevel != null && !experienceLevel.isBlank()) completed++;
        if (workoutLocation != null && !workoutLocation.isBlank()) completed++;
        if (availableEquipment != null && !availableEquipment.isEmpty()) completed++;
        if (preferredWorkoutDurationMin != null && preferredWorkoutDurationMin > 0) completed++;
        if (dietPreference != null && !dietPreference.isBlank()) completed++;
        if (foodAllergies != null && !foodAllergies.isBlank()) completed++;
        if (dietaryRestrictions != null && !dietaryRestrictions.isBlank()) completed++;
        if (injuriesLimitations != null && !injuriesLimitations.isBlank()) completed++;
        return (int) Math.round((completed * 100.0) / total);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getCurrentWeightKg() {
        return currentWeightKg;
    }

    public void setCurrentWeightKg(BigDecimal currentWeightKg) {
        this.currentWeightKg = currentWeightKg;
    }

    public BigDecimal getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(BigDecimal targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getWorkoutLocation() {
        return workoutLocation;
    }

    public void setWorkoutLocation(String workoutLocation) {
        this.workoutLocation = workoutLocation;
    }

    public List<String> getAvailableEquipment() {
        return availableEquipment;
    }

    public void setAvailableEquipment(List<String> availableEquipment) {
        this.availableEquipment = availableEquipment;
    }

    public Integer getPreferredWorkoutDurationMin() {
        return preferredWorkoutDurationMin;
    }

    public void setPreferredWorkoutDurationMin(Integer preferredWorkoutDurationMin) {
        this.preferredWorkoutDurationMin = preferredWorkoutDurationMin;
    }

    public String getDietPreference() {
        return dietPreference;
    }

    public void setDietPreference(String dietPreference) {
        this.dietPreference = dietPreference;
    }

    public String getFoodAllergies() {
        return foodAllergies;
    }

    public void setFoodAllergies(String foodAllergies) {
        this.foodAllergies = foodAllergies;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getInjuriesLimitations() {
        return injuriesLimitations;
    }

    public void setInjuriesLimitations(String injuriesLimitations) {
        this.injuriesLimitations = injuriesLimitations;
    }

    public Integer getProfileCompletionPercentage() {
        return profileCompletionPercentage;
    }

    public void setProfileCompletionPercentage(Integer profileCompletionPercentage) {
        this.profileCompletionPercentage = profileCompletionPercentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
