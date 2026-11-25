package com.flexfit.backend.service;

import com.flexfit.backend.dto.response.LeaderboardEntryDto;
import com.flexfit.backend.dto.response.LeaderboardResponseDto;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.UserChallengeRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.repository.WorkoutLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;
    private final FitnessProfileRepository fitnessProfileRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final ChallengeService challengeService;

    public LeaderboardService(UserRepository userRepository,
                              FitnessProfileRepository fitnessProfileRepository,
                              UserChallengeRepository userChallengeRepository,
                              WorkoutLogRepository workoutLogRepository,
                              ChallengeService challengeService) {
        this.userRepository = userRepository;
        this.fitnessProfileRepository = fitnessProfileRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.challengeService = challengeService;
    }

    @Transactional
    public LeaderboardResponseDto getLeaderboard(String username, String scopeParam) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        challengeService.syncProgressForUser(currentUser.getId());

        String scope = normalizeScope(scopeParam);
        Map<Long, FitnessProfile> profilesByUserId = fitnessProfileRepository.findAll()
                .stream()
                .collect(Collectors.toMap(fp -> fp.getUser().getId(), Function.identity(), (a, b) -> a));

        FitnessProfile currentProfile = profilesByUserId.get(currentUser.getId());
        String targetState = currentProfile != null && currentProfile.getState() != null
                ? currentProfile.getState().trim()
                : "";
        String targetDistrict = currentProfile != null && currentProfile.getDistrict() != null
                ? currentProfile.getDistrict().trim()
                : "";

        String filterLabel = switch (scope) {
            case "state" -> targetState.isEmpty() ? "State (Not Set)" : targetState;
            case "district" -> targetDistrict.isEmpty() ? "District (Not Set)" : targetDistrict + (targetState.isEmpty() ? "" : ", " + targetState);
            default -> "Global";
        };

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::isActive)
                .toList();

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        for (User u : activeUsers) {
            FitnessProfile fp = profilesByUserId.get(u.getId());
            if ("state".equals(scope)) {
                if (targetState.isEmpty() || fp == null || fp.getState() == null
                        || !fp.getState().trim().equalsIgnoreCase(targetState)) {
                    continue;
                }
            } else if ("district".equals(scope)) {
                if (targetDistrict.isEmpty() || fp == null || fp.getDistrict() == null
                        || !fp.getDistrict().trim().equalsIgnoreCase(targetDistrict)) {
                    continue;
                }
            }

            long points = userChallengeRepository.sumCompletedPointsByUserId(u.getId());
            long completedChallenges = userChallengeRepository.countByUserIdAndCompletedTrue(u.getId());
            long completedWorkouts = workoutLogRepository.countByUserId(u.getId());

            LeaderboardEntryDto entry = new LeaderboardEntryDto();
            entry.setUserId(u.getId());
            entry.setUsername(u.getUsername());
            entry.setDisplayName(u.getFullName());
            entry.setState(fp != null ? fp.getState() : null);
            entry.setDistrict(fp != null ? fp.getDistrict() : null);
            entry.setTotalPoints(points);
            entry.setCompletedChallenges(completedChallenges);
            entry.setCompletedWorkouts(completedWorkouts);
            entry.setCurrentUser(u.getId().equals(currentUser.getId()));
            entries.add(entry);
        }

        entries.sort(Comparator
                .comparingLong(LeaderboardEntryDto::getTotalPoints).reversed()
                .thenComparing(Comparator.comparingLong(LeaderboardEntryDto::getCompletedChallenges).reversed())
                .thenComparing(Comparator.comparingLong(LeaderboardEntryDto::getCompletedWorkouts).reversed())
                .thenComparing(LeaderboardEntryDto::getUsername, String.CASE_INSENSITIVE_ORDER));

        LeaderboardEntryDto currentUserEntry = null;
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntryDto e = entries.get(i);
            e.setRank(i + 1);
            if (e.isCurrentUser()) {
                currentUserEntry = e;
            }
        }

        LeaderboardResponseDto response = new LeaderboardResponseDto();
        response.setScope(scope);
        response.setFilterLabel(filterLabel);
        response.setCurrentUserEntry(currentUserEntry);
        response.setEntries(entries);
        return response;
    }

    private String normalizeScope(String scopeParam) {
        if (scopeParam == null) {
            return "global";
        }
        String lower = scopeParam.trim().toLowerCase(Locale.ROOT);
        if (lower.equals("state") || lower.equals("district")) {
            return lower;
        }
        return "global";
    }
}
