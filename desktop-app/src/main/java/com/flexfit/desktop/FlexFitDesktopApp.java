package com.flexfit.desktop;

import com.flexfit.desktop.service.ScreenManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main Application launcher for the FlexFit JavaFX Desktop client.
 */
public class FlexFitDesktopApp extends Application {

    private static final String APP_TITLE = "FlexFit • AI-Powered Fitness Ecosystem";
    private static final int DEFAULT_WIDTH = 1120;
    private static final int DEFAULT_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(680);
        primaryStage.setWidth(DEFAULT_WIDTH);
        primaryStage.setHeight(DEFAULT_HEIGHT);

        ScreenManager.getInstance().init(primaryStage);
        ScreenManager.getInstance().showWelcome();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
