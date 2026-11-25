package com.flexfit.backend.controller.web;

import com.flexfit.backend.dto.request.ProfileUpdateRequest;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.dto.response.UserDto;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.service.AuthService;
import com.flexfit.backend.service.ChallengeService;
import com.flexfit.backend.service.NutritionService;
import com.flexfit.backend.service.ProfileService;
import com.flexfit.backend.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Controller
public class WebDashboardController {

    private final AuthService authService;
    private final ProfileService profileService;
    private final WorkoutService workoutService;
    private final NutritionService nutritionService;
    private final ChallengeService challengeService;

    public WebDashboardController(AuthService authService,
                                   ProfileService profileService,
                                   WorkoutService workoutService,
                                   NutritionService nutritionService,
                                   ChallengeService challengeService) {
        this.authService = authService;
        this.profileService = profileService;
        this.workoutService = workoutService;
        this.nutritionService = nutritionService;
        this.challengeService = challengeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        FitnessProfileDto profile = populateUserContext(userDetails, model, "dashboard", "Dashboard");
        model.addAttribute("workoutSummary", workoutService.getWorkoutSummary(userDetails.getUsername()));
        model.addAttribute("nutritionSummary", nutritionService.getDailySummary(userDetails.getUsername(), LocalDate.now()));
        model.addAttribute("challengesOverview", challengeService.getChallengesOverview(userDetails.getUsername()));

        if (profile != null) {
            int completion = profile.getProfileCompletionPercentage() != null
                    ? profile.getProfileCompletionPercentage()
                    : 0;
            model.addAttribute("completionPercentage", completion);
            model.addAttribute("completionSubtitle",
                    completion >= 100
                            ? "Your fitness profile is complete!"
                            : completion + "% Profile Completed");
            model.addAttribute("bmiCategory", resolveBmiCategory(profile.getBmi()));
            model.addAttribute("weightDelta", resolveWeightDelta(
                    profile.getCurrentWeightKg(), profile.getTargetWeightKg()));
        } else {
            model.addAttribute("completionPercentage", 0);
            model.addAttribute("completionSubtitle", "0% Profile Completed");
            model.addAttribute("bmiCategory", "--");
            model.addAttribute("weightDelta", null);
        }

        return "web/dashboard";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        populateUserContext(userDetails, model, "profile", "Fitness Profile");
        return "web/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        FitnessProfileDto profile = populateUserContext(userDetails, model, "profile", "Edit Fitness Profile");
        if (!model.containsAttribute("profileUpdateRequest")) {
            model.addAttribute("profileUpdateRequest", mapToUpdateRequest(profile));
        }
        populateProfileOptions(model);
        return "web/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String handleEditProfile(@AuthenticationPrincipal UserDetails userDetails,
                                    @Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            populateUserContext(userDetails, model, "profile", "Edit Fitness Profile");
            populateProfileOptions(model);
            return "web/edit-profile";
        }

        profileService.updateProfileForUser(userDetails.getUsername(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Your fitness profile has been updated.");
        return "redirect:/profile";
    }

    @GetMapping("/settings")
    public String settings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        populateUserContext(userDetails, model, "settings", "Settings");
        return "web/settings";
    }

    private FitnessProfileDto populateUserContext(UserDetails userDetails,
                                                  Model model,
                                                  String activePage,
                                                  String pageTitle) {
        UserDto currentUser = authService.getCurrentUser(userDetails.getUsername());
        FitnessProfileDto profile = null;
        try {
            profile = profileService.getProfileForUser(userDetails.getUsername());
        } catch (ResourceNotFoundException ignored) {
            // Leave profile null so views can render empty states
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("profile", profile);
        model.addAttribute("activePage", activePage);
        model.addAttribute("pageTitle", pageTitle);
        return profile;
    }

    private ProfileUpdateRequest mapToUpdateRequest(FitnessProfileDto profile) {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        if (profile == null) {
            return req;
        }
        req.setAge(profile.getAge());
        req.setGender(profile.getGender());
        req.setDistrict(profile.getDistrict());
        req.setState(profile.getState());
        req.setHeightCm(profile.getHeightCm());
        req.setCurrentWeightKg(profile.getCurrentWeightKg());
        req.setTargetWeightKg(profile.getTargetWeightKg());
        req.setFitnessGoal(profile.getFitnessGoal());
        req.setExperienceLevel(profile.getExperienceLevel());
        req.setWorkoutLocation(profile.getWorkoutLocation());
        req.setAvailableEquipment(profile.getAvailableEquipment());
        req.setPreferredWorkoutDurationMin(profile.getPreferredWorkoutDurationMin());
        req.setDietPreference(profile.getDietPreference());
        req.setFoodAllergies(profile.getFoodAllergies());
        req.setDietaryRestrictions(profile.getDietaryRestrictions());
        req.setInjuriesLimitations(profile.getInjuriesLimitations());
        return req;
    }

    private void populateProfileOptions(Model model) {
        model.addAttribute("genders", WebFormOptions.GENDERS);
        model.addAttribute("fitnessGoals", WebFormOptions.FITNESS_GOALS);
        model.addAttribute("experienceLevels", WebFormOptions.EXPERIENCE_LEVELS);
        model.addAttribute("workoutLocations", WebFormOptions.WORKOUT_LOCATIONS);
        model.addAttribute("equipmentOptions", WebFormOptions.EQUIPMENT_OPTIONS);
        model.addAttribute("workoutDurations", WebFormOptions.WORKOUT_DURATIONS);
        model.addAttribute("dietPreferences", WebFormOptions.DIET_PREFERENCES);
    }

    private String resolveBmiCategory(BigDecimal bmi) {
        if (bmi == null || bmi.compareTo(BigDecimal.ZERO) <= 0) {
            return "--";
        }
        double value = bmi.doubleValue();
        if (value < 18.5) {
            return "Underweight";
        }
        if (value < 25.0) {
            return "Normal";
        }
        if (value < 30.0) {
            return "Overweight";
        }
        return "Obese";
    }

    private BigDecimal resolveWeightDelta(BigDecimal currentWeight, BigDecimal targetWeight) {
        if (currentWeight == null || targetWeight == null) {
            return null;
        }
        return targetWeight.subtract(currentWeight).setScale(1, RoundingMode.HALF_UP);
    }
}
