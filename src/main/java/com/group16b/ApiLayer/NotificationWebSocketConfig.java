package com.group16b.ApiLayer;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.group16b.InfrastructureLayer.Notifications.Notifier;

@Configuration
@EnableWebSocket
public class NotificationWebSocketConfig implements WebSocketConfigurer {

    private final Notifier notifier;

    public NotificationWebSocketConfig(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notifier, "/ws/notifications")
                .setAllowedOrigins("*");
    }
}