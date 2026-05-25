package com.group16b.DomainLayer.VirtualQueue;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VirtualQueueTests {

    private final String USER1 = "u1";
    private final String USER2 = "u2";

    VirtualQueue q;

    @BeforeEach
    void setup() {
        q = new VirtualQueue(0);
    }

    @Test
    void copyQueue_sucess() {
        q.addToQueue(USER1);
        VirtualQueue q2 = new VirtualQueue(q);
        assertAll(
                () -> assertEquals(q.isUserPassedQueue(USER1), q2.isUserPassedQueue(USER1)),
                () -> assertEquals(q.getId(), q2.getId()));
        // Validate the lists are new
        q.addToQueue(USER2);
        assertNotEquals(q.isUserPassedQueue(USER2), q2.isUserPassedQueue(USER2));
    }

    @Test
    void addToQueue_usersGetPassedOneAfterAnother()
            throws InterruptedException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Field PQ = q.getClass().getDeclaredField("PASS_NUM");
        PQ.setAccessible(true);
        PQ.set(q, 1);
        Field PT = q.getClass().getDeclaredField("PASS_TIMEOUT");
        PT.setAccessible(true);
        PT.set(q, 4000);
        q.addToQueue(USER1);
        q.addToQueue(USER2);
        assertTrue(q.isUserPassedQueue(USER1));
        assertFalse(q.isUserPassedQueue(USER2));
        Thread.sleep(5000);
        q.addToQueue(USER2);
        assertFalse(q.isUserPassedQueue(USER1));
        assertTrue(q.isUserPassedQueue(USER2));
    }

    @Test
    void isUserPassedQueue_success() {
        assertFalse(q.isUserPassedQueue(USER1));
        q.addToQueue(USER1);
        assertTrue(q.isUserPassedQueue(USER1));
    }
}
