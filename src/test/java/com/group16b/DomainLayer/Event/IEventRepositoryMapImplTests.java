package com.group16b.DomainLayer.Event;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IEventRepositoryMapImplTests {
    IEventRepositoryMapImpl repository;

    @BeforeEach
    void setUp() {
        repository = IEventRepositoryMapImpl.getInstance();
    }

    @Test
    void testAddAndGetEventById() {
        Event mockEvent1= mock(Event.class);
        when(mockEvent1.getEventID()).thenReturn(1);
        repository.addEvent(mockEvent1);
        Event retrievedEvent = repository.getEventByID(mockEvent1.getEventID());
        assertEquals(mockEvent1, retrievedEvent);
    }

    @Test
    void testGetEventByIdNotFound() {
        Event retrievedEvent = repository.getEventByID(999);
        assertEquals(null, retrievedEvent);
    }

    @Test
    void testEventExists() {
        Event mockEvent2 = mock(Event.class);
        when(mockEvent2.getEventID()).thenReturn(2);
        repository.addEvent(mockEvent2);
        assertEquals(true, repository.EventExists(mockEvent2.getEventID()));
        assertEquals(false, repository.EventExists(999));
    }

    @Test
    void testDuplicateEventID() {
        Event mockEvent3 = mock(Event.class);
        when(mockEvent3.getEventID()).thenReturn(3);
        repository.addEvent(mockEvent3);
        
        Event mockEventDuplicate = mock(Event.class);
        when(mockEventDuplicate.getEventID()).thenReturn(3);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addEvent(mockEventDuplicate);
        });
        
        assertEquals("Event with this ID already exists", exception.getMessage());
    }

    @Test
    void testAddNullEvent() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addEvent(null);
        });
        
        assertEquals("Event cannot be null", exception.getMessage());
    }
}
