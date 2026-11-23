package com.flexfit.backend.controller;

import com.flexfit.backend.dto.request.ProfileUpdateRequest;
import com.flexfit.backend.dto.response.ApiResponse;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FitnessProfileDto>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        FitnessProfileDto profileDto = profileService.getProfileForUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Fitness profile retrieved successfully", profileDto));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<FitnessProfileDto>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        FitnessProfileDto updatedDto = profileService.updateProfileForUser(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Fitness profile updated successfully", updatedDto));
    }
}
