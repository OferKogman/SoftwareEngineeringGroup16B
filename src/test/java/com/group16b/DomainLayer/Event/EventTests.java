package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Records.EventRecord;

public class EventTests {


    @Test
    public void SuccessefulEventCreation() {
        assertDoesNotThrow(() -> {
            Event event = new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, null, null));
        });
    } 

    @Test
    public void FailedEventCreationNullName() {
        try {
            new Event(new EventRecord("1",null, LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Event name cannot be null or empty.", e.getMessage());
        }
    }

     @Test
    public void FailedEventCreationEmptyName() {
        try {
            new Event(new EventRecord("1","", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Event name cannot be null or empty.", e.getMessage());
        }
    }

    @Test
    public void FailedEventCreationNullStartTime() {
        try {
            new Event(new EventRecord("1","name", null, LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Start time and end time cannot be null.", e.getMessage());
        }
    }

    @Test
    public void FailedEventCreationNullEndTime() {
        try {
            new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), null, "Artist", "Category", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Start time and end time cannot be null.", e.getMessage());
        }
    }

     @Test
    public void FailedEventCreationEndTimeBeforeStartTime() {
        try {
            new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T12:00:00"), LocalDateTime.parse("2027-10-10T10:00:00"), "Artist", "Category", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Start time must be before end time.", e.getMessage());
        }
    }

    @Test
    public void FailedEventCreationEndTimeInPast() {
        try {
            new Event(new EventRecord("1","name", LocalDateTime.parse("2025-10-10T10:00:00"), LocalDateTime.parse("2025-10-10T12:00:00"), "Artist", "Category", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("End time must be in the future.", e.getMessage());
        }
    }

    @Test
    public void FailedEventCreationNullArtist() {
        try {
            new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), null, "Category", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Event artist cannot be null or empty.", e.getMessage());
        }
    }

     @Test
    public void FailedEventCreationEmptyArtist() {
        try {
            new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "", "Category", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Event artist cannot be null or empty.", e.getMessage());
        }
    }

     @Test
    public void FailedEventCreationNullCategory() {
        try {
            new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", null, 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Event category cannot be null or empty.", e.getMessage());
        }
    }

     @Test
    public void FailedEventCreationEmptyCategory() {
        try {
            new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "", 1, null, null));
            throw new Exception("Expected exception was not thrown.");
        } catch (Exception e) {
            assertEquals("Event category cannot be null or empty.", e.getMessage());
        }
    }

    @Test
    public void SuccessfulEventActivation() {
        assertDoesNotThrow(() -> {
            Event event = new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, null, null));
            event.activateEvent();
        });
    }

    @Test
    public void FailedEventActivationAlreadyActive() {
        try {
            Event event = new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, null, null));
            event.activateEvent();
            event.activateEvent();
        } catch (Exception e) {
            assertEquals("Event is already active.", e.getMessage());
        }
    }

    @Test 
    public void SuccessfulEventDeactivation() {
        assertDoesNotThrow(() -> {
            Event event = new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, null, null));
            event.activateEvent();
            event.deactivateEvent();
        });
    }

    @Test 
    public void FailedEventDeactivationAlreadyInactive() {
        try {
            Event event = new Event(new EventRecord("1","name", LocalDateTime.parse("2027-10-10T10:00:00"), LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, null, null));
            event.deactivateEvent();
        } catch (Exception e) {
            assertEquals("Event is already inactive.", e.getMessage());
        }
    }

}
