package com.group16b.infrastructureLayer.Notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import com.group16b.InfrastructureLayer.Notifications.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group16b.ApplicationLayer.DTOs.NotificationDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.Notification;
import com.group16b.InfrastructureLayer.Database.NotificationRepository;

class NotifierTests {

    private static final String TOKEN = "valid-token";
    private static final String BAD_TOKEN = "bad-token";
    private static final String USER_ID = "u1@example.com";
    private static final String MESSAGE = "You were invited to become a manager in company 6.";
    private static final String JSON_MESSAGE = "{\"message\":\"You were invited to become a manager in company 6.\"}";

    private IAuthenticationService authenticationService;
    private ObjectMapper objectMapper;
    private NotificationRepository notificationRepository;
    private Notifier notifier;

    @BeforeEach
    void setUp() {
        authenticationService = mock(IAuthenticationService.class);
        objectMapper = mock(ObjectMapper.class);
        notificationRepository = mock(NotificationRepository.class);

        notifier = new Notifier(
                authenticationService,
                objectMapper,
                notificationRepository
        );
    }

    @Test
    void notify_offlineUser_savesNotificationForLater() {
        notifier.notify(USER_ID, MESSAGE);

        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification savedNotification = notificationCaptor.getValue();

        assertEquals(USER_ID, savedNotification.getUserID());
        assertEquals(MESSAGE, savedNotification.getMessage());
        assertFalse(savedNotification.isSent());

        verifyNoInteractions(objectMapper);
    }

    @Test
    void notify_onlineUser_savesSendsAndMarksNotificationAsSent() throws Exception {
        WebSocketSession session = validOpenSession();

        List<Boolean> sentStatesAtSaveTime = new ArrayList<>();

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            sentStatesAtSaveTime.add(notification.isSent());
            return notification;
        });

        mockValidToken();
        when(notificationRepository.findUnsentByUserID(USER_ID)).thenReturn(List.of());
        when(objectMapper.writeValueAsString(any(NotificationDTO.class))).thenReturn(JSON_MESSAGE);

        notifier.afterConnectionEstablished(session);
        notifier.notify(USER_ID, MESSAGE);

        assertEquals(List.of(false, true), sentStatesAtSaveTime);

        ArgumentCaptor<TextMessage> textMessageCaptor =
                ArgumentCaptor.forClass(TextMessage.class);

        verify(session, times(1)).sendMessage(textMessageCaptor.capture());
        assertEquals(JSON_MESSAGE, textMessageCaptor.getValue().getPayload());

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void afterConnectionEstablished_sendsPendingNotificationsAndMarksThemAsSent() throws Exception {
        WebSocketSession session = validOpenSession();
        Notification pendingNotification = new Notification(USER_ID, MESSAGE);

        mockValidToken();
        when(notificationRepository.findUnsentByUserID(USER_ID))
                .thenReturn(List.of(pendingNotification));
        when(objectMapper.writeValueAsString(any(NotificationDTO.class))).thenReturn(JSON_MESSAGE);

        notifier.afterConnectionEstablished(session);

        assertTrue(pendingNotification.isSent());

        verify(session, times(1)).sendMessage(any(TextMessage.class));
        verify(notificationRepository, times(1)).save(pendingNotification);
    }

    @Test
    void afterConnectionEstablished_invalidToken_closesSessionAndDoesNotLoadNotifications() throws Exception {
        WebSocketSession session = invalidTokenSession();

        when(authenticationService.validateToken(BAD_TOKEN)).thenReturn(false);

        notifier.afterConnectionEstablished(session);

        verify(session, times(1)).close(any(CloseStatus.class));
        verify(notificationRepository, never()).findUnsentByUserID(any());
    }

    private void mockValidToken() {
        when(authenticationService.validateToken(TOKEN)).thenReturn(true);
        when(authenticationService.isUserToken(TOKEN)).thenReturn(true);
        when(authenticationService.extractSubjectFromToken(TOKEN)).thenReturn(USER_ID);
    }

    private WebSocketSession validOpenSession() {
        WebSocketSession session = mock(WebSocketSession.class);

        when(session.getId()).thenReturn("session-1");
        when(session.getUri()).thenReturn(
                URI.create("ws://localhost:8080/ws/notifications?token=" + TOKEN)
        );
        when(session.isOpen()).thenReturn(true);

        return session;
    }

    private WebSocketSession invalidTokenSession() {
        WebSocketSession session = mock(WebSocketSession.class);

        when(session.getId()).thenReturn("session-2");
        when(session.getUri()).thenReturn(
                URI.create("ws://localhost:8080/ws/notifications?token=" + BAD_TOKEN)
        );
        when(session.isOpen()).thenReturn(true);

        return session;
    }
}