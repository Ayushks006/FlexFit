package com.flexfit.backend.repository;

import com.flexfit.backend.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByActiveTrueOrderByIdAsc();

    List<Challenge> findByActiveTrueAndCategoryOrderByIdAsc(String category);
}
