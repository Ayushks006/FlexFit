package com.flexfit.backend.repository;

import com.flexfit.backend.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    List<WorkoutLog> findByUserIdOrderByCompletedAtDesc(Long userId);

    List<WorkoutLog> findByUserIdAndCompletedAtBetweenOrderByCompletedAtDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    Optional<WorkoutLog> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCompletedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
