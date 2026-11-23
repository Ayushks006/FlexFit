package com.flexfit.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSession {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String role;
    private String token;
    private FitnessProfileModel profile;

    public UserSession() {
    }

    public UserSession(Long id, String fullName, String username, String email, String role, String token, FitnessProfileModel profile) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.role = role;
        this.token = token;
        this.profile = profile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public FitnessProfileModel getProfile() {
        return profile;
    }

    public void setProfile(FitnessProfileModel profile) {
        this.profile = profile;
    }

    public boolean isAuthenticated() {
        return token != null && !token.isBlank();
    }
}
