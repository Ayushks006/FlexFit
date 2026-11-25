package com.flexfit.backend.service;

import com.flexfit.backend.dto.request.WorkoutLogRequest;
import com.flexfit.backend.dto.response.RecommendedWorkoutDto;
import com.flexfit.backend.dto.response.WeeklyDayActivityDto;
import com.flexfit.backend.dto.response.WorkoutLogDto;
import com.flexfit.backend.dto.response.WorkoutSummaryDto;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.entity.WorkoutLog;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.repository.WorkoutLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    private final UserRepository userRepository;
    private final FitnessProfileRepository fitnessProfileRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutRecommendationEngine recommendationEngine;

    public WorkoutService(UserRepository userRepository,
                          FitnessProfileRepository fitnessProfileRepository,
                          WorkoutLogRepository workoutLogRepository,
                          WorkoutRecommendationEngine recommendationEngine) {
        this.userRepository = userRepository;
        this.fitnessProfileRepository = fitnessProfileRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.recommendationEngine = recommendationEngine;
    }

    @Transactional(readOnly = true)
    public List<RecommendedWorkoutDto> getRecommendationsForUser(String username) {
        User user = findUser(username);
        FitnessProfile profile = fitnessProfileRepository.findByUserId(user.getId()).orElse(null);
        return recommendationEngine.recommend(profile);
    }

    @Transactional
    public WorkoutLogDto logWorkout(String username, WorkoutLogRequest request) {
        User user = findUser(username);
        FitnessProfile profile = fitnessProfileRepository.findByUserId(user.getId()).orElse(null);

        int calories = request.getCaloriesBurned() != null
                ? request.getCaloriesBurned()
                : recommendationEngine.estimateCalories(
                        request.getWorkoutType(),
                        request.getDurationMin(),
                        profile != null ? profile.getCurrentWeightKg() : BigDecimal.valueOf(70));

        WorkoutLog log = new WorkoutLog();
        log.setUser(user);
        log.setTitle(request.getTitle().trim());
        log.setWorkoutType(request.getWorkoutType().trim());
        log.setDurationMin(request.getDurationMin());
        log.setCaloriesBurned(calories);
        log.setCompletedAt(LocalDateTime.now());
        log.setNotes(request.getNotes() != null && !request.getNotes().isBlank() ? request.getNotes().trim() : null);

        WorkoutLog saved = workoutLogRepository.save(log);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteWorkout(String username, Long workoutId) {
        User user = findUser(username);
        WorkoutLog log = workoutLogRepository.findByIdAndUserId(workoutId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout log not found"));
        workoutLogRepository.delete(log);
    }

    @Transactional(readOnly = true)
    public List<WorkoutLogDto> getWorkoutHistory(String username) {
        User user = findUser(username);
        return workoutLogRepository.findByUserIdOrderByCompletedAtDesc(user.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutSummaryDto getWorkoutSummary(String username) {
        User user = findUser(username);
        List<WorkoutLog> allLogs = workoutLogRepository.findByUserIdOrderByCompletedAtDesc(user.getId());

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(6);
        LocalDateTime weekStartDateTime = startOfWeek.atStartOfDay();
        LocalDateTime weekEndDateTime = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<WorkoutLog> weeklyLogs = workoutLogRepository
                .findByUserIdAndCompletedAtBetweenOrderByCompletedAtDesc(user.getId(), weekStartDateTime, weekEndDateTime);

        int totalDuration = allLogs.stream().mapToInt(WorkoutLog::getDurationMin).sum();
        int totalCalories = allLogs.stream().mapToInt(WorkoutLog::getCaloriesBurned).sum();
        int weeklyDuration = weeklyLogs.stream().mapToInt(WorkoutLog::getDurationMin).sum();
        int weeklyCalories = weeklyLogs.stream().mapToInt(WorkoutLog::getCaloriesBurned).sum();

        Map<LocalDate, List<WorkoutLog>> byDate = weeklyLogs.stream()
                .collect(Collectors.groupingBy(w -> w.getCompletedAt().toLocalDate()));

        int maxMinutes = byDate.values().stream()
                .mapToInt(list -> list.stream().mapToInt(WorkoutLog::getDurationMin).sum())
                .max()
                .orElse(0);

        List<WeeklyDayActivityDto> weeklyActivity = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            List<WorkoutLog> dayLogs = byDate.getOrDefault(day, List.of());
            int dayMinutes = dayLogs.stream().mapToInt(WorkoutLog::getDurationMin).sum();
            int dayCalories = dayLogs.stream().mapToInt(WorkoutLog::getCaloriesBurned).sum();
            int heightPercent = (maxMinutes > 0 && dayMinutes > 0)
                    ? Math.max(12, (int) Math.round((dayMinutes * 100.0) / maxMinutes))
                    : 0;

            DayOfWeek dow = day.getDayOfWeek();
            String label = dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            weeklyActivity.add(new WeeklyDayActivityDto(label, day, dayLogs.size(), dayMinutes, dayCalories, heightPercent));
        }

        WorkoutSummaryDto summary = new WorkoutSummaryDto();
        summary.setTotalWorkouts(allLogs.size());
        summary.setWeeklyWorkouts(weeklyLogs.size());
        summary.setWeeklyDurationMin(weeklyDuration);
        summary.setTotalDurationMin(totalDuration);
        summary.setWeeklyCaloriesBurned(weeklyCalories);
        summary.setTotalCaloriesBurned(totalCalories);
        summary.setWeeklyFrequencyPerWeek(weeklyLogs.size());
        summary.setWeeklyActivity(weeklyActivity);
        return summary;
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private WorkoutLogDto mapToDto(WorkoutLog log) {
        return new WorkoutLogDto(
                log.getId(),
                log.getTitle(),
                log.getWorkoutType(),
                log.getDurationMin(),
                log.getCaloriesBurned(),
                log.getCompletedAt(),
                log.getNotes()
        );
    }
}
