package com.flexfit.desktop.service;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class ScreenManager {

    private static final ScreenManager INSTANCE = new ScreenManager();

    private Stage primaryStage;
    private Scene mainScene;

    private ScreenManager() {
    }

    public static ScreenManager getInstance() {
        return INSTANCE;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    public void switchScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (mainScene == null) {
                mainScene = new Scene(root, 1120, 800);
                mainScene.setFill(Color.web("#0A0D14"));
                primaryStage.setScene(mainScene);
            } else {
                root.setOpacity(0.0);
                mainScene.setRoot(root);

                FadeTransition ft = new FadeTransition(Duration.millis(300), root);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
            }

            if (title != null && !title.isBlank()) {
                primaryStage.setTitle("FlexFit • " + title);
            }
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load view [" + fxmlPath + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showWelcome() {
        switchScreen("/com/flexfit/desktop/views/welcome.fxml", "Welcome");
    }

    public void showLogin() {
        switchScreen("/com/flexfit/desktop/views/login.fxml", "Member Sign In");
    }

    public void showRegistration() {
        switchScreen("/com/flexfit/desktop/views/registration.fxml", "Create Fitness Profile");
    }

    public void showRegistrationSuccess() {
        switchScreen("/com/flexfit/desktop/views/registration_success.fxml", "Profile Created Successfully");
    }

    public void showDashboard() {
        switchScreen("/com/flexfit/desktop/views/dashboard.fxml", "Dashboard");
    }
}
