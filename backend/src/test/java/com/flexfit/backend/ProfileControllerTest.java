package com.flexfit.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexfit.backend.controller.ProfileController;
import com.flexfit.backend.dto.request.ProfileUpdateRequest;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @Test
    @WithMockUser(username = "fituser1")
    @DisplayName("GET /api/v1/users/me/profile should return HTTP 200 with user profile")
    void testGetProfileSuccess() throws Exception {
        FitnessProfileDto profileDto = new FitnessProfileDto();
        profileDto.setId(10L);
        profileDto.setUserId(1L);
        profileDto.setAge(28);
        profileDto.setGender("Male");
        profileDto.setFitnessGoal("Weight loss");
        profileDto.setBmi(new BigDecimal("24.2"));

        when(profileService.getProfileForUser("fituser1")).thenReturn(profileDto);

        mockMvc.perform(get("/api/v1/users/me/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fitnessGoal", is("Weight loss")))
                .andExpect(jsonPath("$.data.bmi", is(24.2)));
    }

    @Test
    @WithMockUser(username = "fituser1")
    @DisplayName("PUT /api/v1/users/me/profile should return HTTP 200 with updated profile")
    void testUpdateProfileSuccess() throws Exception {
        ProfileUpdateRequest updateReq = new ProfileUpdateRequest();
        updateReq.setAge(29);
        updateReq.setGender("Male");
        updateReq.setDistrict("Downtown");
        updateReq.setState("California");
        updateReq.setHeightCm(new BigDecimal("178.0"));
        updateReq.setCurrentWeightKg(new BigDecimal("75.0"));
        updateReq.setTargetWeightKg(new BigDecimal("70.0"));
        updateReq.setFitnessGoal("Muscle gain");
        updateReq.setExperienceLevel("Intermediate");
        updateReq.setWorkoutLocation("Gym");
        updateReq.setAvailableEquipment(List.of("Dumbbells"));
        updateReq.setPreferredWorkoutDurationMin(45);
        updateReq.setDietPreference("Vegetarian");

        FitnessProfileDto updatedDto = new FitnessProfileDto();
        updatedDto.setAge(29);
        updatedDto.setFitnessGoal("Muscle gain");
        updatedDto.setCurrentWeightKg(new BigDecimal("75.0"));

        when(profileService.updateProfileForUser(eq("fituser1"), any(ProfileUpdateRequest.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fitnessGoal", is("Muscle gain")));
    }
}
