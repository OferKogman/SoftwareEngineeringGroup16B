package com.group16b.InfrastructureLayer.Notifications;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.Notification;
import com.group16b.ApplicationLayer.DTOs.NotificationDTO;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final ConcurrentMap<String, WebSocketSession> sessionsByUser = new ConcurrentHashMap<>();

    private final IAuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    public NotificationWebSocketHandler(IAuthenticationService authenticationService,
                                        ObjectMapper objectMapper) {
        this.authenticationService = authenticationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);

        if (token == null ||
                !authenticationService.validateToken(token) ||
                !authenticationService.isUserToken(token)) {

            logger.warn("NotificationWebSocketHandler: rejected invalid notification WebSocket");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized notification socket"));
            return;
        }

        String userID = authenticationService.extractSubjectFromToken(token);
        sessionsByUser.put(userID, session);

        logger.info("NotificationWebSocketHandler: user {} connected", userID);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.warn("NotificationWebSocketHandler: WebSocket transport error", exception);
        removeSession(session);
    }

    public boolean sendToUser(String userID, Notification notification) {
        WebSocketSession session = sessionsByUser.get(userID);

        if (session == null || !session.isOpen()) {
            return false;
        }

        try {
            String json = objectMapper.writeValueAsString(NotificationDTO.from(notification));
            session.sendMessage(new TextMessage(json));

            notification.markSent();
            return true;

        } catch (Exception e) {
            logger.warn("NotificationWebSocketHandler: failed to send notification to user {}", userID, e);
            removeSession(session);
            return false;
        }
    }

    private void removeSession(WebSocketSession session) {
        if (session == null) {
            return;
        }

        sessionsByUser.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }

    private String extractToken(WebSocketSession session) {
        URI uri = session.getUri();

        if (uri == null || uri.getQuery() == null) {
            return null;
        }

        String[] queryParts = uri.getQuery().split("&");

        for (String part : queryParts) {
            if (part.startsWith("token=")) {
                return URLDecoder.decode(part.substring("token=".length()), StandardCharsets.UTF_8);
            }
        }

        return null;
    }
}