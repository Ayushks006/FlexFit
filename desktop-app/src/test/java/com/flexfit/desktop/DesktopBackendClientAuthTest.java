package com.flexfit.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flexfit.desktop.model.FitnessProfileModel;
import com.flexfit.desktop.model.RegistrationData;
import com.flexfit.desktop.model.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DesktopBackendClientAuthTest {

    @Test
    @DisplayName("RegistrationData serializes into valid JSON matching backend contract")
    void testRegistrationDataSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        RegistrationData data = new RegistrationData();
        data.setFullName("Jordan Smith");
        data.setUsername("jsmith");
        data.setEmail("jordan@flexfit.test");
        data.setPassword("Password123!");
        data.setConfirmPassword("Password123!");
        data.setAge(27);
        data.setGender("Male");
        data.setDistrict("Downtown");
        data.setState("California");
        data.setHeightCm(new BigDecimal("182.0"));
        data.setCurrentWeightKg(new BigDecimal("80.0"));
        data.setTargetWeightKg(new BigDecimal("75.0"));
        data.setFitnessGoal("Weight loss");
        data.setExperienceLevel("Intermediate");
        data.setWorkoutLocation("Gym");
        data.setAvailableEquipment(List.of("Dumbbells", "Barbell"));
        data.setPreferredWorkoutDurationMin(45);
        data.setDietPreference("Vegetarian");

        String json = mapper.writeValueAsString(data);
        assertNotNull(json);

        JsonNode tree = mapper.readTree(json);
        assertEquals("Jordan Smith", tree.get("fullName").asText());
        assertEquals("jsmith", tree.get("username").asText());
        assertEquals(182.0, tree.get("heightCm").asDouble());
        assertEquals("Weight loss", tree.get("fitnessGoal").asText());
        assertTrue(tree.get("availableEquipment").isArray());
    }

    @Test
    @DisplayName("Backend ApiResponse JSON parses into client UserSession and FitnessProfileModel")
    void testAuthResponseParsing() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        String json = """
                {
                    "success": true,
                    "message": "Authentication successful",
                    "data": {
                        "token": "header.payload.signature",
                        "tokenType": "Bearer",
                        "expiresIn": 86400,
                        "user": {
                            "id": 42,
                            "fullName": "Jordan Smith",
                            "username": "jsmith",
                            "email": "jordan@flexfit.test",
                            "role": "ROLE_USER"
                        },
                        "profile": {
                            "id": 10,
                            "userId": 42,
                            "age": 27,
                            "gender": "Male",
                            "district": "Downtown",
                            "state": "California",
                            "heightCm": 182.0,
                            "currentWeightKg": 80.0,
                            "targetWeightKg": 75.0,
                            "bmi": 24.2,
                            "fitnessGoal": "Weight loss",
                            "experienceLevel": "Intermediate",
                            "workoutLocation": "Gym",
                            "availableEquipment": ["Dumbbells", "Barbell"],
                            "preferredWorkoutDurationMin": 45,
                            "dietPreference": "Vegetarian",
                            "profileCompletionPercentage": 100
                        }
                    }
                }
                """;

        JsonNode root = mapper.readTree(json);
        assertTrue(root.get("success").asBoolean());

        JsonNode dataNode = root.get("data");
        String token = dataNode.get("token").asText();
        assertEquals("header.payload.signature", token);

        JsonNode userNode = dataNode.get("user");
        assertEquals("jsmith", userNode.get("username").asText());
        assertEquals(42L, userNode.get("id").asLong());

        FitnessProfileModel profile = mapper.treeToValue(dataNode.get("profile"), FitnessProfileModel.class);
        assertNotNull(profile);
        assertEquals("Weight loss", profile.getFitnessGoal());
        assertEquals(new BigDecimal("24.2"), profile.getBmi());
        assertEquals(2, profile.getAvailableEquipment().size());

        UserSession session = new UserSession(
                userNode.get("id").asLong(),
                userNode.get("fullName").asText(),
                userNode.get("username").asText(),
                userNode.get("email").asText(),
                userNode.get("role").asText(),
                token,
                profile
        );

        assertTrue(session.isAuthenticated());
        assertEquals(42L, session.getId());
    }
}
