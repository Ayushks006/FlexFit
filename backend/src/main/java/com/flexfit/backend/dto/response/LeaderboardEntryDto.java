package com.flexfit.backend.dto.response;

public class LeaderboardEntryDto {
    private int rank;
    private Long userId;
    private String username;
    private String displayName;
    private String state;
    private String district;
    private long totalPoints;
    private long completedChallenges;
    private long completedWorkouts;
    private boolean currentUser;

    public LeaderboardEntryDto() {
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public long getCompletedChallenges() {
        return completedChallenges;
    }

    public void setCompletedChallenges(long completedChallenges) {
        this.completedChallenges = completedChallenges;
    }

    public long getCompletedWorkouts() {
        return completedWorkouts;
    }

    public void setCompletedWorkouts(long completedWorkouts) {
        this.completedWorkouts = completedWorkouts;
    }

    public boolean isCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        this.currentUser = currentUser;
    }
}
