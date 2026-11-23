package com.flexfit.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flexfit.desktop.model.BackendHealth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous REST Client for communicating with the FlexFit Spring Boot Backend.
 * Utilizes Java 21 native HttpClient with CompletableFuture for smooth UI integration.
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
                .connectTimeout(Duration.ofSeconds(3))
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

    /**
     * Asynchronously fetches system health from the backend /api/v1/health endpoint.
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
     * Asynchronously measures ping latency (in milliseconds) to the backend /api/v1/ping endpoint.
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
}
