package org.example.client;

import org.example.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String sessionId;

    private SessionManager() {}


    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
    }


    public User getCurrentUser() {
        return currentUser;
    }


    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public String getSessionId() {
        return sessionId;
    }


    public boolean isLoggedIn() {
        return currentUser != null;
    }


    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }


    public void logout() {
        currentUser = null;
        sessionId = null;
    }
}

