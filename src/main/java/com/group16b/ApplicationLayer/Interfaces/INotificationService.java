package com.group16b.ApplicationLayer.Interfaces;

import com.group16b.DomainLayer.Notification;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

public interface INotificationService {
    void notify(String userID, String message);
    List<Notification> getPendingNotifications(String userID);
    void registerEmitter(String userID, SseEmitter emitter);
}