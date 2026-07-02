package com.group16b.ApiLayer;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.INotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationTestEndpoint {

    private final INotificationService notificationService;
    private final IAuthenticationService authService;

    public NotificationTestEndpoint(INotificationService notificationService,
                                    IAuthenticationService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @PostMapping("/test")
    public ResponseEntity<String> sendTestNotification(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "message", defaultValue = "This is a test notification!") String message) {

        if (!authService.validateToken(token) || !authService.isUserToken(token)) {
            return ResponseEntity.status(401).body("Invalid token");
        }

        String userID = authService.extractSubjectFromToken(token);
        notificationService.notify(userID, message);

        return ResponseEntity.ok("Notification sent to: " + userID);
    }
}