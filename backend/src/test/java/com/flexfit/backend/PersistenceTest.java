package com.flexfit.backend;

import com.flexfit.backend.entity.FitnessProfile;
import com.flexfit.backend.entity.User;
import com.flexfit.backend.repository.FitnessProfileRepository;
import com.flexfit.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FitnessProfileRepository fitnessProfileRepository;

    @Test
    @DisplayName("User and linked FitnessProfile persist and retrieve successfully")
    void testUserAndProfilePersistence() {
        User user = new User("Fitness Member", "fituser1", "fituser1@flexfit.test", "hashed_password");
        User savedUser = userRepository.saveAndFlush(user);

        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());

        FitnessProfile profile = new FitnessProfile();
        profile.setUser(savedUser);
        profile.setAge(25);
        profile.setGender("Female");
        profile.setDistrict("Downtown");
        profile.setState("Metro");
        profile.setHeightCm(new BigDecimal("168.0"));
        profile.setCurrentWeightKg(new BigDecimal("58.0"));
        profile.setTargetWeightKg(new BigDecimal("55.0"));
        profile.setFitnessGoal("General fitness");
        profile.setExperienceLevel("Beginner");
        profile.setWorkoutLocation("Home");
        profile.setPreferredWorkoutDurationMin(30);
        profile.setDietPreference("Vegetarian");

        FitnessProfile savedProfile = fitnessProfileRepository.saveAndFlush(profile);
        assertNotNull(savedProfile.getId());
        assertEquals(savedUser.getId(), savedProfile.getUser().getId());

        Optional<FitnessProfile> retrieved = fitnessProfileRepository.findByUserId(savedUser.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("General fitness", retrieved.get().getFitnessGoal());
    }

    @Test
    @DisplayName("Persisting duplicate username violates unique constraint")
    void testDuplicateUsernameConstraint() {
        User user1 = new User("Member One", "duplicate_user", "email1@flexfit.test", "hash1");
        userRepository.saveAndFlush(user1);

        User user2 = new User("Member Two", "duplicate_user", "email2@flexfit.test", "hash2");
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }

    @Test
    @DisplayName("Persisting duplicate email violates unique constraint")
    void testDuplicateEmailConstraint() {
        User user1 = new User("Member One", "user1", "duplicate@flexfit.test", "hash1");
        userRepository.saveAndFlush(user1);

        User user2 = new User("Member Two", "user2", "duplicate@flexfit.test", "hash2");
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }
}
