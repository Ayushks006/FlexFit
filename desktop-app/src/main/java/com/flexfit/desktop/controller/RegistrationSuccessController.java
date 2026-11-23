package com.flexfit.desktop.controller;

import com.flexfit.desktop.model.UserSession;
import com.flexfit.desktop.service.ScreenManager;
import com.flexfit.desktop.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class RegistrationSuccessController {

    @FXML
    private Label welcomeNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label goalLabel;

    @FXML
    public void initialize() {
        UserSession session = SessionManager.getInstance().getSession();
        if (session != null) {
            welcomeNameLabel.setText(session.getFullName() != null ? session.getFullName() : "Athlete");
            usernameLabel.setText("@" + session.getUsername());
            if (session.getProfile() != null && session.getProfile().getFitnessGoal() != null) {
                goalLabel.setText("Goal: " + session.getProfile().getFitnessGoal());
            } else {
                goalLabel.setText("Goal: Active Fitness");
            }
        }
    }

    @FXML
    private void handleEnterDashboard() {
        ScreenManager.getInstance().showDashboard();
    }

    @FXML
    private void handleGoToLogin() {
        SessionManager.getInstance().clearSession();
        ScreenManager.getInstance().showLogin();
    }
}
