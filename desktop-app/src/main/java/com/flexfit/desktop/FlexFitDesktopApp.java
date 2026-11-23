package com.flexfit.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main Application launcher for the FlexFit JavaFX Desktop client.
 */
public class FlexFitDesktopApp extends Application {

    private static final String APP_TITLE = "FlexFit • AI-Powered Fitness Ecosystem";
    private static final int DEFAULT_WIDTH = 1100;
    private static final int DEFAULT_HEIGHT = 780;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/flexfit/desktop/views/welcome.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            scene.setFill(Color.web("#0A0D14"));

            primaryStage.setTitle(APP_TITLE);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(650);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to load FlexFit Welcome view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
