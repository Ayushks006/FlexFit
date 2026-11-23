package com.flexfit.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FitnessProfileModel {
    private Long id;
    private Long userId;

    private Integer age;
    private String gender;
    private String district;
    private String state;

    private BigDecimal heightCm;
    private BigDecimal currentWeightKg;
    private BigDecimal targetWeightKg;
    private BigDecimal bmi;

    private String fitnessGoal;
    private String experienceLevel;

    private String workoutLocation;
    private List<String> availableEquipment = new ArrayList<>();
    private Integer preferredWorkoutDurationMin;
    private String dietPreference;

    private String foodAllergies;
    private String dietaryRestrictions;
    private String injuriesLimitations;

    private Integer profileCompletionPercentage = 100;

    public FitnessProfileModel() {
    }

    public BigDecimal calculateBmi() {
        if (heightCm == null || currentWeightKg == null || heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal heightSquared = heightM.multiply(heightM);
        return currentWeightKg.divide(heightSquared, 1, RoundingMode.HALF_UP);
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
        if (bmi == null || bmi.compareTo(BigDecimal.ZERO) <= 0) {
            bmi = calculateBmi();
        }
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
        this.availableEquipment = availableEquipment != null ? availableEquipment : new ArrayList<>();
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
        return profileCompletionPercentage != null ? profileCompletionPercentage : 100;
    }

    public void setProfileCompletionPercentage(Integer profileCompletionPercentage) {
        this.profileCompletionPercentage = profileCompletionPercentage;
    }
}
