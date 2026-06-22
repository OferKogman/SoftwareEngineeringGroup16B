package com.group16b.ApplicationLayer;

import com.group16b.ApplicationLayer.Interfaces.INotificationService;
import com.group16b.DomainLayer.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
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
        Notification notification = new Notification(userID, message);
        SseEmitter emitter = emitters.get(userID);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(message));
                notification.markSent();
                logger.info("NotificationService.notify: Sent real-time notification to user {}", userID);
            } catch (IOException e) {
                logger.warn("NotificationService.notify: Failed to send to user {}, queuing", userID);
                emitters.remove(userID);
                queue(userID, notification);
            }
        } else {
            queue(userID, notification);
            logger.info("NotificationService.notify: User {} offline, notification queued", userID);
        }
    }

    @Override
    public List<Notification> getPendingNotifications(String userID) {
        List<Notification> notifications = pending.getOrDefault(userID, new ArrayList<>());
        pending.remove(userID);
        return notifications;
    }

    @Override
    public void registerEmitter(String userID, SseEmitter emitter) {
        emitters.put(userID, emitter);
        logger.info("NotificationService.registerEmitter: User {} connected", userID);
        List<Notification> pendingForUser = getPendingNotifications(userID);
        for (Notification n : pendingForUser) {
            try {
                emitter.send(SseEmitter.event().data(n.getMessage()));
                n.markSent();
            } catch (IOException e) {
                logger.warn("NotificationService.registerEmitter: Failed to flush pending for user {}", userID);
            }
        }
        emitter.onCompletion(() -> emitters.remove(userID));
        emitter.onTimeout(() -> emitters.remove(userID));
    }

    private void queue(String userID, Notification notification) {
        pending.computeIfAbsent(userID, k -> new ArrayList<>()).add(notification);
    }
}