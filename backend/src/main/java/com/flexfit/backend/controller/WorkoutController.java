package com.flexfit.backend.controller;

import com.flexfit.backend.dto.request.WorkoutLogRequest;
import com.flexfit.backend.dto.response.ApiResponse;
import com.flexfit.backend.dto.response.RecommendedWorkoutDto;
import com.flexfit.backend.dto.response.WorkoutLogDto;
import com.flexfit.backend.dto.response.WorkoutSummaryDto;
import com.flexfit.backend.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendedWorkoutDto>>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<RecommendedWorkoutDto> recommendations = workoutService.getRecommendationsForUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Workout recommendations generated", recommendations));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWorkouts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<WorkoutLogDto> history = workoutService.getWorkoutHistory(userDetails.getUsername());
        WorkoutSummaryDto summary = workoutService.getWorkoutSummary(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Workout history retrieved", Map.of(
                "summary", summary,
                "history", history
        )));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutLogDto>> logWorkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkoutLogRequest request) {
        WorkoutLogDto created = workoutService.logWorkout(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workout logged successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteWorkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id) {
        workoutService.deleteWorkout(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Workout deleted", "Deleted"));
    }
}
