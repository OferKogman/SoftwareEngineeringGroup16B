package com.group16b.DomainLayer.Event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VenueTests {
    Venue venue;
    Event e1;
    Event e2;

    @BeforeEach
    void SetUp() {
        venue = new Venue("V", "LA", null);
        Date date = new Date();
        e1 = mock(Event.class);
        when(e1.getEventID()).thenReturn(1);
        when(e1.getEventDate()).thenReturn(date);
        e2 = mock(Event.class);
        when(e2.getEventID()).thenReturn(2);
        when(e2.getEventDate()).thenReturn(date);
    }

    @Test
    void SuccessfulAddEvent() {
        venue.addEvent(e1.getEventDate(), e1.getEventID());
        assertEquals(e1.getEventID(), venue.getEventIDByDate(e1.getEventDate()));
    }

    @Test
    void FailedAddEventVenueReserved() {
        venue.addEvent(e1.getEventDate(), e1.getEventID());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            venue.addEvent(e2.getEventDate(), e2.getEventID());
        });
        assertEquals("Venue is already reserved for requested date !", ex.getMessage());
    }

    @Test
    void SuccessfulRemoveEvent() {
        venue.addEvent(e1.getEventDate(), e1.getEventID());
        venue.removeEvent(e1.getEventDate(), e1.getEventID());
        assertEquals(null, venue.getEventIDByDate(e1.getEventDate()));
    }

    @Test
    void FailedRemoveEventWrongEvent() {
        venue.addEvent(e1.getEventDate(), e1.getEventID());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            venue.removeEvent(e1.getEventDate(), e2.getEventID());
        });
        assertEquals("Venue is not reserved for this event at requested date !", ex.getMessage());
    }

    @Test
    void FailedRemoveEventWrongDate() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            venue.removeEvent(e1.getEventDate(), e1.getEventID());
        });
        assertEquals("Venue is not reserved for this event at requested date !", ex.getMessage());
    }
}
