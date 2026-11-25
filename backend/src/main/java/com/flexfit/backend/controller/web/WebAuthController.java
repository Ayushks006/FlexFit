package com.flexfit.backend.controller.web;

import com.flexfit.backend.dto.request.LoginRequest;
import com.flexfit.backend.dto.request.RegisterRequest;
import com.flexfit.backend.dto.response.AuthResponse;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.dto.response.UserDto;
import com.flexfit.backend.exception.DuplicateResourceException;
import com.flexfit.backend.exception.InvalidRequestException;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.service.AuthService;
import com.flexfit.backend.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class WebAuthController {

    private static final Map<String, Integer> FIELD_STEP_MAP = Map.ofEntries(
            Map.entry("fullName", 1),
            Map.entry("username", 1),
            Map.entry("email", 1),
            Map.entry("password", 1),
            Map.entry("confirmPassword", 1),
            Map.entry("age", 2),
            Map.entry("gender", 2),
            Map.entry("district", 2),
            Map.entry("state", 2),
            Map.entry("heightCm", 3),
            Map.entry("currentWeightKg", 3),
            Map.entry("targetWeightKg", 3),
            Map.entry("fitnessGoal", 4),
            Map.entry("experienceLevel", 4),
            Map.entry("workoutLocation", 5),
            Map.entry("availableEquipment", 5),
            Map.entry("preferredWorkoutDurationMin", 5),
            Map.entry("dietPreference", 5),
            Map.entry("foodAllergies", 6),
            Map.entry("dietaryRestrictions", 6),
            Map.entry("injuriesLimitations", 6)
    );

    private final AuthService authService;
    private final ProfileService profileService;

    @Value("${flexfit.cookie.name:FLEXFIT_TOKEN}")
    private String cookieName;

    @Value("${flexfit.cookie.secure:false}")
    private boolean cookieSecure;

    public WebAuthController(AuthService authService, ProfileService profileService) {
        this.authService = authService;
        this.profileService = profileService;
    }

    @GetMapping("/")
    public String landing() {
        return "web/landing";
    }

    @GetMapping("/login")
    public String loginPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "web/login";
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
                              BindingResult bindingResult,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "web/login";
        }

        try {
            AuthResponse authResponse = authService.login(loginRequest);
            addAuthCookie(response, request, authResponse.getToken(), authResponse.getExpiresIn());
            return "redirect:/dashboard";
        } catch (BadCredentialsException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "web/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("registerRequest")) {
            RegisterRequest defaults = new RegisterRequest();
            defaults.setAge(25);
            defaults.setGender("Male");
            defaults.setFitnessGoal("Weight loss");
            defaults.setExperienceLevel("Beginner");
            defaults.setWorkoutLocation("Gym");
            defaults.setPreferredWorkoutDurationMin(45);
            defaults.setDietPreference("Non-vegetarian");
            model.addAttribute("registerRequest", defaults);
        }
        model.addAttribute("initialStep", 1);
        populateRegisterOptions(model);
        return "web/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                 BindingResult bindingResult,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("initialStep", resolveFirstErrorStep(bindingResult));
            populateRegisterOptions(model);
            return "web/register";
        }

        try {
            AuthResponse authResponse = authService.register(registerRequest);
            addAuthCookie(response, request, authResponse.getToken(), authResponse.getExpiresIn());

            redirectAttributes.addFlashAttribute("registeredUser", authResponse.getUser());
            redirectAttributes.addFlashAttribute("registeredProfile", authResponse.getProfile());
            return "redirect:/register/success";
        } catch (DuplicateResourceException | InvalidRequestException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("initialStep", 1);
            populateRegisterOptions(model);
            return "web/register";
        }
    }

    @GetMapping("/register/success")
    public String registerSuccessPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (!model.containsAttribute("registeredUser")) {
            if (userDetails == null) {
                return "redirect:/login";
            }
            UserDto userDto = authService.getCurrentUser(userDetails.getUsername());
            FitnessProfileDto profileDto = null;
            try {
                profileDto = profileService.getProfileForUser(userDetails.getUsername());
            } catch (ResourceNotFoundException ignored) {
                // Profile optional on success summary
            }
            model.addAttribute("registeredUser", userDto);
            model.addAttribute("registeredProfile", profileDto);
        }
        return "web/register-success";
    }

    private void addAuthCookie(HttpServletResponse response,
                               HttpServletRequest request,
                               String token,
                               Long expiresInSeconds) {
        long maxAge = expiresInSeconds != null ? expiresInSeconds : 86400L;
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure || request.isSecure())
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private int resolveFirstErrorStep(BindingResult bindingResult) {
        int minStep = 6;
        for (FieldError error : bindingResult.getFieldErrors()) {
            Integer step = FIELD_STEP_MAP.get(error.getField());
            if (step != null && step < minStep) {
                minStep = step;
            }
        }
        return minStep;
    }

    private void populateRegisterOptions(Model model) {
        model.addAttribute("genders", WebFormOptions.GENDERS);
        model.addAttribute("fitnessGoals", WebFormOptions.FITNESS_GOALS);
        model.addAttribute("experienceLevels", WebFormOptions.EXPERIENCE_LEVELS);
        model.addAttribute("workoutLocations", WebFormOptions.WORKOUT_LOCATIONS);
        model.addAttribute("equipmentOptions", WebFormOptions.EQUIPMENT_OPTIONS);
        model.addAttribute("workoutDurations", WebFormOptions.WORKOUT_DURATIONS);
        model.addAttribute("dietPreferences", WebFormOptions.DIET_PREFERENCES);
    }
}
