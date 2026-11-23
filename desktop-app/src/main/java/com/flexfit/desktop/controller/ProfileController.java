package com.flexfit.desktop.controller;

import com.flexfit.desktop.model.FitnessProfileModel;
import com.flexfit.desktop.model.UserSession;
import com.flexfit.desktop.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileController {

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label ageGenderLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label heightLabel;

    @FXML
    private Label currentWeightLabel;

    @FXML
    private Label targetWeightLabel;

    @FXML
    private Label bmiLabel;

    @FXML
    private Label fitnessGoalLabel;

    @FXML
    private Label experienceLabel;

    @FXML
    private Label workoutLocationLabel;

    @FXML
    private Label equipmentLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private Label dietLabel;

    @FXML
    private Label allergiesLabel;

    @FXML
    private Label restrictionsLabel;

    @FXML
    private Label injuriesLabel;

    private DashboardController parentDashboard;

    public void setParentDashboard(DashboardController dashboard) {
        this.parentDashboard = dashboard;
    }

    @FXML
    public void initialize() {
        populateProfile();
    }

    public void populateProfile() {
        UserSession session = SessionManager.getInstance().getSession();
        if (session == null) return;

        fullNameLabel.setText(session.getFullName() != null ? session.getFullName() : "--");
        usernameLabel.setText("@" + (session.getUsername() != null ? session.getUsername() : "--"));
        emailLabel.setText(session.getEmail() != null ? session.getEmail() : "--");

        FitnessProfileModel p = session.getProfile();
        if (p != null) {
            ageGenderLabel.setText(p.getAge() + " yrs • " + p.getGender());
            locationLabel.setText(p.getDistrict() + ", " + p.getState());

            heightLabel.setText(p.getHeightCm() + " cm");
            currentWeightLabel.setText(p.getCurrentWeightKg() + " kg");
            targetWeightLabel.setText(p.getTargetWeightKg() + " kg");
            bmiLabel.setText(p.getBmi() != null ? p.getBmi().toString() : "--");

            fitnessGoalLabel.setText(p.getFitnessGoal() != null ? p.getFitnessGoal() : "--");
            experienceLabel.setText(p.getExperienceLevel() != null ? p.getExperienceLevel() : "--");
            workoutLocationLabel.setText(p.getWorkoutLocation() != null ? p.getWorkoutLocation() : "--");

            if (p.getAvailableEquipment() != null && !p.getAvailableEquipment().isEmpty()) {
                equipmentLabel.setText(String.join(", ", p.getAvailableEquipment()));
            } else {
                equipmentLabel.setText("None");
            }

            durationLabel.setText(p.getPreferredWorkoutDurationMin() + " min");
            dietLabel.setText(p.getDietPreference() != null ? p.getDietPreference() : "--");

            allergiesLabel.setText(p.getFoodAllergies() != null ? p.getFoodAllergies() : "None");
            restrictionsLabel.setText(p.getDietaryRestrictions() != null ? p.getDietaryRestrictions() : "None");
            injuriesLabel.setText(p.getInjuriesLimitations() != null ? p.getInjuriesLimitations() : "None");
        }
    }

    @FXML
    private void handleNavigateEditProfile() {
        if (parentDashboard != null) {
            parentDashboard.loadEditProfileView();
        }
    }
}
