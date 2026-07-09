package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDateTime;

import com.group16b.DomainLayer.Notification;

public record NotificationDTO(
        String message,
        LocalDateTime timestamp,
        boolean sent
) {
    public static NotificationDTO from(Notification notification) {
        return new NotificationDTO(
                notification.getMessage(),
                notification.getTimestamp(),
                notification.isSent()
        );
    }
}