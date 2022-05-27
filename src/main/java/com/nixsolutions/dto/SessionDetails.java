package com.nixsolutions.dto;

public class SessionDetails {

    private final String sessionId;
    private final String userId;

    public SessionDetails(String sessionId, String userId) {
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }
}
