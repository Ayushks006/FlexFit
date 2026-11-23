package com.flexfit.desktop.controller;

import com.flexfit.desktop.model.UserSession;
import com.flexfit.desktop.service.BackendClient;
import com.flexfit.desktop.service.ScreenManager;
import com.flexfit.desktop.service.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SettingsController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label backendUrlLabel;

    private final BackendClient backendClient = new BackendClient();

    @FXML
    public void initialize() {
        UserSession session = SessionManager.getInstance().getSession();
        if (session != null) {
            usernameLabel.setText("@" + session.getUsername());
            emailLabel.setText(session.getEmail());
            roleLabel.setText(session.getRole() != null ? session.getRole() : "ROLE_USER");
        }
        backendUrlLabel.setText(backendClient.getBaseUrl());
    }

    @FXML
    private void handleLogout() {
        backendClient.logoutAsync().whenComplete((res, err) -> Platform.runLater(() -> {
            SessionManager.getInstance().clearSession();
            ScreenManager.getInstance().showLogin();
        }));
    }
}
