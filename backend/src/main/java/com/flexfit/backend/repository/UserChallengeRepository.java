package com.flexfit.backend.repository;

import com.flexfit.backend.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    List<UserChallenge> findByUserIdOrderByJoinedAtDesc(Long userId);

    Optional<UserChallenge> findByUserIdAndChallengeId(Long userId, Long challengeId);

    boolean existsByUserIdAndChallengeId(Long userId, Long challengeId);

    long countByUserIdAndCompletedTrue(Long userId);

    @Query("SELECT COALESCE(SUM(uc.challenge.pointsReward), 0) " +
           "FROM UserChallenge uc " +
           "WHERE uc.user.id = :userId AND uc.completed = true")
    long sumCompletedPointsByUserId(@Param("userId") Long userId);
}
