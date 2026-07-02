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
    public void notifyOfflineUserQueuesNotification() {
        service.notify("user1", "hello");

        List<Notification> pending = service.getPendingNotifications("user1");

        assertEquals(1, pending.size());
        assertEquals("user1", pending.get(0).getUserID());
        assertEquals("hello", pending.get(0).getMessage());
    }

    @Test
    public void getPendingClearsQueue() {
        service.notify("user1", "hello");

        service.getPendingNotifications("user1");
        List<Notification> pending = service.getPendingNotifications("user1");

        assertTrue(pending.isEmpty());
    }

    @Test
    public void notifyOnlineUserSendsImmediately() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        service.registerEmitter("user1", emitter);

        service.notify("user1", "hello");

        verify(emitter, atLeastOnce()).send(any(SseEmitter.SseEventBuilder.class));
        assertTrue(service.getPendingNotifications("user1").isEmpty());
    }

    @Test
    public void registerEmitterFlushesQueue() throws IOException {
        service.notify("user1", "pending message");
        SseEmitter emitter = mock(SseEmitter.class);

        service.registerEmitter("user1", emitter);

        verify(emitter, atLeastOnce()).send(any(SseEmitter.SseEventBuilder.class));
        assertTrue(service.getPendingNotifications("user1").isEmpty());
    }

    @Test
    public void failedEmitterQueuesNotification() throws IOException {
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException()).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        service.registerEmitter("user1", emitter);
        service.notify("user1", "hello");

        List<Notification> pending = service.getPendingNotifications("user1");
        assertEquals(1, pending.size());
        assertEquals("hello", pending.get(0).getMessage());
    }

    @Test
    public void failedFlushKeepsPendingNotification() throws IOException {
        service.notify("user1", "pending message");

        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException()).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        service.registerEmitter("user1", emitter);

        List<Notification> pending = service.getPendingNotifications("user1");
        assertEquals(1, pending.size());
        assertEquals("pending message", pending.get(0).getMessage());
    }

    @Test
    public void multiplePendingNotificationsArePreserved() {
        service.notify("user1", "msg1");
        service.notify("user1", "msg2");
        service.notify("user1", "msg3");

        List<Notification> pending = service.getPendingNotifications("user1");

        assertEquals(3, pending.size());
        assertEquals("msg1", pending.get(0).getMessage());
        assertEquals("msg2", pending.get(1).getMessage());
        assertEquals("msg3", pending.get(2).getMessage());
    }

    @Test
    public void differentUsersAreQueuedSeparately() {
        service.notify("user1", "msg for user1");
        service.notify("user2", "msg for user2");

        assertEquals(1, service.getPendingNotifications("user1").size());
        assertEquals(1, service.getPendingNotifications("user2").size());
    }

    @Test
    public void blankNotificationsAreIgnored() {
        service.notify("user1", "");
        service.notify("", "hello");
        service.notify(null, "hello");
        service.notify("user1", null);

        assertTrue(service.getPendingNotifications("user1").isEmpty());
    }
}
