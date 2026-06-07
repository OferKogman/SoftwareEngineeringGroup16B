package com.group16b.ApiLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.INotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final INotificationService notificationService;
    private final IAuthenticationService authenticationService;

    public NotificationController(INotificationService notificationService,
                                  IAuthenticationService authenticationService) {
        this.notificationService = notificationService;
        this.authenticationService = authenticationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("token") String token) {
        if (!authenticationService.validateToken(token) || !authenticationService.isUserToken(token)) {
            logger.warn("NotificationController.stream: rejected SSE connection for invalid/non-user token");
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new SecurityException("Unauthorized notification stream request"));
            return emitter;
        }

        String userID = authenticationService.extractSubjectFromToken(token);
        logger.info("NotificationController.stream: opening notification stream for user {}", userID);

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        notificationService.registerEmitter(userID, emitter);
        return emitter;
    }
}