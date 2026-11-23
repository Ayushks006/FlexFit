package com.flexfit.backend.service;

import com.flexfit.backend.dto.request.ProfileUpdateRequest;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final FitnessProfileRepository fitnessProfileRepository;

    public ProfileService(UserRepository userRepository, FitnessProfileRepository fitnessProfileRepository) {
        this.userRepository = userRepository;
        this.fitnessProfileRepository = fitnessProfileRepository;
    }

    @Transactional(readOnly = true)
    public FitnessProfileDto getProfileForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        FitnessProfile profile = fitnessProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Fitness profile not found for user: " + username));

        return AuthService.mapToProfileDto(profile);
    }

    @Transactional
    public FitnessProfileDto updateProfileForUser(String username, ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        FitnessProfile profile = fitnessProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Fitness profile not found for user: " + username));

        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setDistrict(request.getDistrict().trim());
        profile.setState(request.getState().trim());
        profile.setHeightCm(request.getHeightCm());
        profile.setCurrentWeightKg(request.getCurrentWeightKg());
        profile.setTargetWeightKg(request.getTargetWeightKg());
        profile.setFitnessGoal(request.getFitnessGoal());
        profile.setExperienceLevel(request.getExperienceLevel());
        profile.setWorkoutLocation(request.getWorkoutLocation());

        if (request.getAvailableEquipment() != null && !request.getAvailableEquipment().isEmpty()) {
            profile.setAvailableEquipment(String.join(",", request.getAvailableEquipment()));
        } else {
            profile.setAvailableEquipment("None");
        }

        profile.setPreferredWorkoutDurationMin(request.getPreferredWorkoutDurationMin());
        profile.setDietPreference(request.getDietPreference());
        profile.setFoodAllergies(request.getFoodAllergies());
        profile.setDietaryRestrictions(request.getDietaryRestrictions());
        profile.setInjuriesLimitations(request.getInjuriesLimitations());

        FitnessProfile updatedProfile = fitnessProfileRepository.save(profile);
        return AuthService.mapToProfileDto(updatedProfile);
    }
}
