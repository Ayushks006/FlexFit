package com.flexfit.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexfit.backend.controller.AuthController;
import com.flexfit.backend.dto.request.LoginRequest;
import com.flexfit.backend.dto.request.RegisterRequest;
import com.flexfit.backend.dto.response.AuthResponse;
import com.flexfit.backend.dto.response.UserDto;
import com.flexfit.backend.exception.DuplicateResourceException;
import com.flexfit.backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest createValidRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Fitness Member");
        req.setUsername("fituser1");
        req.setEmail("fituser1@flexfit.test");
        req.setPassword("SecurePass123!");
        req.setConfirmPassword("SecurePass123!");
        req.setAge(28);
        req.setGender("Male");
        req.setDistrict("Central");
        req.setState("Metro");
        req.setHeightCm(new BigDecimal("175.0"));
        req.setCurrentWeightKg(new BigDecimal("74.0"));
        req.setTargetWeightKg(new BigDecimal("70.0"));
        req.setFitnessGoal("Weight loss");
        req.setExperienceLevel("Intermediate");
        req.setWorkoutLocation("Gym");
        req.setAvailableEquipment(List.of("Dumbbells"));
        req.setPreferredWorkoutDurationMin(45);
        req.setDietPreference("Vegetarian");
        return req;
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return HTTP 201 on valid input")
    void testRegisterEndpointSuccess() throws Exception {
        RegisterRequest req = createValidRegisterRequest();
        UserDto userDto = new UserDto(1L, "Fitness Member", "fituser1", "fituser1@flexfit.test", "ROLE_USER", LocalDateTime.now());
        AuthResponse mockResponse = new AuthResponse("mock-jwt-token", 3600L, userDto, null);

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", is("mock-jwt-token")))
                .andExpect(jsonPath("$.data.user.username", is("fituser1")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return HTTP 409 Conflict on duplicate username")
    void testRegisterDuplicateUsername() throws Exception {
        RegisterRequest req = createValidRegisterRequest();
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("Username 'fituser1' is already taken"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Username 'fituser1' is already taken")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return HTTP 400 Bad Request on invalid email")
    void testRegisterValidationFailure() throws Exception {
        RegisterRequest req = createValidRegisterRequest();
        req.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return HTTP 200 with JWT on valid credentials")
    void testLoginSuccess() throws Exception {
        LoginRequest loginReq = new LoginRequest("fituser1", "SecurePass123!");
        UserDto userDto = new UserDto(1L, "Fitness Member", "fituser1", "fituser1@flexfit.test", "ROLE_USER", LocalDateTime.now());
        AuthResponse mockResponse = new AuthResponse("mock-jwt-token", 3600L, userDto, null);

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", is("mock-jwt-token")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout should return HTTP 200")
    void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
