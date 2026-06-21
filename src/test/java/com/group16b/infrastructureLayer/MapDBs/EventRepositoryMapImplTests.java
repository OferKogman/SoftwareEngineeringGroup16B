package com.group16b.infrastructureLayer.MapDBs;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;

public class EventRepositoryMapImplTests {
	EventRepositoryMapImpl repository;
	Event matchingEvent, wrongName, wrongArtist, wrongCategory, tooCheap, tooExpensive, tooEarly, tooLate, lowRating, wrongProductionCompany, testEvent;
	LocalDateTime now = LocalDateTime.now().plusDays(1);

	private final String testEventName = "Test Event";

	@BeforeEach
	void setup() {
		matchingEvent = new Event(new EventRecord("-1", "Rock Night", now.plusDays(1), now.plusDays(2), "Queen", "Music", 1, 4.0), "owner1");
		matchingEvent.setEventPrice(100.0);

		wrongName = new Event(new EventRecord("-1", "Jazz Night", now.plusDays(1), now.plusDays(2), "Queen", "Music", 1, 4.0), "owner1");
		wrongName.setEventPrice(100.0);

		wrongArtist = new Event(new EventRecord("-1", "Rock Night", now.plusDays(1), now.plusDays(2), "ABBA", "Music", 1, 4.0), "owner1");
		wrongArtist.setEventPrice(100.0);

		wrongCategory = new Event(new EventRecord("-1", "Rock Night", now.plusDays(1), now.plusDays(2), "Queen", "Theater", 1, 4.0), "owner1");
		wrongCategory.setEventPrice(100.0);

		tooCheap = new Event(new EventRecord("-1", "Rock Night", now.plusDays(1), now.plusDays(2), "Queen", "Music", 1, 4.0), "owner1");
		tooCheap.setEventPrice(20.0);

		tooExpensive = new Event(new EventRecord("-1", "Rock Night", now.plusDays(1), now.plusDays(2), "Queen", "Music", 1, 4.0), "owner1");
		tooExpensive.setEventPrice(500.0);

		tooEarly = new Event(new EventRecord("-1", "Rock Night", now.minusDays(2), now, "Queen", "Music", 1, 4.0), "owner1");
		tooEarly.setEventPrice(100.0);

		tooLate = new Event(new EventRecord("-1", "Rock Night", now.plusDays(10), now.plusDays(12), "Queen", "Music", 1, 4.0), "owner1");
		tooLate.setEventPrice(100.0);

		lowRating = new Event(new EventRecord("-1", "Rock Night", now.plusDays(1), now.plusDays(2), "Queen", "Music", 1, 2.0), "owner1");
		lowRating.setEventPrice(100.0);

		wrongProductionCompany = new Event(new EventRecord("-1", "Rock Night", now.plusDays(1), now.plusDays(2), "Queen", "Music", 2, 4.0), "owner2");
		wrongProductionCompany.setEventPrice(100.0);

		List<Event> allEvents = List.of(matchingEvent, wrongName, wrongArtist, wrongCategory, tooCheap, tooExpensive, tooEarly, tooLate, lowRating, wrongProductionCompany);
		repository = new EventRepositoryMapImpl();
		allEvents.forEach(repository::save);

		testEvent = new Event(new EventRecord("-1", "Test Event", now.plusDays(3), now.plusDays(4), "Test Artist", "Test Category", 1, 5.0), "owner1");
		testEvent.setEventPrice(150.0);
	}

	@Test
	void successfulGetEventById() {
		Event retrievedEvent = repository.findByID(String.valueOf(matchingEvent.getEventID()));
		assertEquals(matchingEvent.getEventID(), retrievedEvent.getEventID());
	}

	@Test
	void FailureGetEventByIdNotFound() {
		try {
			repository.findByID("999");
		} catch (Exception e) {
			assertEquals("Event with ID 999 not found", e.getMessage());
		}
	}

	@Test
    void findByID_invalidFormat_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findByID("abc")
        );
    }

    @Test
    void findByID_returnsDefensiveCopy() {
        repository.save(testEvent);
        Event found = repository.findByID(String.valueOf(testEvent.getEventID()));
        found.setEventName("HACKED");
        Event actual = repository.findByID(String.valueOf(testEvent.getEventID()));
        assertEquals("Test Event", actual.getEventName());
    }

	@Test
	void FailureSearchEventsMinPriceMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, List.of(10.0, 20.0), null, null, null, null, null);
		});

		assertEquals("Min price filter must have exactly one value.", exception.getMessage());
	}

	@Test
	void FailureSearchEventsMaxPriceMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, null, List.of(10.0, 20.0), null, null, null, null);
		});

		assertEquals("Max price filter must have exactly one value.", exception.getMessage());
	}

	@Test
	void FailureSearchEventsStartTimeMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, null, null, List.of(LocalDateTime.now(), LocalDateTime.now().plusDays(1)), null, null, null);
		});

		assertEquals("Start time filter must have exactly one value.", exception.getMessage());
	}

	@Test
	void FailureSearchEventsEndTimeMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, null, null, null, List.of(LocalDateTime.now(), LocalDateTime.now().plusDays(1)), null, null);
		});

		assertEquals("End time filter must have exactly one value.", exception.getMessage());
	}

	@Test
	void FailureSearchEventsEventRatingMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, null, null, null, null, List.of(3.0, 4.0), null);
		});

		assertEquals("Event rating filter must have exactly one value.", exception.getMessage());
	}

	@Test
    void getAll_returnsAllCompanies() {
        List<Event> events = repository.getAll();
        assertEquals(10, events.size());
    }

	@Test
    void getAll_returnsCopies_notOriginalReferences() {
        List<Event> events = repository.getAll();
        events.get(0).setEventName("HACKED");
        Event fresh = repository.findByID(String.valueOf(events.get(0).getEventID()));
        assertNotEquals("HACKED", fresh.getEventName());
    }

	@Test
	void filterEvents_success() {
		List<Event> result = repository.searchEvents(
				List.of("Rock Night"),
				List.of("Queen"),
				List.of("Music"),
				null,
				List.of(50.0),
				List.of(200.0),
				List.of(now.plusDays(1)),
				List.of(now.plusDays(2)),
				List.of(4.0),
				List.of(1)
		);
		assertEquals(1, result.size());
		assertEquals(result.get(0).getEventID() ,matchingEvent.getEventID());
		result.get(0).setEventName("HACKED");
		Event retrievedEvent = repository.findByID(String.valueOf(matchingEvent.getEventID()));
		assertEquals("Rock Night", retrievedEvent.getEventName());
	}

	@Test
    void save_newEvent_success() {
		repository.save(testEvent);
		Event result = repository.findByID(String.valueOf(testEvent.getEventID()));
		assertAll(
				() -> assertEquals(testEvent.getEventID(), result.getEventID()),
                () -> assertEquals(testEventName, result.getEventName()),
                () -> assertEquals(1, result.getEventProductionCompanyID()),
                () -> assertEquals(0, result.getVersion())
        );
    }

	@Test
    void save_storesDefensiveCopy() {

        Event original =spy(testEvent);

        repository.save(original);

        original.setEventName("HACKED");
        original.incrementVersion();

        Event stored = repository.findByID(String.valueOf(testEvent.getEventID()));

        assertAll(
                () -> assertEquals("Test Event", stored.getEventName()),
                () -> assertEquals(0, stored.getVersion()),
                () -> assertNotSame(original, stored)
        );
    }

	@Test
    void save_nullEvent_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );

        assertTrue(ex.getMessage().contains("Event cannot be null"));
    }

	@Test
    void save_update_success_incrementsVersion() {
        repository.save(testEvent);
        Event updated = repository.findByID(String.valueOf(testEvent.getEventID()));
        updated.setEventName("UPDATED");
        repository.save(updated);
        Event result = repository.findByID(String.valueOf(testEvent.getEventID()));
        assertAll(
                () -> assertEquals("UPDATED", result.getEventName()),
                () -> assertEquals(1, result.getVersion())
        );
    }

	@Test
    void save_update_onlyStoredCopyChangesVersion() {
        repository.save(testEvent);
        Event detached = spy(repository.findByID(String.valueOf(testEvent.getEventID())));
        long oldVersion = detached.getVersion();
        repository.save(detached);
        // detached object should NOT be mutated
        assertEquals(oldVersion, detached.getVersion());
        verify(detached, never()).incrementVersion();
        Event stored = repository.findByID(String.valueOf(testEvent.getEventID()));
        assertEquals(oldVersion + 1, stored.getVersion());
    }

	@Test
    void save_update_returnsDefensiveCopy() {
        repository.save(testEvent);
        Event detached = repository.findByID(String.valueOf(testEvent.getEventID()));
        repository.save(detached);
        Event stored = repository.findByID(String.valueOf(testEvent.getEventID()));
        assertNotSame(detached, stored);
    }

	@Test
    void save_update_versionMismatch_repositoryStateUnchanged() {
        repository.save(testEvent);
        Event stale = repository.findByID(String.valueOf(testEvent.getEventID()));
        stale.incrementVersion();
        stale.setEventName("HACKED");
        assertThrows(
                OptimisticLockingFailureException.class,
                () -> repository.save(stale)
        );
        Event actual = repository.findByID(String.valueOf(testEvent.getEventID()));
        assertAll(
                () -> assertEquals("Test Event", actual.getEventName()),
                () -> assertEquals(0, actual.getVersion())
        );
    }

	@Test
    void save_failedUpdate_doesNotMutateCallerObject() {
        repository.save(testEvent);
        Event stale =spy(repository.findByID(String.valueOf(testEvent.getEventID())));
        stale.incrementVersion();
        assertThrows(
                OptimisticLockingFailureException.class,
                () -> repository.save(stale)
        );
        verify(stale, times(1)).incrementVersion();
        assertEquals(1, stale.getVersion());
    }



	@Test
    void concurrentUpdates_onlyOneSucceeds() throws Exception {
        repository.save(testEvent);
        Event copy1 = repository.findByID(String.valueOf(testEvent.getEventID()));
        Event copy2 = repository.findByID(String.valueOf(testEvent.getEventID()));

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        ExecutorService executor =Executors.newFixedThreadPool(2);

        CountDownLatch startLatch = new CountDownLatch(1);

        Runnable task1 = () -> {
            try {
                startLatch.await();

                copy1.setEventName("Update1");

                repository.save(copy1);

                successCount.incrementAndGet();

            } catch (OptimisticLockingFailureException e) {

                failureCount.incrementAndGet();

            } catch (Exception ignored) {
            }
        };

        Runnable task2 = () -> {
            try {
                startLatch.await();

                copy2.setEventName("Update2");

                repository.save(copy2);

                successCount.incrementAndGet();

            } catch (OptimisticLockingFailureException e) {

                failureCount.incrementAndGet();

            } catch (Exception ignored) {
            }
        };

        executor.submit(task1);
        executor.submit(task2);

        startLatch.countDown();

        executor.shutdown();

        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        Event finalState = repository.findByID(String.valueOf(testEvent.getEventID()));

        assertEquals(1, finalState.getVersion());
    }
}