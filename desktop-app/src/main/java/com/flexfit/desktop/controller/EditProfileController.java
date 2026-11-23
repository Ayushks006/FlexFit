package com.flexfit.desktop.controller;

import com.flexfit.desktop.model.FitnessProfileModel;
import com.flexfit.desktop.model.UserSession;
import com.flexfit.desktop.service.BackendClient;
import com.flexfit.desktop.service.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EditProfileController {

    @FXML
    private Spinner<Integer> ageSpinner;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private TextField districtField;

    @FXML
    private TextField stateField;

    @FXML
    private TextField heightField;

    @FXML
    private TextField currentWeightField;

    @FXML
    private TextField targetWeightField;

    @FXML
    private ComboBox<String> fitnessGoalComboBox;

    @FXML
    private ComboBox<String> experienceLevelComboBox;

    @FXML
    private ComboBox<String> workoutLocationComboBox;

    @FXML
    private CheckBox eqDumbbells;

    @FXML
    private CheckBox eqBarbell;

    @FXML
    private CheckBox eqBands;

    @FXML
    private CheckBox eqPullup;

    @FXML
    private CheckBox eqBench;

    @FXML
    private CheckBox eqCardio;

    @FXML
    private CheckBox eqNone;

    @FXML
    private ComboBox<Integer> durationComboBox;

    @FXML
    private ComboBox<String> dietPreferenceComboBox;

    @FXML
    private TextArea allergiesArea;

    @FXML
    private TextArea restrictionsArea;

    @FXML
    private TextArea injuriesArea;

    @FXML
    private VBox errorBox;

    @FXML
    private Label errorLabel;

    @FXML
    private Button saveButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    private DashboardController parentDashboard;
    private final BackendClient backendClient = new BackendClient();

    public void setParentDashboard(DashboardController dashboard) {
        this.parentDashboard = dashboard;
    }

    @FXML
    public void initialize() {
        // Setup options
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(12, 100, 25));
        genderComboBox.getItems().addAll("Male", "Female", "Non-binary", "Prefer not to say");

        fitnessGoalComboBox.getItems().addAll(
                "Weight loss",
                "Muscle gain",
                "Weight maintenance",
                "Strength improvement",
                "General fitness"
        );

        experienceLevelComboBox.getItems().addAll("Beginner", "Intermediate", "Advanced");
        workoutLocationComboBox.getItems().addAll("Home", "Gym", "Both / Hybrid");
        durationComboBox.getItems().addAll(15, 30, 45, 60, 90, 120);
        dietPreferenceComboBox.getItems().addAll("Vegetarian", "Non-vegetarian");

        loadCurrentValues();
    }

    private void loadCurrentValues() {
        UserSession session = SessionManager.getInstance().getSession();
        if (session == null || session.getProfile() == null) return;

        FitnessProfileModel p = session.getProfile();
        if (p.getAge() != null) ageSpinner.getValueFactory().setValue(p.getAge());
        if (p.getGender() != null) genderComboBox.setValue(p.getGender());
        if (p.getDistrict() != null) districtField.setText(p.getDistrict());
        if (p.getState() != null) stateField.setText(p.getState());

        if (p.getHeightCm() != null) heightField.setText(p.getHeightCm().toString());
        if (p.getCurrentWeightKg() != null) currentWeightField.setText(p.getCurrentWeightKg().toString());
        if (p.getTargetWeightKg() != null) targetWeightField.setText(p.getTargetWeightKg().toString());

        if (p.getFitnessGoal() != null) fitnessGoalComboBox.setValue(p.getFitnessGoal());
        if (p.getExperienceLevel() != null) experienceLevelComboBox.setValue(p.getExperienceLevel());
        if (p.getWorkoutLocation() != null) workoutLocationComboBox.setValue(p.getWorkoutLocation());
        if (p.getPreferredWorkoutDurationMin() != null) durationComboBox.setValue(p.getPreferredWorkoutDurationMin());
        if (p.getDietPreference() != null) dietPreferenceComboBox.setValue(p.getDietPreference());

        List<String> eq = p.getAvailableEquipment();
        if (eq != null) {
            eqDumbbells.setSelected(eq.contains("Dumbbells"));
            eqBarbell.setSelected(eq.contains("Barbell"));
            eqBands.setSelected(eq.contains("Resistance Bands"));
            eqPullup.setSelected(eq.contains("Pull-up Bar"));
            eqBench.setSelected(eq.contains("Workout Bench"));
            eqCardio.setSelected(eq.contains("Cardio Machine"));
            eqNone.setSelected(eq.contains("Bodyweight / None"));
        }

        if (p.getFoodAllergies() != null) allergiesArea.setText(p.getFoodAllergies());
        if (p.getDietaryRestrictions() != null) restrictionsArea.setText(p.getDietaryRestrictions());
        if (p.getInjuriesLimitations() != null) injuriesArea.setText(p.getInjuriesLimitations());
    }

    @FXML
    private void handleSave() {
        hideError();

        BigDecimal height;
        BigDecimal currentWeight;
        BigDecimal targetWeight;

        try {
            height = new BigDecimal(heightField.getText().trim());
            currentWeight = new BigDecimal(currentWeightField.getText().trim());
            targetWeight = new BigDecimal(targetWeightField.getText().trim());
        } catch (Exception e) {
            showError("Height and weights must be valid numbers (e.g. 175.0, 70.5).");
            return;
        }

        if (districtField.getText().trim().isBlank() || stateField.getText().trim().isBlank()) {
            showError("District and State are required fields.");
            return;
        }

        FitnessProfileModel model = new FitnessProfileModel();
        model.setAge(ageSpinner.getValue());
        model.setGender(genderComboBox.getValue());
        model.setDistrict(districtField.getText().trim());
        model.setState(stateField.getText().trim());
        model.setHeightCm(height);
        model.setCurrentWeightKg(currentWeight);
        model.setTargetWeightKg(targetWeight);
        model.setFitnessGoal(fitnessGoalComboBox.getValue());
        model.setExperienceLevel(experienceLevelComboBox.getValue());
        model.setWorkoutLocation(workoutLocationComboBox.getValue());
        model.setPreferredWorkoutDurationMin(durationComboBox.getValue());
        model.setDietPreference(dietPreferenceComboBox.getValue());

        List<String> eq = new ArrayList<>();
        if (eqDumbbells.isSelected()) eq.add("Dumbbells");
        if (eqBarbell.isSelected()) eq.add("Barbell");
        if (eqBands.isSelected()) eq.add("Resistance Bands");
        if (eqPullup.isSelected()) eq.add("Pull-up Bar");
        if (eqBench.isSelected()) eq.add("Workout Bench");
        if (eqCardio.isSelected()) eq.add("Cardio Machine");
        if (eqNone.isSelected() || eq.isEmpty()) eq.add("Bodyweight / None");
        model.setAvailableEquipment(eq);

        model.setFoodAllergies(allergiesArea.getText() != null ? allergiesArea.getText().trim() : "None");
        model.setDietaryRestrictions(restrictionsArea.getText() != null ? restrictionsArea.getText().trim() : "None");
        model.setInjuriesLimitations(injuriesArea.getText() != null ? injuriesArea.getText().trim() : "None");

        String token = SessionManager.getInstance().getToken();
        if (token == null) {
            showError("Session expired. Please sign in again.");
            return;
        }

        setLoading(true);

        backendClient.updateProfileAsync(token, model)
                .thenAccept(updatedProfile -> Platform.runLater(() -> {
                    setLoading(false);
                    SessionManager.getInstance().updateProfile(updatedProfile);
                    if (parentDashboard != null) {
                        parentDashboard.loadMyProfileView();
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        String msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                        showError(msg != null ? msg : "Failed to save profile changes.");
                    });
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        if (parentDashboard != null) {
            parentDashboard.loadMyProfileView();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    private void hideError() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }

    private void setLoading(boolean loading) {
        saveButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
    }
}
