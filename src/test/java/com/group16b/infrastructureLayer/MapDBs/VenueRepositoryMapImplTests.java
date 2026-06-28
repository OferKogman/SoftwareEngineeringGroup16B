package com.group16b.infrastructureLayer.MapDBs;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Venue.VenueGrid;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;

public class VenueRepositoryMapImplTests {

    private VenueRepositoryMapImpl venueRepository;
    private ConcurrentHashMap<String, Venue> venues;
    
    private Venue mockVenue1;
    private Venue mockVenue2;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        venueRepository = new VenueRepositoryMapImpl();
        
        // grab the HashMap with reflection to manipulate state easily
        Field mapField = VenueRepositoryMapImpl.class.getDeclaredField("venues");
        mapField.setAccessible(true);
        venues = (ConcurrentHashMap<String, Venue>) mapField.get(venueRepository);

        mockVenue1 = mock(Venue.class);
        when(mockVenue1.getID()).thenReturn("venue_1");
        when(mockVenue1.getVersion()).thenReturn(1L);
        when(mockVenue1.getGrid()).thenReturn(new VenueGrid(10, 10));

        mockVenue2 = mock(Venue.class);
        when(mockVenue2.getID()).thenReturn("venue_2");
        when(mockVenue2.getVersion()).thenReturn(1L);
        when(mockVenue2.getGrid()).thenReturn(new VenueGrid(10, 10));
    }

    @Test
    void findByID_VenueExists_ReturnsVenueAndAssertsById() {
        venues.put("venue_1", mockVenue1);

        Venue result = venueRepository.findByID("venue_1");

        assertEquals(mockVenue1.getID(), result.getID());
    }

    @Test
    void findByID_VenueDoesNotExist_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> venueRepository.findByID("missing_venue"));
            
        assertTrue(ex.getMessage().contains("No venue found"));
    }

    @Test
    void getAll_MapIsEmpty_ReturnsEmptyList() {
        List<Venue> results = venueRepository.getAll();
        assertTrue(results.isEmpty());
    }

    @Test
    void getAll_VenuesExist_ReturnsListAssertsByIds() {
        venues.put("venue_1", mockVenue1);
        venues.put("venue_2", mockVenue2);

        List<Venue> results = venueRepository.getAll();

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(v -> v.getID().equals("venue_1")));
        assertTrue(results.stream().anyMatch(v -> v.getID().equals("venue_2")));
    }

    @Test
    void delete_VenueExists_RemovesFromMapSuccessfully() {
        venues.put("venue_1", mockVenue1);

        venueRepository.delete("venue_1");

        assertFalse(venues.containsKey("venue_1"));
    }

    @Test
    void delete_VenueDoesNotExist_ThrowsIllegalArgumentExceptionAndMapUntouched() {
        venues.put("venue_1", mockVenue1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> venueRepository.delete("missing_venue"));

        assertTrue(ex.getMessage().contains("No venue found to delete"));
        assertEquals(1, venues.size());
    }

    @Test
    void save_NewVenue_AddsToMapAndSetsVersionToOne() {
        when(mockVenue1.getVersion()).thenReturn(0L); 

        venueRepository.save(mockVenue1);

        assertTrue(venues.containsKey("venue_1"));
        assertEquals(1L, venues.get("venue_1").getVersion()); 
    }

    @Test
    void save_ExistingVenue_ValidVersion_UpdatesSuccessfullyAndIncrements() {
        venues.put("venue_1", mockVenue1); 
        
        Venue incomingUpdate = mock(Venue.class);
        when(incomingUpdate.getID()).thenReturn("venue_1");
        when(incomingUpdate.getVersion()).thenReturn(1L); 
        when(incomingUpdate.getGrid()).thenReturn(new VenueGrid(10, 10));

        venueRepository.save(incomingUpdate);

        assertEquals(2L, venues.get("venue_1").getVersion());
    }

    @Test
    void save_ExistingVenue_StaleVersion_ThrowsIllegalArgumentException() {
        venues.put("venue_1", mockVenue1); 
        
        Venue staleUpdate = mock(Venue.class);
        when(staleUpdate.getID()).thenReturn("venue_1");
        when(staleUpdate.getVersion()).thenReturn(0L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> venueRepository.save(staleUpdate));
        
        assertTrue(ex.getMessage().contains("Version mismatch"));
        assertEquals(1L, venues.get("venue_1").getVersion()); // state wasn't changed
    }

    @Test
    void save_ConcurrentWritesToSameVenue_OptimisticLockPreventsOverwrites() throws InterruptedException {
        // Setup: Pre-load the map with a real venue (Version 1)
        Venue realVenue = new Venue("venue_1", null, new ConcurrentHashMap<>(), "venueID", new VenueGrid(6, 7), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(),1);
        realVenue.setVersion(1L);
        venues.put("venue_1", realVenue);
        
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    //all threads attempt to save an update claiming to be version 1
                    Venue concurrentMock = mock(Venue.class);
                    when(concurrentMock.getID()).thenReturn("venue_1");
                    when(concurrentMock.getVersion()).thenReturn(1L); 
                    when(concurrentMock.getGrid()).thenReturn(new VenueGrid(10, 10));
                     
                    venueRepository.save(concurrentMock); 
                } catch (IllegalArgumentException e) {
                    if (e.getMessage().contains("Version mismatch")) {
                        failureCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        //exactly 1 thread will read v1 and update it to v2
        //the other 9 threads will read v2 realize their incoming v1 is stale, and fail.
        assertEquals(9, failureCount.get(), "9 threads should have failed due to Version mismatch.");
        assertEquals(2L, venues.get("venue_1").getVersion(), "The venue should have only updated exactly once.");
    }
}