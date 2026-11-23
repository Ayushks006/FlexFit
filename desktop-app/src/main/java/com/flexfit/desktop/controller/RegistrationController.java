package com.flexfit.desktop.controller;

import com.flexfit.desktop.model.RegistrationData;
import com.flexfit.desktop.model.UserSession;
import com.flexfit.desktop.service.BackendClient;
import com.flexfit.desktop.service.ScreenManager;
import com.flexfit.desktop.service.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RegistrationController {

    @FXML
    private ProgressBar stepProgressBar;

    @FXML
    private Label stepIndicatorLabel;

    @FXML
    private Label stepTitleLabel;

    @FXML
    private VBox errorBox;

    @FXML
    private Label errorLabel;

    // Panels for Steps 1-6
    @FXML
    private VBox step1Panel;

    @FXML
    private VBox step2Panel;

    @FXML
    private VBox step3Panel;

    @FXML
    private VBox step4Panel;

    @FXML
    private VBox step5Panel;

    @FXML
    private VBox step6Panel;

    // Step 1 Controls
    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField confirmPasswordVisibleField;

    @FXML
    private CheckBox passwordToggleCheckbox;

    // Step 2 Controls
    @FXML
    private Spinner<Integer> ageSpinner;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private TextField districtField;

    @FXML
    private TextField stateField;

    // Step 3 Controls
    @FXML
    private TextField heightField;

    @FXML
    private TextField currentWeightField;

    @FXML
    private TextField targetWeightField;

    // Step 4 Controls
    @FXML
    private ComboBox<String> fitnessGoalComboBox;

    @FXML
    private ComboBox<String> experienceLevelComboBox;

    // Step 5 Controls
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

    // Step 6 Controls
    @FXML
    private TextArea allergiesArea;

    @FXML
    private TextArea restrictionsArea;

    @FXML
    private TextArea injuriesArea;

    // Navigation Buttons
    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    private int currentStep = 1;
    private final int totalSteps = 6;
    private final RegistrationData data = new RegistrationData();
    private final BackendClient backendClient = new BackendClient();

    @FXML
    public void initialize() {
        setupFormControls();
        updateStepView();
    }

    private void setupFormControls() {
        // Step 1 Password Toggles
        passwordVisibleField.managedProperty().bind(passwordToggleCheckbox.selectedProperty());
        passwordVisibleField.visibleProperty().bind(passwordToggleCheckbox.selectedProperty());
        passwordField.managedProperty().bind(passwordToggleCheckbox.selectedProperty().not());
        passwordField.visibleProperty().bind(passwordToggleCheckbox.selectedProperty().not());
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        confirmPasswordVisibleField.managedProperty().bind(passwordToggleCheckbox.selectedProperty());
        confirmPasswordVisibleField.visibleProperty().bind(passwordToggleCheckbox.selectedProperty());
        confirmPasswordField.managedProperty().bind(passwordToggleCheckbox.selectedProperty().not());
        confirmPasswordField.visibleProperty().bind(passwordToggleCheckbox.selectedProperty().not());
        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        // Step 2 Age Spinner & Gender ComboBox
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(12, 100, 25));
        genderComboBox.getItems().addAll("Male", "Female", "Non-binary", "Prefer not to say");
        genderComboBox.setValue("Male");

        // Step 4 Options
        fitnessGoalComboBox.getItems().addAll(
                "Weight loss",
                "Muscle gain",
                "Weight maintenance",
                "Strength improvement",
                "General fitness"
        );
        fitnessGoalComboBox.setValue("Weight loss");

        experienceLevelComboBox.getItems().addAll("Beginner", "Intermediate", "Advanced");
        experienceLevelComboBox.setValue("Beginner");

        // Step 5 Options
        workoutLocationComboBox.getItems().addAll("Home", "Gym", "Both / Hybrid");
        workoutLocationComboBox.setValue("Gym");

        durationComboBox.getItems().addAll(15, 30, 45, 60, 90, 120);
        durationComboBox.setValue(45);

        dietPreferenceComboBox.getItems().addAll("Vegetarian", "Non-vegetarian");
        dietPreferenceComboBox.setValue("Non-vegetarian");
    }

    @FXML
    private void handleNext() {
        hideError();
        if (!validateAndSaveCurrentStep()) {
            return;
        }

        if (currentStep < totalSteps) {
            currentStep++;
            updateStepView();
        } else {
            submitRegistration();
        }
    }

    @FXML
    private void handlePrevious() {
        hideError();
        if (currentStep > 1) {
            currentStep--;
            updateStepView();
        }
    }

    @FXML
    private void handleCancel() {
        ScreenManager.getInstance().showLogin();
    }

    private boolean validateAndSaveCurrentStep() {
        switch (currentStep) {
            case 1 -> {
                data.setFullName(fullNameField.getText());
                data.setUsername(usernameField.getText());
                data.setEmail(emailField.getText());
                data.setPassword(passwordField.getText());
                data.setConfirmPassword(confirmPasswordField.getText());
                List<String> errors = data.validateStep1();
                if (!errors.isEmpty()) {
                    showError(String.join("\n", errors));
                    return false;
                }
            }
            case 2 -> {
                data.setAge(ageSpinner.getValue());
                data.setGender(genderComboBox.getValue());
                data.setDistrict(districtField.getText());
                data.setState(stateField.getText());
                List<String> errors = data.validateStep2();
                if (!errors.isEmpty()) {
                    showError(String.join("\n", errors));
                    return false;
                }
            }
            case 3 -> {
                try {
                    data.setHeightCm(new BigDecimal(heightField.getText().trim()));
                    data.setCurrentWeightKg(new BigDecimal(currentWeightField.getText().trim()));
                    data.setTargetWeightKg(new BigDecimal(targetWeightField.getText().trim()));
                } catch (Exception e) {
                    showError("Please enter valid decimal numbers for height and weights (e.g. 175.5, 72.0).");
                    return false;
                }
                List<String> errors = data.validateStep3();
                if (!errors.isEmpty()) {
                    showError(String.join("\n", errors));
                    return false;
                }
            }
            case 4 -> {
                data.setFitnessGoal(fitnessGoalComboBox.getValue());
                data.setExperienceLevel(experienceLevelComboBox.getValue());
                List<String> errors = data.validateStep4();
                if (!errors.isEmpty()) {
                    showError(String.join("\n", errors));
                    return false;
                }
            }
            case 5 -> {
                data.setWorkoutLocation(workoutLocationComboBox.getValue());
                data.setPreferredWorkoutDurationMin(durationComboBox.getValue());
                data.setDietPreference(dietPreferenceComboBox.getValue());

                List<String> equipment = new ArrayList<>();
                if (eqDumbbells.isSelected()) equipment.add("Dumbbells");
                if (eqBarbell.isSelected()) equipment.add("Barbell");
                if (eqBands.isSelected()) equipment.add("Resistance Bands");
                if (eqPullup.isSelected()) equipment.add("Pull-up Bar");
                if (eqBench.isSelected()) equipment.add("Workout Bench");
                if (eqCardio.isSelected()) equipment.add("Cardio Machine");
                if (eqNone.isSelected() || equipment.isEmpty()) equipment.add("Bodyweight / None");
                data.setAvailableEquipment(equipment);

                List<String> errors = data.validateStep5();
                if (!errors.isEmpty()) {
                    showError(String.join("\n", errors));
                    return false;
                }
            }
            case 6 -> {
                data.setFoodAllergies(allergiesArea.getText() != null ? allergiesArea.getText().trim() : "None");
                data.setDietaryRestrictions(restrictionsArea.getText() != null ? restrictionsArea.getText().trim() : "None");
                data.setInjuriesLimitations(injuriesArea.getText() != null ? injuriesArea.getText().trim() : "None");
            }
        }
        return true;
    }

    private void updateStepView() {
        stepProgressBar.setProgress((double) currentStep / totalSteps);
        stepIndicatorLabel.setText("STEP " + currentStep + " OF " + totalSteps);

        step1Panel.setVisible(currentStep == 1);
        step1Panel.setManaged(currentStep == 1);
        step2Panel.setVisible(currentStep == 2);
        step2Panel.setManaged(currentStep == 2);
        step3Panel.setVisible(currentStep == 3);
        step3Panel.setManaged(currentStep == 3);
        step4Panel.setVisible(currentStep == 4);
        step4Panel.setManaged(currentStep == 4);
        step5Panel.setVisible(currentStep == 5);
        step5Panel.setManaged(currentStep == 5);
        step6Panel.setVisible(currentStep == 6);
        step6Panel.setManaged(currentStep == 6);

        prevButton.setDisable(currentStep == 1);

        switch (currentStep) {
            case 1 -> stepTitleLabel.setText("Account Credentials");
            case 2 -> stepTitleLabel.setText("Personal Demographics");
            case 3 -> stepTitleLabel.setText("Biometrics & Body Dimensions");
            case 4 -> stepTitleLabel.setText("Fitness Goals & Experience");
            case 5 -> stepTitleLabel.setText("Workout & Dietary Preferences");
            case 6 -> stepTitleLabel.setText("Health & Safety Considerations");
        }

        if (currentStep == totalSteps) {
            nextButton.setText("Complete Registration");
        } else {
            nextButton.setText("Continue →");
        }
    }

    private void submitRegistration() {
        setLoading(true);

        backendClient.registerAsync(data)
                .thenAccept(userSession -> Platform.runLater(() -> {
                    setLoading(false);
                    SessionManager.getInstance().startSession(userSession);
                    ScreenManager.getInstance().showRegistrationSuccess();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        String msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                        showError(msg != null ? msg : "Registration failed. Please review your entries and try again.");
                    });
                    return null;
                });
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
        nextButton.setDisable(loading);
        prevButton.setDisable(loading || currentStep == 1);
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
    }
}
