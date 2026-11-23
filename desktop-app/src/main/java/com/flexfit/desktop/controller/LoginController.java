package com.flexfit.desktop.controller;

import com.flexfit.desktop.model.UserSession;
import com.flexfit.desktop.service.BackendClient;
import com.flexfit.desktop.service.ScreenManager;
import com.flexfit.desktop.service.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML
    private TextField usernameOrEmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private CheckBox passwordToggleCheckbox;

    @FXML
    private Button signInButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private VBox errorBox;

    @FXML
    private Label errorLabel;

    private final BackendClient backendClient = new BackendClient();

    @FXML
    public void initialize() {
        // Sync password field and visible text field
        passwordVisibleField.managedProperty().bind(passwordToggleCheckbox.selectedProperty());
        passwordVisibleField.visibleProperty().bind(passwordToggleCheckbox.selectedProperty());

        passwordField.managedProperty().bind(passwordToggleCheckbox.selectedProperty().not());
        passwordField.visibleProperty().bind(passwordToggleCheckbox.selectedProperty().not());

        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleLogin() {
        String usernameOrEmail = usernameOrEmailField.getText() != null ? usernameOrEmailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (usernameOrEmail.isBlank()) {
            showError("Please enter your username or email address.");
            return;
        }

        if (password.isBlank()) {
            showError("Please enter your password.");
            return;
        }

        hideError();
        setLoading(true);

        backendClient.loginAsync(usernameOrEmail, password)
                .thenAccept(userSession -> Platform.runLater(() -> {
                    setLoading(false);
                    SessionManager.getInstance().startSession(userSession);
                    ScreenManager.getInstance().showDashboard();
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        String msg = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
                        showError(msg != null ? msg : "Login failed. Please verify credentials and backend connectivity.");
                    });
                    return null;
                });
    }

    @FXML
    private void handleNavigateRegister() {
        ScreenManager.getInstance().showRegistration();
    }

    @FXML
    private void handleNavigateBack() {
        ScreenManager.getInstance().showWelcome();
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
        signInButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
    }
}
