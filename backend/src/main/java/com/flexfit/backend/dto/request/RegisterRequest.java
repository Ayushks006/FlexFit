package com.flexfit.backend.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object capturing all 6 steps of the FlexFit user registration.
 */
public class RegisterRequest {

    // Step 1: Account Information
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain alphanumeric characters, dots, underscores, and dashes")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    // Step 2: Personal Information
    @NotNull(message = "Age is required")
    @Min(value = 12, message = "Age must be at least 12")
    @Max(value = 100, message = "Age must not exceed 100")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "District is required")
    @Size(max = 50, message = "District must not exceed 50 characters")
    private String district;

    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    // Step 3: Body Information
    @NotNull(message = "Height is required")
    @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
    @DecimalMax(value = "260.0", message = "Height must not exceed 260 cm")
    private BigDecimal heightCm;

    @NotNull(message = "Current weight is required")
    @DecimalMin(value = "25.0", message = "Weight must be at least 25 kg")
    @DecimalMax(value = "300.0", message = "Weight must not exceed 300 kg")
    private BigDecimal currentWeightKg;

    @NotNull(message = "Target weight is required")
    @DecimalMin(value = "25.0", message = "Target weight must be at least 25 kg")
    @DecimalMax(value = "300.0", message = "Target weight must not exceed 300 kg")
    private BigDecimal targetWeightKg;

    // Step 4: Fitness Information
    @NotBlank(message = "Fitness goal is required")
    private String fitnessGoal;

    @NotBlank(message = "Experience level is required")
    private String experienceLevel;

    // Step 5: Workout and Diet Preferences
    @NotBlank(message = "Workout location is required")
    private String workoutLocation;

    private List<String> availableEquipment;

    @NotNull(message = "Preferred workout duration is required")
    @Min(value = 10, message = "Duration must be at least 10 minutes")
    @Max(value = 180, message = "Duration must not exceed 180 minutes")
    private Integer preferredWorkoutDurationMin;

    @NotBlank(message = "Diet preference is required")
    private String dietPreference;

    // Step 6: Safety Information
    private String foodAllergies;
    private String dietaryRestrictions;
    private String injuriesLimitations;

    public RegisterRequest() {
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
}
