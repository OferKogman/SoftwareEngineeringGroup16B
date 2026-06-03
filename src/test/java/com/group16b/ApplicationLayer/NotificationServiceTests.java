package com.group16b.ApplicationLayer;

import com.group16b.DomainLayer.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceTests {

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService();
    }

    @Test
    public void testNotifyOfflineUserQueuesNotification() {
        service.notify("user1", "hello");
        List<Notification> pending = service.getPendingNotifications("user1");
        assertEquals(1, pending.size());
        assertEquals("hello", pending.get(0).getMessage());
    }

    @Test
    public void testGetPendingClearsQueue() {
        service.notify("user1", "hello");
        service.getPendingNotifications("user1");
        List<Notification> pending = service.getPendingNotifications("user1");
        assertTrue(pending.isEmpty());
    }

    @Test
    public void testNotifyOnlineUserSendsImmediately() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        service.registerEmitter("user1", emitter);
        service.notify("user1", "hello");
        verify(emitter, atLeastOnce()).send(any(SseEmitter.SseEventBuilder.class));
        assertTrue(service.getPendingNotifications("user1").isEmpty());
    }

    @Test
    public void testRegisterEmitterFlushesQueue() throws IOException {
        service.notify("user1", "pending message");
        SseEmitter emitter = mock(SseEmitter.class);
        service.registerEmitter("user1", emitter);
        verify(emitter, atLeastOnce()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    public void testFailedEmitterQueuesNotification() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException()).when(emitter).send(any(SseEmitter.SseEventBuilder.class));
        service.registerEmitter("user1", emitter);
        service.notify("user1", "hello");
        List<Notification> pending = service.getPendingNotifications("user1");
        assertEquals(1, pending.size());
    }

    @Test
    public void testMultiplePendingNotifications() {
        service.notify("user1", "msg1");
        service.notify("user1", "msg2");
        service.notify("user1", "msg3");
        List<Notification> pending = service.getPendingNotifications("user1");
        assertEquals(3, pending.size());
    }

    @Test
    public void testDifferentUsersQueuedSeparately() {
        service.notify("user1", "msg for user1");
        service.notify("user2", "msg for user2");
        assertEquals(1, service.getPendingNotifications("user1").size());
        assertEquals(1, service.getPendingNotifications("user2").size());
    }
}