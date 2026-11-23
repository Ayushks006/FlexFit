package com.flexfit.backend;

import com.flexfit.backend.controller.HealthController;
import com.flexfit.backend.dto.HealthResponse;
import com.flexfit.backend.service.HealthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthService healthService;

    @Test
    @DisplayName("GET /api/v1/ping should return HTTP 200 and PONG message")
    void testPingEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.message", is("PONG")))
                .andExpect(jsonPath("$.app", is("FlexFit")));
    }

    @Test
    @DisplayName("GET /api/v1/health should return system status UP")
    void testHealthEndpoint() throws Exception {
        HealthResponse mockResponse = new HealthResponse(
                "UP",
                "flexfit-backend",
                "1.0.0-SNAPSHOT (v1)",
                Instant.now(),
                "CONNECTED",
                "STANDBY (Phase 2 Integration)",
                Map.of("javaVersion", "21")
        );

        Mockito.when(healthService.getSystemHealth()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("flexfit-backend")))
                .andExpect(jsonPath("$.databaseStatus", is("CONNECTED")));
    }
}
