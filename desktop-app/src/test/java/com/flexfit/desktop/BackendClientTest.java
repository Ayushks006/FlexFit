package com.flexfit.desktop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flexfit.desktop.model.BackendHealth;
import com.flexfit.desktop.service.BackendClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BackendClientTest {

    @Test
    @DisplayName("BackendClient should initialize with proper base URL and strip trailing slashes")
    void testClientInitialization() {
        BackendClient client = new BackendClient("http://localhost:8080/");
        assertEquals("http://localhost:8080", client.getBaseUrl());
    }

    @Test
    @DisplayName("BackendHealth model should serialize and deserialize JSON correctly")
    void testBackendHealthJsonMapping() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        String json = """
                {
                    "status": "UP",
                    "service": "flexfit-backend",
                    "version": "1.0.0-SNAPSHOT",
                    "timestamp": "2026-11-23T12:00:00Z",
                    "databaseStatus": "CONNECTED",
                    "mlServiceStatus": "STANDBY",
                    "systemDetails": {
                        "javaVersion": "21",
                        "osName": "Windows 11"
                    }
                }
                """;

        BackendHealth health = mapper.readValue(json, BackendHealth.class);

        assertNotNull(health);
        assertEquals("UP", health.getStatus());
        assertEquals("flexfit-backend", health.getService());
        assertEquals("CONNECTED", health.getDatabaseStatus());
        assertEquals("Windows 11", health.getSystemDetails().get("osName"));
    }
}
