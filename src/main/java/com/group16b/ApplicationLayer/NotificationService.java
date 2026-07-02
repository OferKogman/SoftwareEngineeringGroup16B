package com.group16b.ApplicationLayer;

import com.group16b.ApplicationLayer.Interfaces.INotificationService;
import com.group16b.DomainLayer.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService implements INotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, List<Notification>> pending = new ConcurrentHashMap<>();

    @Override
    public void notify(String userID, String message) {
        if (userID == null || userID.isBlank() || message == null || message.isBlank()) {
            return;
        }

        Notification notification = new Notification(userID, message);
        SseEmitter emitter = emitters.get(userID);

        if (emitter == null) {
            queue(userID, notification);
            logger.info("NotificationService.notify: User {} offline, notification queued", userID);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(message));
            notification.markSent();
            logger.info("NotificationService.notify: Sent real-time notification to user {}", userID);
        } catch (IOException | IllegalStateException e) {
            logger.warn("NotificationService.notify: Failed to send to user {}, queuing", userID);
            emitters.remove(userID, emitter);
            queue(userID, notification);
        }
    }

    @Override
    public List<Notification> getPendingNotifications(String userID) {
        List<Notification> notifications = pending.remove(userID);
        if (notifications == null) {
            return new ArrayList<>();
        }

        synchronized (notifications) {
            return new ArrayList<>(notifications);
        }
    }

    @Override
    public void registerEmitter(String userID, SseEmitter emitter) {
        if (userID == null || userID.isBlank() || emitter == null) {
            return;
        }

        emitters.put(userID, emitter);
        logger.info("NotificationService.registerEmitter: User {} connected", userID);

        emitter.onCompletion(() -> emitters.remove(userID, emitter));
        emitter.onTimeout(() -> emitters.remove(userID, emitter));
        emitter.onError(error -> emitters.remove(userID, emitter));

        List<Notification> pendingForUser = getPendingNotifications(userID);
        List<Notification> failedToFlush = new ArrayList<>();

        for (Notification notification : pendingForUser) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification.getMessage()));
                notification.markSent();
            } catch (IOException | IllegalStateException e) {
                failedToFlush.add(notification);
            }
        }

        if (!failedToFlush.isEmpty()) {
            emitters.remove(userID, emitter);
            pending.put(userID, Collections.synchronizedList(failedToFlush));
            logger.warn("NotificationService.registerEmitter: Failed to flush pending notifications for user {}", userID);
        }
    }

    private void queue(String userID, Notification notification) {
        pending.computeIfAbsent(userID, key -> Collections.synchronizedList(new ArrayList<>()))
                .add(notification);
    }
}
