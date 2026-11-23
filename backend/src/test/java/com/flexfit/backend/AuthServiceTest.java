package com.flexfit.backend;

import com.flexfit.backend.dto.request.LoginRequest;
import com.flexfit.backend.dto.request.RegisterRequest;
import com.flexfit.backend.dto.response.AuthResponse;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.exception.DuplicateResourceException;
import com.flexfit.backend.exception.InvalidRequestException;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.security.JwtTokenProvider;
import com.flexfit.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private FitnessProfileRepository fitnessProfileRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        fitnessProfileRepository = mock(FitnessProfileRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        tokenProvider = new JwtTokenProvider("test_secret_key_that_is_at_least_32_bytes_long_for_security", 3600000);
        authService = new AuthService(userRepository, fitnessProfileRepository, passwordEncoder, tokenProvider);
    }

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
        req.setAvailableEquipment(List.of("Dumbbells", "Barbell"));
        req.setPreferredWorkoutDurationMin(45);
        req.setDietPreference("Vegetarian");
        req.setFoodAllergies("None");
        req.setDietaryRestrictions("None");
        req.setInjuriesLimitations("None");
        return req;
    }

    @Test
    @DisplayName("Successful registration encodes password with BCrypt and creates linked profile")
    void testSuccessfulRegistration() {
        RegisterRequest req = createValidRegisterRequest();

        when(userRepository.existsByUsername("fituser1")).thenReturn(false);
        when(userRepository.existsByEmail("fituser1@flexfit.test")).thenReturn(false);

        User savedUser = new User("Fitness Member", "fituser1", "fituser1@flexfit.test", passwordEncoder.encode(req.getPassword()));
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        FitnessProfile savedProfile = new FitnessProfile();
        savedProfile.setId(10L);
        savedProfile.setUser(savedUser);
        savedProfile.setAge(28);
        savedProfile.setGender("Male");
        savedProfile.setDistrict("Central");
        savedProfile.setState("Metro");
        savedProfile.setHeightCm(new BigDecimal("175.0"));
        savedProfile.setCurrentWeightKg(new BigDecimal("74.0"));
        savedProfile.setTargetWeightKg(new BigDecimal("70.0"));
        savedProfile.setFitnessGoal("Weight loss");
        savedProfile.setExperienceLevel("Intermediate");
        savedProfile.setWorkoutLocation("Gym");
        savedProfile.setAvailableEquipment("Dumbbells,Barbell");
        savedProfile.setPreferredWorkoutDurationMin(45);
        savedProfile.setDietPreference("Vegetarian");

        when(fitnessProfileRepository.save(any(FitnessProfile.class))).thenReturn(savedProfile);

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("fituser1", response.getUser().getUsername());
        assertEquals("fituser1@flexfit.test", response.getUser().getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotEquals("SecurePass123!", capturedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("SecurePass123!", capturedUser.getPasswordHash()));
        verify(fitnessProfileRepository).save(any(FitnessProfile.class));
    }

    @Test
    @DisplayName("Registration rejects mismatched password and confirm password")
    void testPasswordMismatchRejection() {
        RegisterRequest req = createValidRegisterRequest();
        req.setConfirmPassword("DifferentPassword123!");

        assertThrows(InvalidRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration rejects duplicate username")
    void testDuplicateUsernameRejection() {
        RegisterRequest req = createValidRegisterRequest();
        when(userRepository.existsByUsername("fituser1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration rejects duplicate email")
    void testDuplicateEmailRejection() {
        RegisterRequest req = createValidRegisterRequest();
        when(userRepository.existsByUsername("fituser1")).thenReturn(false);
        when(userRepository.existsByEmail("fituser1@flexfit.test")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Successful login verifies credentials and generates valid JWT")
    void testSuccessfulLogin() {
        String rawPassword = "UserSecret123!";
        User user = new User("Fitness Member", "fituser1", "fituser1@flexfit.test", passwordEncoder.encode(rawPassword));
        user.setId(2L);

        when(userRepository.findByUsernameOrEmail("fituser1", "fituser1")).thenReturn(Optional.of(user));

        LoginRequest loginReq = new LoginRequest("fituser1", rawPassword);
        AuthResponse response = authService.login(loginReq);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("fituser1", response.getUser().getUsername());
        assertTrue(tokenProvider.validateToken(response.getToken()));
        assertEquals("fituser1", tokenProvider.getUsernameFromToken(response.getToken()));
    }

    @Test
    @DisplayName("Login rejects incorrect password")
    void testIncorrectPasswordRejection() {
        User user = new User("Fitness Member", "fituser1", "fituser1@flexfit.test", passwordEncoder.encode("CorrectPassword123!"));
        user.setId(2L);

        when(userRepository.findByUsernameOrEmail("fituser1", "fituser1")).thenReturn(Optional.of(user));

        LoginRequest loginReq = new LoginRequest("fituser1", "WrongPassword123!");
        assertThrows(BadCredentialsException.class, () -> authService.login(loginReq));
    }
}
