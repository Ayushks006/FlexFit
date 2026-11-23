package com.flexfit.backend;

import com.flexfit.backend.dto.request.ProfileUpdateRequest;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private UserRepository userRepository;
    private FitnessProfileRepository fitnessProfileRepository;
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        fitnessProfileRepository = mock(FitnessProfileRepository.class);
        profileService = new ProfileService(userRepository, fitnessProfileRepository);
    }

    @Test
    @DisplayName("getProfileForUser returns mapped profile with calculated BMI")
    void testGetProfileSuccess() {
        User user = new User("Fitness Member", "fituser1", "fituser1@flexfit.test", "hash");
        user.setId(5L);

        FitnessProfile profile = new FitnessProfile();
        profile.setId(12L);
        profile.setUser(user);
        profile.setAge(29);
        profile.setGender("Female");
        profile.setDistrict("North");
        profile.setState("Metro");
        profile.setHeightCm(new BigDecimal("165.0"));
        profile.setCurrentWeightKg(new BigDecimal("60.0"));
        profile.setTargetWeightKg(new BigDecimal("58.0"));
        profile.setFitnessGoal("General fitness");
        profile.setExperienceLevel("Beginner");
        profile.setWorkoutLocation("Home");
        profile.setAvailableEquipment("Resistance Bands");
        profile.setPreferredWorkoutDurationMin(30);
        profile.setDietPreference("Vegetarian");

        when(userRepository.findByUsername("fituser1")).thenReturn(Optional.of(user));
        when(fitnessProfileRepository.findByUserId(5L)).thenReturn(Optional.of(profile));

        FitnessProfileDto dto = profileService.getProfileForUser("fituser1");

        assertNotNull(dto);
        assertEquals(5L, dto.getUserId());
        assertEquals("General fitness", dto.getFitnessGoal());
        assertEquals(new BigDecimal("22.0"), dto.getBmi());
        assertEquals(100, dto.getProfileCompletionPercentage());
    }

    @Test
    @DisplayName("updateProfileForUser persists updated metrics and recalculates BMI")
    void testUpdateProfileSuccess() {
        User user = new User("Fitness Member", "fituser1", "fituser1@flexfit.test", "hash");
        user.setId(5L);

        FitnessProfile existingProfile = new FitnessProfile();
        existingProfile.setId(12L);
        existingProfile.setUser(user);
        existingProfile.setAge(29);
        existingProfile.setGender("Female");
        existingProfile.setDistrict("North");
        existingProfile.setState("Metro");
        existingProfile.setHeightCm(new BigDecimal("165.0"));
        existingProfile.setCurrentWeightKg(new BigDecimal("65.0"));
        existingProfile.setTargetWeightKg(new BigDecimal("58.0"));
        existingProfile.setFitnessGoal("Weight loss");
        existingProfile.setExperienceLevel("Beginner");
        existingProfile.setWorkoutLocation("Home");
        existingProfile.setAvailableEquipment("None");
        existingProfile.setPreferredWorkoutDurationMin(30);
        existingProfile.setDietPreference("Vegetarian");

        when(userRepository.findByUsername("fituser1")).thenReturn(Optional.of(user));
        when(fitnessProfileRepository.findByUserId(5L)).thenReturn(Optional.of(existingProfile));
        when(fitnessProfileRepository.save(any(FitnessProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileUpdateRequest updateReq = new ProfileUpdateRequest();
        updateReq.setAge(30);
        updateReq.setGender("Female");
        updateReq.setDistrict("South");
        updateReq.setState("Metro");
        updateReq.setHeightCm(new BigDecimal("165.0"));
        updateReq.setCurrentWeightKg(new BigDecimal("62.0")); // Updated weight
        updateReq.setTargetWeightKg(new BigDecimal("58.0"));
        updateReq.setFitnessGoal("Muscle gain");
        updateReq.setExperienceLevel("Intermediate");
        updateReq.setWorkoutLocation("Gym");
        updateReq.setAvailableEquipment(List.of("Dumbbells", "Barbell"));
        updateReq.setPreferredWorkoutDurationMin(45);
        updateReq.setDietPreference("Non-vegetarian");

        FitnessProfileDto updatedDto = profileService.updateProfileForUser("fituser1", updateReq);

        assertNotNull(updatedDto);
        assertEquals(Integer.valueOf(30), updatedDto.getAge());
        assertEquals("South", updatedDto.getDistrict());
        assertEquals(new BigDecimal("62.0"), updatedDto.getCurrentWeightKg());
        assertEquals("Muscle gain", updatedDto.getFitnessGoal());
        assertEquals(new BigDecimal("22.8"), updatedDto.getBmi());
        verify(fitnessProfileRepository).save(existingProfile);
    }

    @Test
    @DisplayName("getProfileForUser throws ResourceNotFoundException when user does not exist")
    void testGetProfileUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfileForUser("unknown"));
    }
}
