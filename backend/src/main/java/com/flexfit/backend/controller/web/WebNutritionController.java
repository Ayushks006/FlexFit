package com.flexfit.backend.controller.web;

import com.flexfit.backend.dto.request.NutritionLogRequest;
import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.dto.response.UserDto;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.service.AuthService;
import com.flexfit.backend.service.NutritionService;
import com.flexfit.backend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class WebNutritionController {

    private final AuthService authService;
    private final ProfileService profileService;
    private final NutritionService nutritionService;

    public WebNutritionController(AuthService authService,
                                  ProfileService profileService,
                                  NutritionService nutritionService) {
        this.authService = authService;
        this.profileService = profileService;
        this.nutritionService = nutritionService;
    }

    @GetMapping("/nutrition")
    public String nutritionPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "historyDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate historyDate,
            Model model) {
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        populateNutritionModel(userDetails.getUsername(), selectedDate, historyDate, model);

        if (!model.containsAttribute("nutritionLogRequest")) {
            NutritionLogRequest defaultReq = new NutritionLogRequest();
            defaultReq.setLoggedDate(selectedDate);
            defaultReq.setProteinG(BigDecimal.ZERO);
            defaultReq.setCarbsG(BigDecimal.ZERO);
            defaultReq.setFatsG(BigDecimal.ZERO);
            model.addAttribute("nutritionLogRequest", defaultReq);
        }
        return "web/nutrition";
    }

    @PostMapping("/nutrition/log")
    public String logMeal(@AuthenticationPrincipal UserDetails userDetails,
                          @Valid @ModelAttribute("nutritionLogRequest") NutritionLogRequest nutritionLogRequest,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        LocalDate selectedDate = nutritionLogRequest.getLoggedDate() != null
                ? nutritionLogRequest.getLoggedDate()
                : LocalDate.now();

        if (bindingResult.hasErrors()) {
            populateNutritionModel(userDetails.getUsername(), selectedDate, null, model);
            return "web/nutrition";
        }

        nutritionService.logMeal(userDetails.getUsername(), nutritionLogRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Meal logged to your nutrition tracker.");
        return "redirect:/nutrition?date=" + selectedDate;
    }

    @PostMapping("/nutrition/{id}/delete")
    public String deleteMeal(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable("id") Long id,
                             @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             RedirectAttributes redirectAttributes) {
        nutritionService.deleteMeal(userDetails.getUsername(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Meal entry removed and daily totals updated.");
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return "redirect:/nutrition?date=" + targetDate;
    }

    private void populateNutritionModel(String username,
                                        LocalDate selectedDate,
                                        LocalDate historyDate,
                                        Model model) {
        UserDto currentUser = authService.getCurrentUser(username);
        FitnessProfileDto profile = null;
        try {
            profile = profileService.getProfileForUser(username);
        } catch (ResourceNotFoundException ignored) {
            // Optional profile
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("profile", profile);
        model.addAttribute("activePage", "nutrition");
        model.addAttribute("pageTitle", "Diet & Nutrition");
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("historyDate", historyDate);
        model.addAttribute("mealCategories", NutritionService.MEAL_CATEGORIES);
        model.addAttribute("nutritionSummary", nutritionService.getDailySummary(username, selectedDate));
        model.addAttribute("nutritionHistory", nutritionService.getNutritionHistory(username, historyDate));
        model.addAttribute("foodRecommendations", nutritionService.getFoodRecommendations(username, selectedDate));
    }
}
