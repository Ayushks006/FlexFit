package com.flexfit.backend.repository;

import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FitnessProfileRepository extends JpaRepository<FitnessProfile, Long> {

    Optional<FitnessProfile> findByUserId(Long userId);

    Optional<FitnessProfile> findByUser(User user);
}
