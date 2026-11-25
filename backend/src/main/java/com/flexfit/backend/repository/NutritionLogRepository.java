package com.flexfit.backend.repository;

import com.flexfit.backend.entity.NutritionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {

    List<NutritionLog> findByUserIdOrderByLoggedDateDescCreatedAtDesc(Long userId);

    List<NutritionLog> findByUserIdAndLoggedDateOrderByCreatedAtDesc(Long userId, LocalDate loggedDate);

    List<NutritionLog> findByUserIdAndLoggedDateBetweenOrderByLoggedDateDescCreatedAtDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    Optional<NutritionLog> findByIdAndUserId(Long id, Long userId);
}
