package com.flexfit.backend.controller;

import com.flexfit.backend.dto.request.NutritionLogRequest;
import com.flexfit.backend.dto.response.ApiResponse;
import com.flexfit.backend.dto.response.FoodRecommendationDto;
import com.flexfit.backend.dto.response.NutritionLogDto;
import com.flexfit.backend.dto.response.NutritionSummaryDto;
import com.flexfit.backend.service.NutritionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<NutritionSummaryDto>> getSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        NutritionSummaryDto summary = nutritionService.getDailySummary(userDetails.getUsername(), date);
        return ResponseEntity.ok(ApiResponse.success("Nutrition summary retrieved", summary));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<NutritionLogDto>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<NutritionLogDto> history = nutritionService.getNutritionHistory(userDetails.getUsername(), date);
        return ResponseEntity.ok(ApiResponse.success("Nutrition history retrieved", history));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<FoodRecommendationDto>>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<FoodRecommendationDto> recs = nutritionService.getFoodRecommendations(userDetails.getUsername(), date);
        return ResponseEntity.ok(ApiResponse.success("Food recommendations generated", recs));
    }

    @PostMapping("/logs")
    public ResponseEntity<ApiResponse<NutritionLogDto>> logMeal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NutritionLogRequest request) {
        NutritionLogDto created = nutritionService.logMeal(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Meal logged successfully", created));
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMeal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id) {
        nutritionService.deleteMeal(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Meal deleted", "Deleted"));
    }
}
