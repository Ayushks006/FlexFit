package com.flexfit.backend.controller;

import com.flexfit.backend.dto.response.ApiResponse;
import com.flexfit.backend.dto.response.LeaderboardResponseDto;
import com.flexfit.backend.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<LeaderboardResponseDto>> getLeaderboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "scope", defaultValue = "global") String scope) {
        LeaderboardResponseDto leaderboard = leaderboardService.getLeaderboard(userDetails.getUsername(), scope);
        return ResponseEntity.ok(ApiResponse.success("Leaderboard retrieved", leaderboard));
    }
}
