package com.flexfit.desktop.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegistrationData {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    // Step 1: Account
    private String fullName = "";
    private String username = "";
    private String email = "";
    private String password = "";
    private String confirmPassword = "";

    // Step 2: Personal
    private Integer age = 25;
    private String gender = "Male";
    private String district = "";
    private String state = "";

    // Step 3: Body
    private BigDecimal heightCm = new BigDecimal("175.0");
    private BigDecimal currentWeightKg = new BigDecimal("70.0");
    private BigDecimal targetWeightKg = new BigDecimal("68.0");

    // Step 4: Fitness
    private String fitnessGoal = "Weight loss";
    private String experienceLevel = "Beginner";

    // Step 5: Workout & Diet
    private String workoutLocation = "Home";
    private List<String> availableEquipment = new ArrayList<>();
    private Integer preferredWorkoutDurationMin = 30;
    private String dietPreference = "Vegetarian";

    // Step 6: Safety
    private String foodAllergies = "";
    private String dietaryRestrictions = "";
    private String injuriesLimitations = "";

    public RegistrationData() {
    }

    public List<String> validateStep1() {
        List<String> errors = new ArrayList<>();
        if (fullName == null || fullName.trim().length() < 2) {
            errors.add("Full name must be at least 2 characters.");
        }
        if (username == null || username.trim().length() < 3) {
            errors.add("Username must be at least 3 characters.");
        } else if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            errors.add("Username can only contain alphanumeric characters, dots, underscores, and dashes.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.add("A valid email address is required.");
        }
        if (password == null || password.length() < 6) {
            errors.add("Password must be at least 6 characters.");
        }
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.add("Passwords do not match.");
        }
        return errors;
    }

    public List<String> validateStep2() {
        List<String> errors = new ArrayList<>();
        if (age == null || age < 12 || age > 100) {
            errors.add("Age must be between 12 and 100.");
        }
        if (gender == null || gender.isBlank()) {
            errors.add("Gender selection is required.");
        }
        if (district == null || district.trim().isBlank()) {
            errors.add("District is required.");
        }
        if (state == null || state.trim().isBlank()) {
            errors.add("State is required.");
        }
        return errors;
    }

    public List<String> validateStep3() {
        List<String> errors = new ArrayList<>();
        if (heightCm == null || heightCm.compareTo(new BigDecimal("50")) < 0 || heightCm.compareTo(new BigDecimal("260")) > 0) {
            errors.add("Height must be between 50 cm and 260 cm.");
        }
        if (currentWeightKg == null || currentWeightKg.compareTo(new BigDecimal("25")) < 0 || currentWeightKg.compareTo(new BigDecimal("300")) > 0) {
            errors.add("Current weight must be between 25 kg and 300 kg.");
        }
        if (targetWeightKg == null || targetWeightKg.compareTo(new BigDecimal("25")) < 0 || targetWeightKg.compareTo(new BigDecimal("300")) > 0) {
            errors.add("Target weight must be between 25 kg and 300 kg.");
        }
        return errors;
    }

    public List<String> validateStep4() {
        List<String> errors = new ArrayList<>();
        if (fitnessGoal == null || fitnessGoal.isBlank()) {
            errors.add("Fitness goal is required.");
        }
        if (experienceLevel == null || experienceLevel.isBlank()) {
            errors.add("Experience level is required.");
        }
        return errors;
    }

    public List<String> validateStep5() {
        List<String> errors = new ArrayList<>();
        if (workoutLocation == null || workoutLocation.isBlank()) {
            errors.add("Workout location is required.");
        }
        if (preferredWorkoutDurationMin == null || preferredWorkoutDurationMin < 10 || preferredWorkoutDurationMin > 180) {
            errors.add("Workout duration must be between 10 and 180 minutes.");
        }
        if (dietPreference == null || dietPreference.isBlank()) {
            errors.add("Diet preference is required.");
        }
        return errors;
    }

    public List<String> validateStep6() {
        return new ArrayList<>(); // Safety information is optional
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
