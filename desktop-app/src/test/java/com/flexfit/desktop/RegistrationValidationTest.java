package com.flexfit.desktop;

import com.flexfit.desktop.model.RegistrationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidationTest {

    private RegistrationData data;

    @BeforeEach
    void setUp() {
        data = new RegistrationData();
        data.setFullName("Jordan Smith");
        data.setUsername("jsmith_fit");
        data.setEmail("jordan@flexfit.test");
        data.setPassword("SecurePass123!");
        data.setConfirmPassword("SecurePass123!");
        data.setAge(26);
        data.setGender("Male");
        data.setDistrict("Downtown");
        data.setState("California");
        data.setHeightCm(new BigDecimal("178.0"));
        data.setCurrentWeightKg(new BigDecimal("75.0"));
        data.setTargetWeightKg(new BigDecimal("70.0"));
        data.setFitnessGoal("Muscle gain");
        data.setExperienceLevel("Intermediate");
        data.setWorkoutLocation("Gym");
        data.setPreferredWorkoutDurationMin(45);
        data.setDietPreference("Vegetarian");
    }

    @Test
    @DisplayName("Valid Step 1 registration data yields no validation errors")
    void testStep1ValidationSuccess() {
        List<String> errors = data.validateStep1();
        assertTrue(errors.isEmpty(), "Expected no validation errors for valid step 1 data");
    }

    @Test
    @DisplayName("Step 1 detects invalid email format and mismatched password")
    void testStep1ValidationFailures() {
        data.setEmail("invalid-email-string");
        data.setConfirmPassword("MismatchPassword123!");

        List<String> errors = data.validateStep1();
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("valid email")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("do not match")));
    }

    @Test
    @DisplayName("Step 1 detects short password and invalid username characters")
    void testStep1UsernameAndPasswordValidation() {
        data.setUsername("bad user name with spaces!");
        data.setPassword("123");
        data.setConfirmPassword("123");

        List<String> errors = data.validateStep1();
        assertTrue(errors.stream().anyMatch(e -> e.contains("alphanumeric")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("at least 6 characters")));
    }

    @Test
    @DisplayName("Step 2 detects out-of-range age and empty district/state")
    void testStep2Validation() {
        data.setAge(8); // Below 12
        data.setDistrict("");
        data.setState("   ");

        List<String> errors = data.validateStep2();
        assertEquals(3, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("between 12 and 100")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("District")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("State")));
    }

    @Test
    @DisplayName("Step 3 validates realistic height and weight ranges")
    void testStep3BiometricValidation() {
        data.setHeightCm(new BigDecimal("35.0")); // Too short
        data.setCurrentWeightKg(new BigDecimal("15.0")); // Too light
        data.setTargetWeightKg(new BigDecimal("550.0")); // Exceeds upper limit

        List<String> errors = data.validateStep3();
        assertEquals(3, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Height must be between 50 cm and 260 cm")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Current weight must be between 25 kg and 300 kg")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Target weight must be between 25 kg and 300 kg")));
    }

    @Test
    @DisplayName("Step 5 validates workout duration bounds")
    void testStep5DurationValidation() {
        data.setPreferredWorkoutDurationMin(5); // Too short

        List<String> errors = data.validateStep5();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("between 10 and 180 minutes"));
    }
}
