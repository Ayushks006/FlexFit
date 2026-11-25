package com.flexfit.backend.controller.web;

import com.flexfit.backend.dto.response.FitnessProfileDto;
import com.flexfit.backend.dto.response.UserDto;
import com.flexfit.backend.exception.DuplicateResourceException;
import com.flexfit.backend.exception.InvalidRequestException;
import com.flexfit.backend.exception.ResourceNotFoundException;
import com.flexfit.backend.service.AuthService;
import com.flexfit.backend.service.ChallengeService;
import com.flexfit.backend.service.LeaderboardService;
import com.flexfit.backend.service.ProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebChallengeController {

    private final AuthService authService;
    private final ProfileService profileService;
    private final ChallengeService challengeService;
    private final LeaderboardService leaderboardService;

    public WebChallengeController(AuthService authService,
                                  ProfileService profileService,
                                  ChallengeService challengeService,
                                  LeaderboardService leaderboardService) {
        this.authService = authService;
        this.profileService = profileService;
        this.challengeService = challengeService;
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/challenges")
    public String challengesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        populateUserContext(userDetails.getUsername(), "challenges", "Fitness Challenges", model);
        model.addAttribute("overview", challengeService.getChallengesOverview(userDetails.getUsername()));
        return "web/challenges";
    }

    @PostMapping("/challenges/{id}/join")
    public String joinChallenge(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            challengeService.joinChallenge(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Challenge joined! Progress updates automatically as you log workouts and meals.");
        } catch (DuplicateResourceException | InvalidRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/challenges";
    }

    @GetMapping("/leaderboard")
    public String leaderboardPage(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam(name = "scope", defaultValue = "global") String scope,
                                  Model model) {
        populateUserContext(userDetails.getUsername(), "leaderboard", "Community Leaderboard", model);
        model.addAttribute("leaderboard", leaderboardService.getLeaderboard(userDetails.getUsername(), scope));
        return "web/leaderboard";
    }

    private void populateUserContext(String username, String activePage, String pageTitle, Model model) {
        UserDto currentUser = authService.getCurrentUser(username);
        FitnessProfileDto profile = null;
        try {
            profile = profileService.getProfileForUser(username);
        } catch (ResourceNotFoundException ignored) {
            // Optional profile
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("profile", profile);
        model.addAttribute("activePage", activePage);
        model.addAttribute("pageTitle", pageTitle);
    }
}
