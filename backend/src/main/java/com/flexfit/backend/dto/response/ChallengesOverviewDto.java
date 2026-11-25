package com.flexfit.backend.dto.response;

import java.util.List;

public class ChallengesOverviewDto {
    private long totalPoints;
    private int activeCount;
    private int completedCount;
    private int availableCount;
    private List<ChallengeViewDto> challenges;

    public ChallengesOverviewDto() {
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }

    public List<ChallengeViewDto> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<ChallengeViewDto> challenges) {
        this.challenges = challenges;
    }
}
