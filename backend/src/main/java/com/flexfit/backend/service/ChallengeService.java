package com.flexfit.backend.service;

import com.flexfit.backend.dto.response.ChallengeViewDto;
import com.flexfit.backend.dto.response.ChallengesOverviewDto;
import com.flexfit.backend.entity.Challenge;
import com.flexfit.backend.entity.NutritionLog;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.entity.UserChallenge;
import com.flexfit.backend.entity.WorkoutLog;
import com.flexfit.backend.exception.DuplicateResourceException;
import com.flexfit.backend.exception.InvalidRequestException;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.repository.ChallengeRepository;
import com.flexfit.backend.repository.NutritionLogRepository;
import com.flexfit.backend.repository.UserChallengeRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.repository.WorkoutLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ChallengeService {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final NutritionLogRepository nutritionLogRepository;

    public ChallengeService(UserRepository userRepository,
                            ChallengeRepository challengeRepository,
                            UserChallengeRepository userChallengeRepository,
                            WorkoutLogRepository workoutLogRepository,
                            NutritionLogRepository nutritionLogRepository) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.nutritionLogRepository = nutritionLogRepository;
    }

    @Transactional
    public ChallengesOverviewDto getChallengesOverview(String username) {
        User user = findUser(username);
        syncProgressForUser(user.getId());

        List<Challenge> activeChallenges = challengeRepository.findByActiveTrueOrderByIdAsc();
        Map<Long, UserChallenge> joinedMap = userChallengeRepository.findByUserIdOrderByJoinedAtDesc(user.getId())
                .stream()
                .collect(Collectors.toMap(uc -> uc.getChallenge().getId(), Function.identity(), (a, b) -> a));

        List<ChallengeViewDto> views = new ArrayList<>();
        int activeCount = 0;
        int completedCount = 0;
        int availableCount = 0;

        for (Challenge c : activeChallenges) {
            UserChallenge uc = joinedMap.get(c.getId());
            ChallengeViewDto dto = mapToViewDto(c, uc);
            views.add(dto);
            switch (dto.getStatus()) {
                case "Completed" -> completedCount++;
                case "In Progress" -> activeCount++;
                default -> availableCount++;
            }
        }

        long totalPoints = userChallengeRepository.sumCompletedPointsByUserId(user.getId());

        ChallengesOverviewDto overview = new ChallengesOverviewDto();
        overview.setTotalPoints(totalPoints);
        overview.setActiveCount(activeCount);
        overview.setCompletedCount(completedCount);
        overview.setAvailableCount(availableCount);
        overview.setChallenges(views);
        return overview;
    }

    @Transactional
    public ChallengeViewDto getChallengeDetails(String username, Long challengeId) {
        User user = findUser(username);
        syncProgressForUser(user.getId());

        Challenge challenge = challengeRepository.findById(challengeId)
                .filter(Challenge::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        UserChallenge uc = userChallengeRepository
                .findByUserIdAndChallengeId(user.getId(), challengeId)
                .orElse(null);

        return mapToViewDto(challenge, uc);
    }

    @Transactional
    public List<ChallengeViewDto> getJoinedChallenges(String username) {
        User user = findUser(username);
        syncProgressForUser(user.getId());

        return userChallengeRepository.findByUserIdOrderByJoinedAtDesc(user.getId())
                .stream()
                .map(uc -> mapToViewDto(uc.getChallenge(), uc))
                .toList();
    }

    @Transactional
    public ChallengeViewDto joinChallenge(String username, Long challengeId) {
        User user = findUser(username);
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        if (!challenge.isActive()) {
            throw new InvalidRequestException("This challenge is no longer active");
        }

        if (userChallengeRepository.existsByUserIdAndChallengeId(user.getId(), challengeId)) {
            throw new DuplicateResourceException("You have already joined '" + challenge.getTitle() + "'");
        }

        UserChallenge participation = new UserChallenge();
        participation.setUser(user);
        participation.setChallenge(challenge);
        participation.setProgressCount(0);
        participation.setCompleted(false);
        participation.setJoinedAt(LocalDateTime.now());

        UserChallenge saved = userChallengeRepository.save(participation);
        return mapToViewDto(challenge, saved);
    }

    @Transactional
    public void syncProgressForUser(Long userId) {
        List<UserChallenge> participations = userChallengeRepository.findByUserIdOrderByJoinedAtDesc(userId);
        if (participations.isEmpty()) {
            return;
        }

        boolean hasIncomplete = participations.stream().anyMatch(uc -> !uc.isCompleted());
        if (!hasIncomplete) {
            return;
        }

        List<WorkoutLog> workouts = workoutLogRepository.findByUserIdOrderByCompletedAtDesc(userId);
        List<NutritionLog> meals = nutritionLogRepository.findByUserIdOrderByLoggedDateDescCreatedAtDesc(userId);

        for (UserChallenge uc : participations) {
            if (uc.isCompleted()) {
                continue;
            }
            Challenge challenge = uc.getChallenge();
            int actual = calculateProgress(challenge, uc.getJoinedAt(), workouts, meals);
            int clamped = Math.min(actual, challenge.getTargetCount());

            boolean changed = clamped != uc.getProgressCount();
            uc.setProgressCount(clamped);
            if (clamped >= challenge.getTargetCount()) {
                uc.setCompleted(true);
                uc.setCompletedAt(LocalDateTime.now());
                changed = true;
            }
            if (changed) {
                userChallengeRepository.save(uc);
            }
        }
    }

    private int calculateProgress(Challenge challenge,
                                  LocalDateTime joinedAt,
                                  List<WorkoutLog> workouts,
                                  List<NutritionLog> meals) {
        String category = challenge.getCategory() != null
                ? challenge.getCategory().trim().toLowerCase(Locale.ROOT)
                : "";
        LocalDate joinedDate = joinedAt != null ? joinedAt.toLocalDate() : LocalDate.now();

        if (category.contains("nutrition")) {
            return (int) meals.stream()
                    .map(NutritionLog::getLoggedDate)
                    .filter(d -> d != null && !d.isBefore(joinedDate))
                    .distinct()
                    .count();
        }

        return (int) workouts.stream()
                .filter(w -> w.getCompletedAt() != null && (joinedAt == null || !w.getCompletedAt().isBefore(joinedAt)))
                .filter(w -> matchesWorkoutCategory(category, w.getWorkoutType()))
                .count();
    }

    private boolean matchesWorkoutCategory(String challengeCategory, String workoutType) {
        String wt = workoutType != null ? workoutType.toLowerCase(Locale.ROOT) : "";
        if (challengeCategory.contains("cardio")) {
            return wt.contains("cardio") || wt.contains("hiit");
        }
        if (challengeCategory.contains("strength")) {
            return wt.contains("strength") || wt.contains("full body");
        }
        if (challengeCategory.contains("flexibility") || challengeCategory.contains("mobility")) {
            return wt.contains("mobility") || wt.contains("flexibility");
        }
        return true; // Consistency / Wellness / Weight Management count all workouts
    }

    private ChallengeViewDto mapToViewDto(Challenge challenge, UserChallenge uc) {
        ChallengeViewDto dto = new ChallengeViewDto();
        dto.setId(challenge.getId());
        dto.setTitle(challenge.getTitle());
        dto.setDescription(challenge.getDescription());
        dto.setCategory(challenge.getCategory());
        dto.setTargetCount(challenge.getTargetCount());
        dto.setPointsReward(challenge.getPointsReward());

        if (uc == null) {
            dto.setStatus("Available");
            dto.setProgressCount(0);
            dto.setProgressPercent(0);
        } else {
            dto.setJoinedAt(uc.getJoinedAt());
            dto.setCompletedAt(uc.getCompletedAt());
            int progress = uc.getProgressCount() != null ? uc.getProgressCount() : 0;
            dto.setProgressCount(progress);
            int percent = challenge.getTargetCount() > 0
                    ? (int) Math.min(100, Math.round((progress * 100.0) / challenge.getTargetCount()))
                    : 0;
            dto.setProgressPercent(percent);
            dto.setStatus(uc.isCompleted() ? "Completed" : "In Progress");
        }
        return dto;
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
