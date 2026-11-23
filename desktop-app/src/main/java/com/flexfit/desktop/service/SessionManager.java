package com.flexfit.desktop.service;

import com.flexfit.desktop.model.FitnessProfileModel;
import com.flexfit.desktop.model.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private UserSession currentSession;
    private final List<Consumer<UserSession>> sessionListeners = new ArrayList<>();

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized void startSession(UserSession session) {
        this.currentSession = session;
        notifyListeners();
    }

    public synchronized void updateProfile(FitnessProfileModel updatedProfile) {
        if (this.currentSession != null) {
            this.currentSession.setProfile(updatedProfile);
            notifyListeners();
        }
    }

    public synchronized void clearSession() {
        this.currentSession = null;
        notifyListeners();
    }

    public synchronized UserSession getSession() {
        return currentSession;
    }

    public synchronized boolean isLoggedIn() {
        return currentSession != null && currentSession.isAuthenticated();
    }

    public synchronized String getToken() {
        return currentSession != null ? currentSession.getToken() : null;
    }

    public synchronized void addSessionListener(Consumer<UserSession> listener) {
        sessionListeners.add(listener);
    }

    public synchronized void removeSessionListener(Consumer<UserSession> listener) {
        sessionListeners.remove(listener);
    }

    private void notifyListeners() {
        for (Consumer<UserSession> listener : sessionListeners) {
            try {
                listener.accept(currentSession);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
