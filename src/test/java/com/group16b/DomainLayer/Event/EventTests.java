package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;

public class EventTests {

	@Test
	public void SuccessefulEventCreation() {
		assertDoesNotThrow(() -> {
			Event event = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
		});
	}

	@Test
	public void FailedEventCreationNullName() {
		try {
			new Event(new EventRecord("1", null, LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event name cannot be null or empty.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationEmptyName() {
		try {
			new Event(new EventRecord("1", "", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event name cannot be null or empty.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationNullStartTime() {
		try {
			new Event(new EventRecord("1", "name", null, LocalDateTime.parse("2027-10-10T12:00:00"), "Artist",
					"Category", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Start time and end time cannot be null.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationNullEndTime() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"), null, "Artist",
					"Category", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Start time and end time cannot be null.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationEndTimeBeforeStartTime() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T12:00:00"),
					LocalDateTime.parse("2027-10-10T10:00:00"), "Artist", "Category", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Start time must be before end time.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationEndTimeInPast() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2025-10-10T10:00:00"),
					LocalDateTime.parse("2025-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("End time must be in the future.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationNullArtist() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), null, "Category", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event artist cannot be null or empty.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationEmptyArtist() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "", "Category", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event artist cannot be null or empty.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationNullCategory() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", null, 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event category cannot be null or empty.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationEmptyCategory() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "", 1, 0, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event category cannot be null or empty.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationNegativePrice() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category",1, -1, 0), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event price cannot be negative.", e.getMessage());
		}
	}

	@Test
	public void FailedEventCreationInvalidRating() {
		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 6), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event rating must be between 0 and 5.", e.getMessage());
		}

		try {
			new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1,0, -1), "0");
			throw new Exception("Expected exception was not thrown.");
		} catch (Exception e) {
			assertEquals("Event rating must be between 0 and 5.", e.getMessage());
		}
	}

	@Test
	public void SuccessfulEventCreationFromOther() {
		Event otherEvent = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
				LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
		Event newEvent = new Event(otherEvent);
		assertAll(
			() -> assertEquals(otherEvent.getEventID(), newEvent.getEventID()),
			() -> assertEquals(otherEvent.getEventStatus(), newEvent.getEventStatus()),
			() -> assertEquals(otherEvent.getEventVenueID(), newEvent.getEventVenueID()),
			() -> assertEquals(otherEvent.getEventName(), newEvent.getEventName()),
			() -> assertEquals(otherEvent.getEventStartTime(), newEvent.getEventStartTime()),
			() -> assertEquals(otherEvent.getEventEndTime(), newEvent.getEventEndTime()),
			() -> assertEquals(otherEvent.getEventArtist(), newEvent.getEventArtist()),
			() -> assertEquals(otherEvent.getEventCategory(), newEvent.getEventCategory()),
			() -> assertEquals(otherEvent.getEventProductionCompanyID(), newEvent.getEventProductionCompanyID()),
			() -> assertEquals(otherEvent.getEventDiscountPolicy(), newEvent.getEventDiscountPolicy()),
			() -> assertEquals(otherEvent.getEventPurchasePolicy(), newEvent.getEventPurchasePolicy()),
			() -> assertEquals(otherEvent.getEventPrice(), newEvent.getEventPrice()),
			() -> assertEquals(otherEvent.getEventRating(), newEvent.getEventRating()),
			() -> assertEquals(otherEvent.getOwnerId(), newEvent.getOwnerId()),
			() -> assertEquals(otherEvent.getVersion(), newEvent.getVersion())
		);
	}

	@Test
	public void SuccessfulEventActivation() {
		assertDoesNotThrow(() -> {
			Event event = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			event.activateEvent();
		});
	}

	@Test
	public void FailedEventActivationAlreadyActive() {
		try {
			Event event = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			event.activateEvent();
			event.activateEvent();
		} catch (Exception e) {
			assertEquals("Event is already active.", e.getMessage());
		}
	}

	@Test
	public void SuccessfulEventDeactivation() {
		assertDoesNotThrow(() -> {
			Event event = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1,0, 0), "0");
			event.activateEvent();
			event.deactivateEvent();
		});
	}

	@Test
	public void FailedEventDeactivationAlreadyInactive() {
		try {
			Event event = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			event.deactivateEvent();
		} catch (Exception e) {
			assertEquals("Event is already inactive.", e.getMessage());
		}
	}

	@Test
	public void SuccedssfulLotteryEnrollment() {
		assertDoesNotThrow(() -> {
			Event event = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			event.activateEvent();
			LotteryPolicy lotteryPolicy = mock(LotteryPolicy.class);
			doNothing().when(lotteryPolicy).enrollInLottery(anyInt(), anyString());
			event.addEventPurchasePolicy(lotteryPolicy);
			event.enrollInLottery("user1");
		});
	}

	@Test
	public void FailedLotteryEnrollmentInactiveEvent() {
		try {
			Event event = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			LotteryPolicy lotteryPolicy = mock(LotteryPolicy.class);
			doNothing().when(lotteryPolicy).enrollInLottery(anyInt(), anyString());
			event.addEventPurchasePolicy(lotteryPolicy);
			event.enrollInLottery("user1");
		} catch (Exception e) {
			assertEquals("Can't enroll in lottery for an inactive event", e.getMessage());
		}
	}	

	@Test
	public void FailedLotteryEnrollmentNoLotteryPolicy() {
		try {
			Event event = new Event(new EventRecord("1", "name", LocalDateTime.parse("2027-10-10T10:00:00"),
					LocalDateTime.parse("2027-10-10T12:00:00"), "Artist", "Category", 1, 0, 0), "0");
			event.activateEvent();
			event.enrollInLottery("user1");
		} catch (Exception e) {
			assertEquals("Event does not have a lottery purchase policy.", e.getMessage());
		}
	}

}
