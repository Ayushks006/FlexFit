package com.flexfit.backend.controller.web;

import com.flexfit.backend.dto.request.WorkoutLogRequest;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.dto.response.UserDto;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.service.AuthService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebWorkoutController {

    private static final List<String> WORKOUT_CATEGORIES = List.of(
            "Strength", "Cardio", "HIIT & Conditioning", "Full Body", "Mobility", "Core"
    );

    private final AuthService authService;
    private final ProfileService profileService;
    private final WorkoutService workoutService;

    public WebWorkoutController(AuthService authService,
                                ProfileService profileService,
                                WorkoutService workoutService) {
        this.authService = authService;
        this.profileService = profileService;
        this.workoutService = workoutService;
    }

    @GetMapping("/workouts")
    public String workoutsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        populateWorkoutModel(userDetails.getUsername(), model);
        if (!model.containsAttribute("workoutLogRequest")) {
            WorkoutLogRequest defaultReq = new WorkoutLogRequest();
            defaultReq.setDurationMin(30);
            model.addAttribute("workoutLogRequest", defaultReq);
        }
        return "web/workouts";
    }

    @PostMapping("/workouts/log")
    public String logWorkout(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute("workoutLogRequest") WorkoutLogRequest workoutLogRequest,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            populateWorkoutModel(userDetails.getUsername(), model);
            return "web/workouts";
        }

        workoutService.logWorkout(userDetails.getUsername(), workoutLogRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Workout session logged to your history.");
        return "redirect:/workouts";
    }

    @PostMapping("/workouts/{id}/delete")
    public String deleteWorkout(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes) {
        workoutService.deleteWorkout(userDetails.getUsername(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Workout log removed.");
        return "redirect:/workouts";
    }

    private void populateWorkoutModel(String username, Model model) {
        UserDto currentUser = authService.getCurrentUser(username);
        FitnessProfileDto profile = null;
        try {
            profile = profileService.getProfileForUser(username);
        } catch (ResourceNotFoundException ignored) {
            // Optional profile
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("profile", profile);
        model.addAttribute("activePage", "workouts");
        model.addAttribute("pageTitle", "Workouts");
        model.addAttribute("workoutCategories", WORKOUT_CATEGORIES);
        model.addAttribute("recommendations", workoutService.getRecommendationsForUser(username));
        model.addAttribute("workoutSummary", workoutService.getWorkoutSummary(username));
        model.addAttribute("workoutHistory", workoutService.getWorkoutHistory(username));
    }
}
