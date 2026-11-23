package com.flexfit.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flexfit.desktop.model.BackendHealth;
import com.flexfit.desktop.model.FitnessProfileModel;
import com.flexfit.desktop.model.RegistrationData;
import com.flexfit.desktop.model.UserSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous REST Client for communicating with the FlexFit Spring Boot Backend.
 * Handles Authentication, Fitness Profile operations, and Health diagnostics.
 */
public class BackendClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendClient() {
        this(resolveBaseUrl());
    }

    public BackendClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(4))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private static String resolveBaseUrl() {
        String envUrl = System.getenv("BACKEND_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl.trim();
        }
        return "http://localhost:8080";
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    // ==================== AUTHENTICATION APIS ====================

    /**
     * Authenticates user credentials with POST /api/v1/auth/login.
     */
    public CompletableFuture<UserSession> loginAsync(String usernameOrEmail, String password) {
        try {
            Map<String, String> payload = Map.of(
                    "usernameOrEmail", usernameOrEmail,
                    "password", password
            );
            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/auth/login"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::handleAuthResponse);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Registers a new user with all 6 steps using POST /api/v1/auth/register.
     */
    public CompletableFuture<UserSession> registerAsync(RegistrationData data) {
        try {
            String jsonBody = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/auth/register"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::handleAuthResponse);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Dispatches logout acknowledgement via POST /api/v1/auth/logout.
     */
    public CompletableFuture<Void> logoutAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/auth/logout"))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(4))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(res -> {});
    }

    // ==================== PROFILE APIS ====================

    /**
     * Fetches current authenticated user's profile with GET /api/v1/users/me/profile.
     */
    public CompletableFuture<FitnessProfileModel> getProfileAsync(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/users/me/profile"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            JsonNode root = objectMapper.readTree(response.body());
                            JsonNode dataNode = root.get("data");
                            return objectMapper.treeToValue(dataNode, FitnessProfileModel.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse fitness profile: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException(extractErrorMessage(response));
                    }
                });
    }

    /**
     * Updates profile information with PUT /api/v1/users/me/profile.
     */
    public CompletableFuture<FitnessProfileModel> updateProfileAsync(String token, FitnessProfileModel profile) {
        try {
            String jsonBody = objectMapper.writeValueAsString(profile);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/users/me/profile"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            try {
                                JsonNode root = objectMapper.readTree(response.body());
                                JsonNode dataNode = root.get("data");
                                return objectMapper.treeToValue(dataNode, FitnessProfileModel.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse updated profile: " + e.getMessage(), e);
                            }
                        } else {
                            throw new RuntimeException(extractErrorMessage(response));
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // ==================== HEALTH & DIAGNOSTIC APIS ====================

    /**
     * Asynchronously fetches system health from backend /api/v1/health endpoint.
     */
    public CompletableFuture<BackendHealth> checkHealthAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/health"))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            return objectMapper.readValue(response.body(), BackendHealth.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse health response: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("Backend returned HTTP " + response.statusCode());
                    }
                });
    }

    /**
     * Asynchronously measures ping latency (ms) to /api/v1/ping endpoint.
     */
    public CompletableFuture<Long> pingAsync() {
        long startTime = System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/ping"))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return System.currentTimeMillis() - startTime;
                    } else {
                        throw new RuntimeException("Ping failed with HTTP " + response.statusCode());
                    }
                });
    }

    // ==================== HELPER METHODS ====================

    private UserSession handleAuthResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode dataNode = root.get("data");
                String token = dataNode.get("token").asText();

                JsonNode userNode = dataNode.get("user");
                Long userId = userNode.get("id").asLong();
                String fullName = userNode.get("fullName").asText();
                String username = userNode.get("username").asText();
                String email = userNode.get("email").asText();
                String role = userNode.get("role").asText();

                FitnessProfileModel profile = null;
                if (dataNode.has("profile") && !dataNode.get("profile").isNull()) {
                    profile = objectMapper.treeToValue(dataNode.get("profile"), FitnessProfileModel.class);
                }

                return new UserSession(userId, fullName, username, email, role, token, profile);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse authentication response: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException(extractErrorMessage(response));
        }
    }

    private String extractErrorMessage(HttpResponse<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.body());
            if (root.has("message")) {
                return root.get("message").asText();
            }
            if (root.has("error")) {
                return root.get("error").asText();
            }
        } catch (Exception ignored) {
        }
        return "Server request failed with HTTP " + response.statusCode();
    }
}
