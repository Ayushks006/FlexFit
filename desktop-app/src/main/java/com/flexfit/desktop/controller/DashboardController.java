package com.flexfit.desktop.controller;

import com.flexfit.desktop.model.FitnessProfileModel;
import com.flexfit.desktop.model.UserSession;
import com.flexfit.desktop.service.BackendClient;
import com.flexfit.desktop.service.ScreenManager;
import com.flexfit.desktop.service.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label usernameBadgeLabel;

    @FXML
    private Label goalMetricLabel;

    @FXML
    private Label currentWeightMetricLabel;

    @FXML
    private Label targetWeightMetricLabel;

    @FXML
    private Label heightMetricLabel;

    @FXML
    private Label experienceMetricLabel;

    @FXML
    private Label bmiMetricLabel;

    @FXML
    private Label completionPercentLabel;

    @FXML
    private Label completionSubtitleLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox overviewContent;

    // Sidebar navigation buttons
    @FXML
    private Button navDashboardBtn;

    @FXML
    private Button navCoachBtn;

    @FXML
    private Button navWorkoutsBtn;

    @FXML
    private Button navDietBtn;

    @FXML
    private Button navChallengesBtn;

    @FXML
    private Button navLeaderboardBtn;

    @FXML
    private Button navProgressBtn;

    @FXML
    private Button navAchievementsBtn;

    @FXML
    private Button navProfileBtn;

    @FXML
    private Button navSettingsBtn;

    private final BackendClient backendClient = new BackendClient();

    @FXML
    public void initialize() {
        SessionManager.getInstance().addSessionListener(session -> Platform.runLater(this::updateDashboardMetrics));
        updateDashboardMetrics();
    }

    private void updateDashboardMetrics() {
        UserSession session = SessionManager.getInstance().getSession();
        if (session == null) return;

        welcomeLabel.setText("Welcome back, " + (session.getFullName() != null ? session.getFullName() : "Athlete") + "!");
        usernameBadgeLabel.setText("@" + (session.getUsername() != null ? session.getUsername() : "athlete"));

        FitnessProfileModel p = session.getProfile();
        if (p != null) {
            goalMetricLabel.setText(p.getFitnessGoal() != null ? p.getFitnessGoal() : "--");
            currentWeightMetricLabel.setText(p.getCurrentWeightKg() != null ? p.getCurrentWeightKg() + " kg" : "--");
            targetWeightMetricLabel.setText(p.getTargetWeightKg() != null ? p.getTargetWeightKg() + " kg" : "--");
            heightMetricLabel.setText(p.getHeightCm() != null ? p.getHeightCm() + " cm" : "--");
            experienceMetricLabel.setText(p.getExperienceLevel() != null ? p.getExperienceLevel() : "--");
            bmiMetricLabel.setText(p.getBmi() != null ? p.getBmi().toString() : "--");
            int completion = p.getProfileCompletionPercentage();
            completionPercentLabel.setText(completion + "%");
            if (completionSubtitleLabel != null) {
                if (completion >= 100) {
                    completionSubtitleLabel.setText("Your fitness profile is complete!");
                    completionSubtitleLabel.setStyle("-fx-text-fill: #00E676; -fx-font-size: 12px; -fx-font-weight: 700;");
                } else {
                    completionSubtitleLabel.setText(completion + "% Profile Completed");
                    completionSubtitleLabel.setStyle("-fx-text-fill: #00E5FF; -fx-font-size: 12px; -fx-font-weight: 700;");
                }
            }
        }
    }

    @FXML
    public void handleNavDashboard() {
        setActiveNav(navDashboardBtn);
        contentArea.getChildren().setAll(overviewContent);
    }

    @FXML
    public void handleNavCoach() {
        setActiveNav(navCoachBtn);
        loadPlaceholder(
                "AI Fitness Coach",
                "COMING IN PHASE 3",
                "Personalized AI coaching with Gemini Live and Scikit-learn recommendation models will be integrated in Phase 3."
        );
    }

    @FXML
    public void handleNavWorkouts() {
        setActiveNav(navWorkoutsBtn);
        loadPlaceholder(
                "Workout Planner",
                "COMING IN PHASE 3",
                "Dynamic workout plan generation based on your biometrics, available equipment, and preferred duration."
        );
    }

    @FXML
    public void handleNavDiet() {
        setActiveNav(navDietBtn);
        loadPlaceholder(
                "Diet & Nutrition Planner",
                "COMING IN PHASE 3",
                "Tailored macronutrient distribution, calorie tracking, and customized meal plans matching your dietary preferences."
        );
    }

    @FXML
    public void handleNavChallenges() {
        setActiveNav(navChallengesBtn);
        loadPlaceholder(
                "Daily & Weekly Challenges",
                "COMING IN PHASE 4",
                "Community fitness challenges, daily streaks, workout consistency quests, and bonus XP multipliers."
        );
    }

    @FXML
    public void handleNavLeaderboard() {
        setActiveNav(navLeaderboardBtn);
        loadPlaceholder(
                "Competitive Leaderboards",
                "COMING IN PHASE 4",
                "Global, regional, and tier-based athlete rankings (Diamond, Master, Elite) celebrating consistency and achievements."
        );
    }

    @FXML
    public void handleNavProgress() {
        setActiveNav(navProgressBtn);
        loadPlaceholder(
                "Progress & Biometric Tracker",
                "COMING IN PHASE 5",
                "Interactive time-series biometric charts tracking weight changes, workout volume, calories, and body measurements."
        );
    }

    @FXML
    public void handleNavAchievements() {
        setActiveNav(navAchievementsBtn);
        loadPlaceholder(
                "Achievements & Trophies",
                "COMING IN PHASE 5",
                "Unlockable gamer badges, milestones, milestone certificates, and workout leveling perks."
        );
    }

    @FXML
    public void handleNavProfile() {
        setActiveNav(navProfileBtn);
        loadMyProfileView();
    }

    @FXML
    public void handleNavSettings() {
        setActiveNav(navSettingsBtn);
        loadSettingsView();
    }

    @FXML
    public void handleLogout() {
        backendClient.logoutAsync().whenComplete((res, err) -> Platform.runLater(() -> {
            SessionManager.getInstance().clearSession();
            ScreenManager.getInstance().showLogin();
        }));
    }

    public void loadMyProfileView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/flexfit/desktop/views/my_profile.fxml"));
            Node node = loader.load();
            ProfileController controller = loader.getController();
            controller.setParentDashboard(this);
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadEditProfileView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/flexfit/desktop/views/edit_profile.fxml"));
            Node node = loader.load();
            EditProfileController controller = loader.getController();
            controller.setParentDashboard(this);
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSettingsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/flexfit/desktop/views/settings.fxml"));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlaceholder(String title, String phase, String description) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/flexfit/desktop/views/placeholder.fxml"));
            Node node = loader.load();
            PlaceholderController controller = loader.getController();
            controller.setModuleInfo(title, phase, description);
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveNav(Button activeButton) {
        Button[] buttons = {
                navDashboardBtn, navCoachBtn, navWorkoutsBtn, navDietBtn,
                navChallengesBtn, navLeaderboardBtn, navProgressBtn, navAchievementsBtn,
                navProfileBtn, navSettingsBtn
        };
        for (Button b : buttons) {
            if (b != null) {
                b.getStyleClass().remove("nav-button-active");
            }
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }
}
