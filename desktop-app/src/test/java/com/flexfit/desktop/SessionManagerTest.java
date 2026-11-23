package com.flexfit.desktop;

import com.flexfit.desktop.model.FitnessProfileModel;
import com.flexfit.desktop.model.UserSession;
import com.flexfit.desktop.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @Test
    @DisplayName("SessionManager starts session, stores JWT token, and notifies registered listeners")
    void testSessionLifecycle() {
        AtomicBoolean listenerNotified = new AtomicBoolean(false);
        sessionManager.addSessionListener(session -> {
            if (session != null) {
                listenerNotified.set(true);
            }
        });

        FitnessProfileModel profile = new FitnessProfileModel();
        profile.setFitnessGoal("Muscle gain");
        profile.setCurrentWeightKg(new BigDecimal("75.0"));
        profile.setHeightCm(new BigDecimal("180.0"));

        UserSession userSession = new UserSession(
                1L, "Jordan Smith", "jsmith", "jordan@flexfit.test", "ROLE_USER", "mock-jwt-token-xyz", profile
        );

        sessionManager.startSession(userSession);

        assertTrue(sessionManager.isLoggedIn());
        assertEquals("mock-jwt-token-xyz", sessionManager.getToken());
        assertEquals("jsmith", sessionManager.getSession().getUsername());
        assertEquals(new BigDecimal("23.1"), sessionManager.getSession().getProfile().getBmi());
        assertTrue(listenerNotified.get());

        // Update profile
        profile.setCurrentWeightKg(new BigDecimal("78.0"));
        sessionManager.updateProfile(profile);
        assertEquals(new BigDecimal("78.0"), sessionManager.getSession().getProfile().getCurrentWeightKg());

        // Clear session on logout
        sessionManager.clearSession();
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getToken());
        assertNull(sessionManager.getSession());
    }
}
