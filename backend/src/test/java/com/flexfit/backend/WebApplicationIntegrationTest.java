package com.flexfit.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexfit.backend.dto.request.LoginRequest;
import com.flexfit.backend.entity.Challenge;
import com.flexfit.backend.repository.ChallengeRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    @DisplayName("Public web pages render with expected content and CSRF protection")
    void testPublicPagesRender() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FlexFit")))
                .andExpect(content().string(containsString("Transform Your")))
                .andExpect(content().string(containsString("Elevate Your")));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("_csrf")));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Account Information")))
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    @DisplayName("Unauthenticated browser requests redirect to /login while API requests return JSON 401")
    void testUnauthorizedAccessBehavior() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/api/v1/users/me/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Web forms enforce CSRF protection")
    void testCsrfEnforcementOnWebForms() throws Exception {
        mockMvc.perform(post("/login")
                        .param("usernameOrEmail", "nobody")
                        .param("password", "secret123"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/register")
                        .param("username", "nobody"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Full web registration, cookie auth, dashboard, profile edit, logout, and REST API compatibility")
    void testFullWebAndApiFlow() throws Exception {
        // 1. Register via web form with CSRF
        MvcResult registerResult = mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Web Athlete")
                        .param("username", "webathlete")
                        .param("email", "webathlete@flexfit.test")
                        .param("password", "StrongPass123!")
                        .param("confirmPassword", "StrongPass123!")
                        .param("age", "27")
                        .param("gender", "Male")
                        .param("district", "Central")
                        .param("state", "Metro")
                        .param("heightCm", "180.0")
                        .param("currentWeightKg", "81.0")
                        .param("targetWeightKg", "75.0")
                        .param("fitnessGoal", "Weight loss")
                        .param("experienceLevel", "Intermediate")
                        .param("workoutLocation", "Gym")
                        .param("availableEquipment", "Dumbbells", "Barbell")
                        .param("preferredWorkoutDurationMin", "45")
                        .param("dietPreference", "Non-vegetarian"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register/success"))
                .andReturn();

        String setCookie = registerResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("FLEXFIT_TOKEN=");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Lax");

        Cookie authCookie = extractCookie(setCookie, "FLEXFIT_TOKEN");
        assertThat(authCookie).isNotNull();

        // 2. Access protected web pages using the HttpOnly cookie
        mockMvc.perform(get("/dashboard").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back, Web Athlete!")))
                .andExpect(content().string(containsString("81.00 kg")))
                .andExpect(content().string(containsString("75.00 kg")));

        mockMvc.perform(get("/profile").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("@webathlete")));

        mockMvc.perform(get("/profile/edit").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Edit Fitness Profile")));

        mockMvc.perform(get("/settings").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HttpOnly JWT cookie")));

        for (String modulePath : new String[]{"/workouts", "/nutrition", "/challenges", "/leaderboard"}) {
            mockMvc.perform(get(modulePath).cookie(authCookie))
                    .andExpect(status().isOk());
        }

        // 3. Update profile via web form
        mockMvc.perform(post("/profile/edit")
                        .with(csrf())
                        .cookie(authCookie)
                        .param("age", "28")
                        .param("gender", "Male")
                        .param("district", "Central")
                        .param("state", "Metro")
                        .param("heightCm", "180.0")
                        .param("currentWeightKg", "79.5")
                        .param("targetWeightKg", "75.0")
                        .param("fitnessGoal", "Muscle gain")
                        .param("experienceLevel", "Intermediate")
                        .param("workoutLocation", "Gym")
                        .param("availableEquipment", "Dumbbells")
                        .param("preferredWorkoutDurationMin", "60")
                        .param("dietPreference", "Non-vegetarian"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        // 4. Logout clears the HttpOnly cookie
        MvcResult logoutResult = mockMvc.perform(post("/logout")
                        .with(csrf())
                        .cookie(authCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andReturn();

        String clearedCookieHeader = logoutResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(clearedCookieHeader).isNotNull();
        assertThat(clearedCookieHeader).contains("Max-Age=0");

        // 5. Login via web form
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("usernameOrEmail", "webathlete")
                        .param("password", "StrongPass123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();

        Cookie reloginCookie = extractCookie(
                loginResult.getResponse().getHeader(HttpHeaders.SET_COOKIE), "FLEXFIT_TOKEN");
        assertThat(reloginCookie).isNotNull();

        // 6. Verify existing REST API still works with Bearer token (no CSRF required)
        LoginRequest apiLogin = new LoginRequest("webathlete", "StrongPass123!");
        MvcResult apiLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode json = objectMapper.readTree(apiLoginResult.getResponse().getContentAsString());
        String bearerToken = json.path("data").path("token").asText();
        assertThat(bearerToken).isNotBlank();

        mockMvc.perform(get("/api/v1/users/me/profile")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fitnessGoal").value("Muscle gain"));

        // 7. Workout module: empty state, recommendations, logging, invalid input, REST API, and ownership
        mockMvc.perform(get("/workouts").cookie(reloginCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No workout history yet")))
                .andExpect(content().string(containsString("Recommended Workouts for Your Profile")));

        // Invalid input (duration = 0) stays on workouts page with validation error
        mockMvc.perform(post("/workouts/log")
                        .with(csrf())
                        .cookie(reloginCookie)
                        .param("title", "")
                        .param("workoutType", "Strength")
                        .param("durationMin", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Workout name is required")));

        // Valid web workout log
        mockMvc.perform(post("/workouts/log")
                        .with(csrf())
                        .cookie(reloginCookie)
                        .param("title", "Afternoon Hypertrophy")
                        .param("workoutType", "Strength")
                        .param("durationMin", "50")
                        .param("notes", "Felt strong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workouts"));

        mockMvc.perform(get("/workouts").cookie(reloginCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Afternoon Hypertrophy")))
                .andExpect(content().string(containsString("50 min")));

        // REST API workout endpoints
        mockMvc.perform(get("/api/v1/workouts/recommendations")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/workouts")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalWorkouts").value(1));

        mockMvc.perform(delete("/api/v1/workouts/999999")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isNotFound());

        // 8. Nutrition module: overview, meal logging, validation, history, REST API, and ownership
        mockMvc.perform(get("/nutrition").cookie(reloginCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Daily Calorie Target")))
                .andExpect(content().string(containsString("No meals logged for this date")));

        // Invalid input (negative calories) returns form validation error
        mockMvc.perform(post("/nutrition/log")
                        .with(csrf())
                        .cookie(reloginCookie)
                        .param("mealName", "")
                        .param("mealType", "Breakfast")
                        .param("calories", "-50")
                        .param("proteinG", "10")
                        .param("carbsG", "20")
                        .param("fatsG", "5")
                        .param("loggedDate", java.time.LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Meal name is required")));

        // Valid meal log via web form
        String todayStr = java.time.LocalDate.now().toString();
        mockMvc.perform(post("/nutrition/log")
                        .with(csrf())
                        .cookie(reloginCookie)
                        .param("mealName", "Protein Oats Bowl")
                        .param("mealType", "Breakfast")
                        .param("calories", "410")
                        .param("proteinG", "24.5")
                        .param("carbsG", "52.0")
                        .param("fatsG", "9.5")
                        .param("loggedDate", todayStr))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nutrition?date=" + todayStr));

        mockMvc.perform(get("/nutrition").cookie(reloginCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Protein Oats Bowl")))
                .andExpect(content().string(containsString("410 kcal")));

        // REST API nutrition endpoints
        mockMvc.perform(get("/api/v1/nutrition/summary")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.caloriesConsumed").value(410))
                .andExpect(jsonPath("$.data.mealsLoggedCount").value(1));

        mockMvc.perform(get("/api/v1/nutrition/recommendations")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(delete("/api/v1/nutrition/logs/999999")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isNotFound());

        // 9. Challenges & Leaderboard module: seed challenge, join, prevent duplicate, sync progress, leaderboard
        Challenge testChallenge = new Challenge();
        testChallenge.setTitle("Integration 1-Workout Sprint");
        testChallenge.setDescription("Complete 1 workout after joining");
        testChallenge.setCategory("Consistency");
        testChallenge.setTargetCount(1);
        testChallenge.setPointsReward(200);
        testChallenge.setActive(true);
        Challenge savedChallenge = challengeRepository.saveAndFlush(testChallenge);

        mockMvc.perform(get("/challenges").cookie(reloginCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Integration 1-Workout Sprint")));

        // Join via web form with CSRF
        mockMvc.perform(post("/challenges/" + savedChallenge.getId() + "/join")
                        .with(csrf())
                        .cookie(reloginCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/challenges"));

        // Duplicate join via REST API returns 409 Conflict
        mockMvc.perform(post("/api/v1/challenges/" + savedChallenge.getId() + "/join")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isConflict());

        // Log 1 workout to complete the challenge automatically
        mockMvc.perform(post("/workouts/log")
                        .with(csrf())
                        .cookie(reloginCookie)
                        .param("title", "Sprint Finisher")
                        .param("workoutType", "Cardio")
                        .param("durationMin", "25"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api/v1/challenges")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPoints").value(200))
                .andExpect(jsonPath("$.data.completedCount").value(1));

        // Leaderboard global, state, district & dashboard points
        mockMvc.perform(get("/leaderboard?scope=global").cookie(reloginCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your Rank: #1")))
                .andExpect(content().string(containsString("200 pts")));

        mockMvc.perform(get("/api/v1/leaderboard?scope=district")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentUserEntry.totalPoints").value(200));

        mockMvc.perform(get("/dashboard").cookie(reloginCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("200 pts")));
    }

    private Cookie extractCookie(String setCookieHeader, String name) {
        if (setCookieHeader == null) {
            return null;
        }
        for (String part : setCookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(name + "=")) {
                return new Cookie(name, trimmed.substring(name.length() + 1));
            }
        }
        return null;
    }
}
