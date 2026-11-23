package com.flexfit.backend.controller;

import com.flexfit.backend.dto.HealthResponse;
import com.flexfit.backend.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for FlexFit system health and diagnostic checks.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * Comprehensive health check reporting backend, database, and system statuses.
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse health = healthService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * Fast ping endpoint for low-overhead connectivity verification.
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "PONG",
                "app", "FlexFit"
        ));
    }
}
