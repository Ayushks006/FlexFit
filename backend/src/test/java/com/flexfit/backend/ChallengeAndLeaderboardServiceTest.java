package com.flexfit.backend;

import com.flexfit.backend.dto.response.ChallengeViewDto;
import com.flexfit.backend.dto.response.ChallengesOverviewDto;
import com.flexfit.backend.dto.response.LeaderboardResponseDto;
import com.flexfit.backend.entity.Challenge;
import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.entity.UserChallenge;
import com.flexfit.backend.entity.WorkoutLog;
import com.flexfit.backend.exception.DuplicateResourceException;
import com.flexfit.backend.repository.ChallengeRepository;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.NutritionLogRepository;
import com.flexfit.backend.repository.UserChallengeRepository;
import com.flexfit.backend.repository.UserRepository;
import com.flexfit.backend.repository.WorkoutLogRepository;
import com.flexfit.backend.service.ChallengeService;
import com.flexfit.backend.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeAndLeaderboardServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FitnessProfileRepository fitnessProfileRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private UserChallengeRepository userChallengeRepository;
    @Mock
    private WorkoutLogRepository workoutLogRepository;
    @Mock
    private NutritionLogRepository nutritionLogRepository;

    private ChallengeService challengeService;
    private LeaderboardService leaderboardService;

    private User user1;
    private User user2;
    private Challenge consistencyChallenge;

    @BeforeEach
    void setUp() {
        challengeService = new ChallengeService(
                userRepository, challengeRepository, userChallengeRepository, workoutLogRepository, nutritionLogRepository);
        leaderboardService = new LeaderboardService(
                userRepository, fitnessProfileRepository, userChallengeRepository, workoutLogRepository, challengeService);

        user1 = new User("Athlete One", "athlete1", "a1@flexfit.test", "hash");
        user1.setId(1L);
        user2 = new User("Athlete Two", "athlete2", "a2@flexfit.test", "hash");
        user2.setId(2L);

        consistencyChallenge = new Challenge();
        consistencyChallenge.setId(100L);
        consistencyChallenge.setTitle("2-Session Starter");
        consistencyChallenge.setDescription("Complete 2 workouts");
        consistencyChallenge.setCategory("Consistency");
        consistencyChallenge.setTargetCount(2);
        consistencyChallenge.setPointsReward(150);
        consistencyChallenge.setActive(true);
    }

    @Test
    @DisplayName("Joining a challenge succeeds once and prevents duplicate participation")
    void testJoinAndDuplicatePrevention() {
        when(userRepository.findByUsername("athlete1")).thenReturn(Optional.of(user1));
        when(challengeRepository.findById(100L)).thenReturn(Optional.of(consistencyChallenge));
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, 100L)).thenReturn(false);
        when(userChallengeRepository.save(any(UserChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeViewDto joined = challengeService.joinChallenge("athlete1", 100L);
        assertEquals("In Progress", joined.getStatus());
        assertEquals(0, joined.getProgressCount());

        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, 100L)).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> challengeService.joinChallenge("athlete1", 100L));
    }

    @Test
    @DisplayName("Challenge progress syncs from real WorkoutLogs, completes once, and prevents duplicate points")
    void testProgressSyncAndCompletionIdempotency() {
        LocalDateTime joinedAt = LocalDateTime.now().minusHours(2);
        UserChallenge uc = new UserChallenge();
        uc.setUser(user1);
        uc.setChallenge(consistencyChallenge);
        uc.setJoinedAt(joinedAt);
        uc.setProgressCount(0);
        uc.setCompleted(false);

        WorkoutLog w1 = new WorkoutLog();
        w1.setWorkoutType("Strength");
        w1.setCompletedAt(joinedAt.plusMinutes(10));
        WorkoutLog w2 = new WorkoutLog();
        w2.setWorkoutType("Cardio");
        w2.setCompletedAt(joinedAt.plusMinutes(20));

        when(userRepository.findByUsername("athlete1")).thenReturn(Optional.of(user1));
        when(userChallengeRepository.findByUserIdOrderByJoinedAtDesc(1L)).thenReturn(List.of(uc));
        when(workoutLogRepository.findByUserIdOrderByCompletedAtDesc(1L)).thenReturn(List.of(w2, w1));
        when(nutritionLogRepository.findByUserIdOrderByLoggedDateDescCreatedAtDesc(1L)).thenReturn(List.of());
        when(challengeRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(consistencyChallenge));
        when(userChallengeRepository.sumCompletedPointsByUserId(1L)).thenReturn(150L);

        ChallengesOverviewDto overview = challengeService.getChallengesOverview("athlete1");
        assertTrue(uc.isCompleted());
        assertEquals(2, uc.getProgressCount());
        assertEquals(150L, overview.getTotalPoints());
        assertEquals(1, overview.getCompletedCount());

        // Second sync should not re-save completed UserChallenge
        clearInvocations(userChallengeRepository);
        challengeService.syncProgressForUser(1L);
        verify(userChallengeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Leaderboard ranks users by points and filters by global, state, and district")
    void testLeaderboardRankingAndLocationFilters() {
        FitnessProfile p1 = new FitnessProfile();
        p1.setUser(user1);
        p1.setState("California");
        p1.setDistrict("Downtown");

        FitnessProfile p2 = new FitnessProfile();
        p2.setUser(user2);
        p2.setState("Texas");
        p2.setDistrict("Austin");

        when(userRepository.findByUsername("athlete1")).thenReturn(Optional.of(user1));
        when(userChallengeRepository.findByUserIdOrderByJoinedAtDesc(1L)).thenReturn(List.of());
        when(fitnessProfileRepository.findAll()).thenReturn(List.of(p1, p2));
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        when(userChallengeRepository.sumCompletedPointsByUserId(1L)).thenReturn(150L);
        when(userChallengeRepository.sumCompletedPointsByUserId(2L)).thenReturn(300L);
        when(userChallengeRepository.countByUserIdAndCompletedTrue(1L)).thenReturn(1L);
        when(userChallengeRepository.countByUserIdAndCompletedTrue(2L)).thenReturn(2L);
        when(workoutLogRepository.countByUserId(1L)).thenReturn(3L);
        when(workoutLogRepository.countByUserId(2L)).thenReturn(5L);

        LeaderboardResponseDto global = leaderboardService.getLeaderboard("athlete1", "global");
        assertEquals(2, global.getEntries().size());
        assertEquals("athlete2", global.getEntries().get(0).getUsername());
        assertEquals(1, global.getEntries().get(0).getRank());
        assertEquals(2, global.getCurrentUserEntry().getRank());

        LeaderboardResponseDto stateOnly = leaderboardService.getLeaderboard("athlete1", "state");
        assertEquals(1, stateOnly.getEntries().size());
        assertEquals("athlete1", stateOnly.getEntries().get(0).getUsername());

        LeaderboardResponseDto districtOnly = leaderboardService.getLeaderboard("athlete1", "district");
        assertEquals(1, districtOnly.getEntries().size());
        assertEquals("Downtown, California", districtOnly.getFilterLabel());
    }

    @Test
    @DisplayName("Empty leaderboard when no users match location filter")
    void testEmptyLeaderboardBehavior() {
        when(userRepository.findByUsername("athlete1")).thenReturn(Optional.of(user1));
        when(userChallengeRepository.findByUserIdOrderByJoinedAtDesc(1L)).thenReturn(List.of());
        when(fitnessProfileRepository.findAll()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of(user1));

        LeaderboardResponseDto emptyDistrict = leaderboardService.getLeaderboard("athlete1", "district");
        assertTrue(emptyDistrict.getEntries().isEmpty());
        assertNull(emptyDistrict.getCurrentUserEntry());
    }
}
