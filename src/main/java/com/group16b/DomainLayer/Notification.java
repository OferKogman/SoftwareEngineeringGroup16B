package com.group16b.DomainLayer;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    private String id;

    @Column(nullable = false)
    private String userID;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean sent;

    @Version
    private long version;

    protected Notification() {
        // Required by JPA
    }

    public Notification(String userID, String message) {
        if (userID == null || userID.isBlank()) {
            throw new IllegalArgumentException("Notification recipient userID cannot be empty");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message cannot be empty");
        }

        this.id = UUID.randomUUID().toString();
        this.userID = userID;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.sent = false;
    }

    public String getId() {
        return id;
    }

    public String getUserID() {
        return userID;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isSent() {
        return sent;
    }

    public void markSent() {
        this.sent = true;
    }

    public long getVersion() {
        return version;
    }
}