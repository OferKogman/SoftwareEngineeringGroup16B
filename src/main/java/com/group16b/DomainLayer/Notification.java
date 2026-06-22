package com.group16b.DomainLayer;

import java.time.LocalDateTime;

public class Notification {
    private final String userID;
    private final String message;
    private final LocalDateTime timestamp;
    private boolean sent;

    public Notification(String userID, String message) {
        this.userID = userID;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.sent = false;
    }

    public String getUserID() { return userID; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSent() { return sent; }
    public void markSent() { this.sent = true; }
}