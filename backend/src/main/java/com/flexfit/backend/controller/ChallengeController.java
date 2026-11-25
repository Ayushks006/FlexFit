package com.flexfit.backend.controller;

import com.flexfit.backend.dto.response.ApiResponse;
import com.flexfit.backend.dto.response.ChallengeViewDto;
import com.flexfit.backend.dto.response.ChallengesOverviewDto;
import com.flexfit.backend.service.ChallengeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ChallengesOverviewDto>> listChallenges(
            @AuthenticationPrincipal UserDetails userDetails) {
        ChallengesOverviewDto overview = challengeService.getChallengesOverview(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Challenges retrieved", overview));
    }

    @GetMapping("/joined")
    public ResponseEntity<ApiResponse<List<ChallengeViewDto>>> listJoinedChallenges(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ChallengeViewDto> joined = challengeService.getJoinedChallenges(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Joined challenges retrieved", joined));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChallengeViewDto>> getChallengeDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id) {
        ChallengeViewDto details = challengeService.getChallengeDetails(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Challenge details retrieved", details));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<ChallengeViewDto>> joinChallenge(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id) {
        ChallengeViewDto joined = challengeService.joinChallenge(userDetails.getUsername(), id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Joined challenge successfully", joined));
    }
}
