package com.group16b.ApiLayer;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private static final long STREAM_TIMEOUT_MS = 30L * 60L * 1000L;

    private final INotificationService notificationService;
    private final IAuthenticationService authenticationService;

    public NotificationController(INotificationService notificationService,
                                  IAuthenticationService authenticationService) {
        this.notificationService = notificationService;
        this.authenticationService = authenticationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestHeader("Authorization") String token) {
        if (!authenticationService.validateToken(token) || !authenticationService.isUserToken(token)) {
            logger.warn("NotificationController.stream: rejected invalid/non-user token");
            SseEmitter emitter = new SseEmitter(1000L);
            emitter.completeWithError(new SecurityException("Unauthorized notification stream request"));
            return emitter;
        }

        String userID = authenticationService.extractSubjectFromToken(token);
        logger.info("NotificationController.stream: opening stream for user {}", userID);

        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        notificationService.registerEmitter(userID, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("connected"));
        } catch (Exception e) {
            logger.warn("NotificationController.stream: failed to send connected event to user {}", userID);
        }

        return emitter;
    }
}