package com.flexfit.backend.service;

import com.flexfit.backend.dto.request.LoginRequest;
import com.flexfit.backend.dto.request.RegisterRequest;
import com.flexfit.backend.dto.response.AuthResponse;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.dto.response.UserDto;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.exception.DuplicateResourceException;
import com.flexfit.backend.exception.InvalidRequestException;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FitnessProfileRepository fitnessProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       FitnessProfileRepository fitnessProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.fitnessProfileRepository = fitnessProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Password Confirmation Check
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Password and confirmation password do not match");
        }

        // 2. Uniqueness Checks
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        // 3. Create User with BCrypt Hashed Password
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // 4. Create Associated Fitness Profile
        FitnessProfile profile = new FitnessProfile();
        profile.setUser(savedUser);
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

        FitnessProfile savedProfile = fitnessProfileRepository.save(profile);
        savedUser.setFitnessProfile(savedProfile);

        // 5. Generate JWT Token
        String token = tokenProvider.generateToken(savedUser.getUsername(), savedUser.getId(), savedUser.getRole());

        UserDto userDto = mapToUserDto(savedUser);
        FitnessProfileDto profileDto = mapToProfileDto(savedProfile);

        return new AuthResponse(token, tokenProvider.getExpirationMs() / 1000, userDto, profileDto);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim().toLowerCase();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account has been deactivated");
        }

        String token = tokenProvider.generateToken(user.getUsername(), user.getId(), user.getRole());

        UserDto userDto = mapToUserDto(user);
        FitnessProfile profile = fitnessProfileRepository.findByUserId(user.getId()).orElse(null);
        FitnessProfileDto profileDto = profile != null ? mapToProfileDto(profile) : null;

        return new AuthResponse(token, tokenProvider.getExpirationMs() / 1000, userDto, profileDto);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return mapToUserDto(user);
    }

    public static UserDto mapToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public static FitnessProfileDto mapToProfileDto(FitnessProfile profile) {
        FitnessProfileDto dto = new FitnessProfileDto();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setDistrict(profile.getDistrict());
        dto.setState(profile.getState());
        dto.setHeightCm(profile.getHeightCm());
        dto.setCurrentWeightKg(profile.getCurrentWeightKg());
        dto.setTargetWeightKg(profile.getTargetWeightKg());
        dto.setBmi(FitnessProfileDto.calculateBmi(profile.getHeightCm(), profile.getCurrentWeightKg()));
        dto.setFitnessGoal(profile.getFitnessGoal());
        dto.setExperienceLevel(profile.getExperienceLevel());
        dto.setWorkoutLocation(profile.getWorkoutLocation());

        if (profile.getAvailableEquipment() != null && !profile.getAvailableEquipment().isBlank()) {
            dto.setAvailableEquipment(Arrays.asList(profile.getAvailableEquipment().split(",")));
        } else {
            dto.setAvailableEquipment(Collections.emptyList());
        }

        dto.setPreferredWorkoutDurationMin(profile.getPreferredWorkoutDurationMin());
        dto.setDietPreference(profile.getDietPreference());
        dto.setFoodAllergies(profile.getFoodAllergies());
        dto.setDietaryRestrictions(profile.getDietaryRestrictions());
        dto.setInjuriesLimitations(profile.getInjuriesLimitations());
        dto.setProfileCompletionPercentage(100);
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());

        return dto;
    }
}
