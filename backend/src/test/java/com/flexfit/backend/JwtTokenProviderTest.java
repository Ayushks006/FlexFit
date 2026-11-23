package com.flexfit.backend;

import com.flexfit.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "test_super_secret_key_at_least_32_bytes_long_1234567890";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("generateToken produces a valid signed JWT containing username and userId")
    void testGenerateAndValidateToken() {
        String token = tokenProvider.generateToken("member1", 101L, "ROLE_USER");

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("member1", tokenProvider.getUsernameFromToken(token));
        assertEquals(101L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("validateToken returns false for corrupted or tampered token")
    void testTamperedTokenRejection() {
        String token = tokenProvider.generateToken("member1", 101L, "ROLE_USER");
        String tamperedToken = token + "xyz";

        assertFalse(tokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("validateToken returns false for expired token")
    void testExpiredTokenRejection() {
        // Create token provider with 1 millisecond expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, 1L);
        String token = shortLivedProvider.generateToken("member1", 101L, "ROLE_USER");

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }

        assertFalse(shortLivedProvider.validateToken(token));
    }
}
