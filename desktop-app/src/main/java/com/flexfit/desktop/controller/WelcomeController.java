package com.flexfit.desktop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.flexfit.desktop.model.BackendHealth;
import com.flexfit.desktop.service.BackendClient;
import com.flexfit.desktop.service.ScreenManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Controller for the FlexFit Welcome Dashboard.
 * Handles UI interactions, card animations, and asynchronous REST communication
 * with the Spring Boot backend.
 */
public class WelcomeController {

    @FXML
    private VBox rootContainer;

    @FXML
    private HBox statusPill;

    @FXML
    private Circle statusDot;

    @FXML
    private Text statusText;

    @FXML
    private Label latencyLabel;

    @FXML
    private Label endpointLabel;

    @FXML
    private Button pingButton;

    @FXML
    private Button healthButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private TextArea diagnosticConsole;

    @FXML
    private VBox backendCard;

    @FXML
    private VBox fitnessCard;

    @FXML
    private VBox leaderboardCard;

    @FXML
    private VBox workoutCard;

    private final BackendClient backendClient;
    private final ObjectMapper prettyPrinter;

    public WelcomeController() {
        this.backendClient = new BackendClient();
        this.prettyPrinter = new ObjectMapper();
        this.prettyPrinter.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @FXML
    public void initialize() {
        endpointLabel.setText(backendClient.getBaseUrl() + "/api/v1/health");

        // Run smooth entrance animations on cards
        runEntranceAnimations();

        // Perform initial non-blocking background check
        handlePingBackend();
    }

    @FXML
    private void handleNavigateLogin() {
        ScreenManager.getInstance().showLogin();
    }

    @FXML
    private void handleNavigateRegister() {
        ScreenManager.getInstance().showRegistration();
    }

    private void runEntranceAnimations() {
        Node[] cards = {backendCard, fitnessCard, leaderboardCard, workoutCard};
        int delayMs = 50;
        for (Node card : cards) {
            if (card != null) {
                card.setOpacity(0.0);
                card.setTranslateY(20.0);

                FadeTransition ft = new FadeTransition(Duration.millis(500), card);
                ft.setToValue(1.0);

                TranslateTransition tt = new TranslateTransition(Duration.millis(500), card);
                tt.setToY(0.0);

                ParallelTransition pt = new ParallelTransition(ft, tt);
                pt.setDelay(Duration.millis(delayMs));
                pt.play();

                delayMs += 80;
            }
        }
    }

    @FXML
    private void handlePingBackend() {
        setCheckingState("PINGING BACKEND...");

        backendClient.pingAsync()
                .thenAccept(latencyMs -> Platform.runLater(() -> {
                    setOnlineState();
                    latencyLabel.setText("Latency: " + latencyMs + " ms");
                    diagnosticConsole.setText(String.format(
                            "[%s] SUCCESS: Ping acknowledged by %s in %d ms.\nEndpoint: %s/api/v1/ping",
                            java.time.LocalTime.now().withNano(0),
                            backendClient.getBaseUrl(),
                            latencyMs,
                            backendClient.getBaseUrl()
                    ));
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setOfflineState();
                        latencyLabel.setText("Latency: Timeout / Down");
                        diagnosticConsole.setText(String.format(
                                "[%s] BACKEND OFFLINE:\nTarget: %s\nDetails: %s\n\nTo start backend:\n  cd backend\n  ../mvnw spring-boot:run",
                                java.time.LocalTime.now().withNano(0),
                                backendClient.getBaseUrl(),
                                throwable.getMessage()
                        ));
                    });
                    return null;
                });
    }

    @FXML
    private void handleCheckHealth() {
        setCheckingState("FETCHING HEALTH DATA...");

        backendClient.checkHealthAsync()
                .thenAccept(health -> Platform.runLater(() -> {
                    setOnlineState();
                    try {
                        String formattedJson = prettyPrinter.writeValueAsString(health);
                        diagnosticConsole.setText(String.format(
                                "[%s] HEALTH DIAGNOSTICS REPORT:\n%s",
                                java.time.LocalTime.now().withNano(0),
                                formattedJson
                        ));
                    } catch (Exception e) {
                        diagnosticConsole.setText(String.format(
                                "Status: %s | Service: %s | DB: %s",
                                health.getStatus(), health.getService(), health.getDatabaseStatus()
                        ));
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setOfflineState();
                        diagnosticConsole.setText(String.format(
                                "[%s] ERROR FETCHING DIAGNOSTICS:\n%s",
                                java.time.LocalTime.now().withNano(0),
                                throwable.getMessage()
                        ));
                    });
                    return null;
                });
    }

    private void setCheckingState(String message) {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        if (pingButton != null) pingButton.setDisable(true);
        if (healthButton != null) healthButton.setDisable(true);

        statusPill.getStyleClass().removeAll("status-pill-online", "status-pill-offline", "status-pill-checking");
        statusPill.getStyleClass().add("status-pill-checking");
        statusDot.setFill(Color.web("#FFAB00"));
        statusText.getStyleClass().removeAll("status-text-online", "status-text-offline", "status-text-checking");
        statusText.getStyleClass().add("status-text-checking");
        statusText.setText(message);
    }

    private void setOnlineState() {
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        if (pingButton != null) pingButton.setDisable(false);
        if (healthButton != null) healthButton.setDisable(false);

        statusPill.getStyleClass().removeAll("status-pill-online", "status-pill-offline", "status-pill-checking");
        statusPill.getStyleClass().add("status-pill-online");
        statusDot.setFill(Color.web("#00E676"));
        statusText.getStyleClass().removeAll("status-text-online", "status-text-offline", "status-text-checking");
        statusText.getStyleClass().add("status-text-online");
        statusText.setText("BACKEND ONLINE");
    }

    private void setOfflineState() {
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        if (pingButton != null) pingButton.setDisable(false);
        if (healthButton != null) healthButton.setDisable(false);

        statusPill.getStyleClass().removeAll("status-pill-online", "status-pill-offline", "status-pill-checking");
        statusPill.getStyleClass().add("status-pill-offline");
        statusDot.setFill(Color.web("#FF5252"));
        statusText.getStyleClass().removeAll("status-text-online", "status-text-offline", "status-text-checking");
        statusText.getStyleClass().add("status-text-offline");
        statusText.setText("BACKEND OFFLINE");
    }
}
