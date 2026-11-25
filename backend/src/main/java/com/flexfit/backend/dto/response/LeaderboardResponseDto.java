package com.flexfit.backend.dto.response;

import java.util.List;

public class LeaderboardResponseDto {
    private String scope;
    private String filterLabel;
    private LeaderboardEntryDto currentUserEntry;
    private List<LeaderboardEntryDto> entries;

    public LeaderboardResponseDto() {
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getFilterLabel() {
        return filterLabel;
    }

    public void setFilterLabel(String filterLabel) {
        this.filterLabel = filterLabel;
    }

    public LeaderboardEntryDto getCurrentUserEntry() {
        return currentUserEntry;
    }

    public void setCurrentUserEntry(LeaderboardEntryDto currentUserEntry) {
        this.currentUserEntry = currentUserEntry;
    }

    public List<LeaderboardEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<LeaderboardEntryDto> entries) {
        this.entries = entries;
    }
}
