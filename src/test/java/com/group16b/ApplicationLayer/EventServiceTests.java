package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.GridRectangle;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Venue.VenueGrid;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.Security.Role;

public class EventServiceTests {

        private EventService eventService;
        private IAuthenticationService mockTokenService;
        private ILocationService mockLocationService;
        private EventFilteringService eventFilteringService;
        private IProductionCompanyRepository productionCompanyRepository;
        private IRepository<User> userRepository;
        private IRepository<Venue> venueRepository;
        private IEventRepository eventRepository;
        private IRepository<VirtualQueue> virtualQueueRepository;
        private User user1;
        private User user2;
        private Event e1;
        private Location location1;
        private Segment segment1;
        private ProductionCompany company1;

        @BeforeEach
        public void setUp()
                        throws NoSuchFieldException, SecurityException, IllegalArgumentException,
                        IllegalAccessException {
                mockTokenService = mock(IAuthenticationService.class);
                mockLocationService = mock(ILocationService.class);
                productionCompanyRepository = new ProductionCompanyRepositoryMapImpl();
                userRepository = new UserRepositoryMapImpl();
                venueRepository = new VenueRepositoryMapImpl();
                eventRepository = new EventRepositoryMapImpl();
                virtualQueueRepository = new VirtualQueueRepositoryMapImpl();
                eventFilteringService = new EventFilteringService(productionCompanyRepository, eventRepository,
                                venueRepository);
                eventService = new EventService(mockTokenService, mockLocationService, eventFilteringService,
                                productionCompanyRepository, virtualQueueRepository, venueRepository, eventRepository,
                                userRepository);

                when(mockTokenService.validateToken("invalid_token")).thenReturn(false);

                user1 = new User("testuser1", "password");
                when(mockTokenService.validateToken("user1")).thenReturn(true);
                when(mockTokenService.extractRoleFromToken("user1")).thenReturn(Role.SIGNED);
                when(mockTokenService.isUserToken("user1")).thenReturn(true);
                when(mockTokenService.extractSubjectFromToken("user1")).thenReturn(String.valueOf(user1.getEmail()));

                user2 = new User("testuser2", "password");
                when(mockTokenService.validateToken("user2")).thenReturn(true);
                when(mockTokenService.extractRoleFromToken("user2")).thenReturn(Role.SIGNED);
                when(mockTokenService.isUserToken("user2")).thenReturn(true);
                when(mockTokenService.extractSubjectFromToken("user2")).thenReturn(String.valueOf(user2.getEmail()));

                userRepository.save(user1);
                userRepository.save(user2);

                company1 = new ProductionCompany(1, "Sony", 4.2, "testuser1");
                productionCompanyRepository.save(company1);

                location1 = new Location("location1", "1", "street", "city", "state", "country", 0.00, 0.00);

                segment1 = new FieldSeg("segment1", 50, new GridRectangle(0, 0, 10, 5));
                Map<String, Segment> segmentMap = new TreeMap<>();
                segmentMap.put("segment1", segment1);

                Venue venue1 = new Venue("Test Venue", location1, segmentMap, "venue1", new VenueGrid(10, 10),
                                new TreeMap<>(), new TreeMap<>(),1);

                LocalDateTime startTime = LocalDateTime.now().plusDays(1);
                LocalDateTime endTime = LocalDateTime.now().plusDays(2);

                e1 = new Event(new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 1, 5.0,
                                3.5), user1.getEmail());
                eventRepository.save(e1);
                venue1.bookEvent(e1.getEventStartTime(), e1.getEventEndTime(), 1);
                venueRepository.save(venue1);
        }

        @Test // 2.2.1.ii //2.2
        public void ReceiveEventInfo_Success() {
                assertEquals(new EventDTO(e1), eventService.viewEvent(e1.getEventID()).getValue());
        }

        @Test
        public void ReceiveEventInfo_EventNotExists_Failure() {
                assertEquals("Event with ID 500 not found", eventService.viewEvent(500).getError());
        }

        @Test // 2.2.3.i
        public void EditEvents_Success() {
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("name", "Updated Event");
                assertEquals(new EventDTO(e1), eventService.editEvent(editParams, e1.getEventID(), "user1").getValue());
                assertEquals("Updated Event", eventRepository.findByID(String.valueOf(e1.getEventID())).getEventName());
        }

        @Test
        public void EditEvents_InvalidParameterName_Failure() {
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("name", 123);
                assertEquals("Invalid type for parameter 'name'. Expected: String",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals("event1", eventRepository.findByID(String.valueOf(e1.getEventID())).getEventName());
        }

        @Test
        public void EditEvents_InvalidParameterArtist_Failure() {
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("artist", 123);
                assertEquals("Invalid type for parameter 'artist'. Expected: String",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals("artist1", eventRepository.findByID(String.valueOf(e1.getEventID())).getEventArtist());
        }

        @Test
        public void EditEvents_InvalidParameterCategory_Failure() {
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("category", 123);
                assertEquals("Invalid type for parameter 'category'. Expected: String",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals("category1", eventRepository.findByID(String.valueOf(e1.getEventID())).getEventCategory());
        }

        @Test
        public void EditEvents_InvalidParameterStartTime_Failure() {
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("startTime", "not_a_date");
                editParams.put("endTime", LocalDateTime.now().plusDays(1));
                assertEquals("Invalid type for parameter 'startTime'. Expected: LocalDateTime",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals(e1.getEventStartTime(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStartTime());
                assertEquals(e1.getEventEndTime(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventEndTime());
        }

        @Test
        public void SearchEvents_InvalidParameterOnlyStartTime_Failure() {
                LocalDateTime now = LocalDateTime.now();
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("startTime", now.plusDays(1));
                assertEquals("Must edit both start and end time together to update event time !",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals(e1.getEventStartTime(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStartTime());
        }

        @Test
        public void EditEvents_InvalidParameterEndTime_Failure() {
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("startTime", LocalDateTime.now());
                editParams.put("endTime", "not_a_date");
                assertEquals("Invalid type for parameter 'endTime'. Expected: LocalDateTime",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals(e1.getEventEndTime(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventEndTime());
                assertEquals(e1.getEventStartTime(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStartTime());
        }

        @Test
        public void SearchEvents_InvalidParameterOnlyEndTime_Failure() {
                LocalDateTime now = LocalDateTime.now();
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("endTime", now.plusDays(1));
                assertEquals("Must edit both start and end time together to update event time !",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals(e1.getEventEndTime(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventEndTime());
        }

        @Test
        public void EditEvents_InvalidParameterEventRating_Failure() {
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("eventRating", "not_a_double");
                assertEquals("Invalid type for parameter 'eventRating'. Expected: Double",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals(e1.getEventRating(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventRating());
        }

        @Test
        public void EditEvents_InvalidParameterVenue_Failure() {
                Map<String, Object> editParams = new TreeMap<>();
                editParams.put("venue", 123);
                assertEquals("Invalid type for parameter 'venue'. Expected: String",
                                eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
                assertEquals(e1.getEventVenueID(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventVenueID());
        }

        @Test // 2.2.3.i
        public void SearchEvents_Success() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("name", List.of("empty"));
                assertEquals(new ArrayList<EventDTO>(), eventService.searchEvents(searchParams).getValue());
        }

        @Test
        public void SearchEvents_InvalidParameterName_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("name", List.of(123));
                assertEquals("Invalid search parameters: Invalid type for parameter 'name'. Expected: String",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterArtist_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("artist", List.of(123));
                assertEquals("Invalid search parameters: Invalid type for parameter 'artist'. Expected: String",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterCategory_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("category", List.of(123));
                assertEquals("Invalid search parameters: Invalid type for parameter 'category'. Expected: String",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterKeyword_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("keyword", List.of(123));
                assertEquals("Invalid search parameters: Invalid type for parameter 'keyword'. Expected: String",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterMinPrice_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("minPrice", List.of("not_a_number"));
                assertEquals("Invalid search parameters: Invalid type for parameter 'minPrice'. Expected: Number",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterMinPriceMoreThanOne_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("minPrice", List.of(123.45, 67.89));
                assertEquals("Invalid search parameters: Min price filter must have exactly one value.",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterMaxPrice_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("maxPrice", List.of("not_a_number"));
                assertEquals("Invalid search parameters: Invalid type for parameter 'maxPrice'. Expected: Number",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterMaxPriceMoreThanOne_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("maxPrice", List.of(123.45, 67.89));
                assertEquals("Invalid search parameters: Max price filter must have exactly one value.",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterStartTime_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("startTime", List.of("not_a_date"));
                assertEquals("Invalid search parameters: Invalid type for parameter 'startTime'. Expected: LocalDateTime",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterStartTimeMoreThanOne_Failure() {
                LocalDateTime now = LocalDateTime.now();
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("startTime", List.of(now, now.plusDays(1)));
                assertEquals("Invalid search parameters: Start time filter must have exactly one value.",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterEndTime_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("endTime", List.of("not_a_date"));
                assertEquals("Invalid search parameters: Invalid type for parameter 'endTime'. Expected: LocalDateTime",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterEndTimeMoreThanOne_Failure() {
                LocalDateTime now = LocalDateTime.now();
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("endTime", List.of(now, now.plusDays(1)));
                assertEquals("Invalid search parameters: End time filter must have exactly one value.",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterEventRating_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("eventRating", List.of("not_a_double"));
                assertEquals("Invalid search parameters: Invalid type for parameter 'eventRating'. Expected: Number",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterEventRatingMoreThanOne_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("eventRating", List.of(123.45, 67.89));
                assertEquals("Invalid search parameters: Event rating filter must have exactly one value.",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterPCRating_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("productionCompanyRating", List.of("not_a_double"));
                assertEquals(
                                "Invalid search parameters: Invalid type for parameter 'productionCompanyRating'. Expected: Number",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterPCRatingMoreThanOne_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("productionCompanyRating", List.of(123.45, 67.89));
                assertEquals("Invalid search parameters: Production company rating filter must have exactly one value.",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterPCID_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("productionCompany", List.of(123));
                assertEquals("Invalid search parameters: Invalid type for parameter 'productionCompany'. Expected: String",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test
        public void SearchEvents_InvalidParameterLocation_Failure() {
                Map<String, List<Object>> searchParams = new TreeMap<>();
                searchParams.put("location", List.of(123));
                assertEquals("Invalid search parameters: Invalid type for parameter 'location'. Expected: String",
                                eventService.searchEvents(searchParams).getError());
        }

        @Test // 2.4.1.1
        public void CreateEvent_Success()
                        throws NoSuchFieldException, SecurityException, IllegalArgumentException,
                        IllegalAccessException {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now.plusDays(6), now.plusDays(7), "artist2",
                                "category2", 1, 67.0, 4.5);
                EventDTO e2 = new EventDTO(new Event(record, user1.getEmail()));
                assertEquals(e2.getEventID() + 1, eventService.createEvent(record, "user1").getValue().getEventID());
        }

        @Test
        public void CreateEvent_UserIsNotAuthenticated_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2",
                                1, 67.0, 4.5);
                assertEquals("Authentication failed: Invalid session token.",
                                eventService.createEvent(record, "invalid_token").getError());
        }

        @Test
        public void CreateEvent_CompanyNotFound_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2",
                                999, 67.0, 4.5);
                assertEquals("Production company with ID 999 is not found.",
                                eventService.createEvent(record, "user1").getError());
        }

        @Test
        public void CreateEvent_UserIsNotOwner_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2",
                                1, 67.0, 4.5);
                assertEquals("user testuser2 dont have high enough permissions in company 1",
                                eventService.createEvent(record, "user2").getError());
        }

        @Test
        public void CreateEvent_InvalidName_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "", now, now.plusDays(1), "artist2", "category2", 1,
                                67.0, 4.5);
                assertEquals("Event name cannot be null or empty.",
                                eventService.createEvent(record, "user1").getError());
                record = new EventRecord("venue1", null, now, now.plusDays(1), "artist2", "category2", 1, 67.0, 4.5);
                assertEquals("Event name cannot be null or empty.",
                                eventService.createEvent(record, "user1").getError());
        }

        @Test
        public void CreateEvent_InvalidDates_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now.minusDays(2), now.minusDays(1), "artist2",
                                "category2", 1, 67.0, 4.5);
                assertEquals("End time must be in the future.", eventService.createEvent(record, "user1").getError());
                record = new EventRecord("venue1", "event2", now.plusDays(2), now.plusDays(1), "artist2", "category2",
                                1, 67.0,
                                4.5);
                assertEquals("Start time must be before end time.",
                                eventService.createEvent(record, "user1").getError());
                record = new EventRecord("venue1", "event2", null, now.plusDays(1), "artist2", "category2", 1, 67.0,
                                4.5);
                assertEquals("Start time and end time cannot be null.",
                                eventService.createEvent(record, "user1").getError());
                record = new EventRecord("venue1", "event2", now.plusDays(1), null, "artist2", "category2", 1, 67.0,
                                4.5);
                assertEquals("Start time and end time cannot be null.",
                                eventService.createEvent(record, "user1").getError());
        }

        @Test
        public void CreateEvent_InvalidArtist_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "", "category2", 1, 67.0,
                                4.5);
                assertEquals("Event artist cannot be null or empty.",
                                eventService.createEvent(record, "user1").getError());
                record = new EventRecord("venue1", "event2", now, now.plusDays(1), null, "category2", 1, 67.0, 4.5);
                assertEquals("Event artist cannot be null or empty.",
                                eventService.createEvent(record, "user1").getError());
        }

        @Test
        public void CreateEvent_InvalidCategory_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "", 1, 67.0,
                                4.5);
                assertEquals("Event category cannot be null or empty.",
                                eventService.createEvent(record, "user1").getError());
                record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", null, 1, 67.0, 4.5);
                assertEquals("Event category cannot be null or empty.",
                                eventService.createEvent(record, "user1").getError());
        }

        @Test
        public void CreateEvent_InvalidPrice_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2",
                                1, -1.0,
                                4.5);
                assertEquals("Event price cannot be negative.", eventService.createEvent(record, "user1").getError());
        }

        @Test
        public void CreateEvent_InvalidRating_Failure() {
                LocalDateTime now = LocalDateTime.now();
                EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2",
                                1, 67.0,
                                -1.0);
                assertEquals("Event rating must be between 0 and 5.",
                                eventService.createEvent(record, "user1").getError());
                record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2", 1, 67.0,
                                6.0);
                assertEquals("Event rating must be between 0 and 5.",
                                eventService.createEvent(record, "user1").getError());
        }

        @Test
        public void CreateEvent_VenueIsUnavailable_Failure() {
                EventRecord record = new EventRecord("venue1", "event2", e1.getEventStartTime(), e1.getEventEndTime(),
                                "artist2", "category2", 1, 67.0, 4.5);
                Result<EventDTO> result = eventService.createEvent(record, "user1");
                assertFalse(result.isSuccess());
                assertTrue(result.getError().startsWith("Venue is already booked during this time frame!"));
        }

        // update event //2.4.1.2

        @Test // 2.4.1.3
        public void DeactivateEvent_Success() {
                Event event = eventRepository.findByID(String.valueOf(e1.getEventID()));
                event.activateEvent();
                eventRepository.save(event);
                assertEquals(true, eventService.deactivateEvent(e1.getEventID(), "user1").getValue());
        }

        @Test
        public void DeactivateEvent_TwoThreadsOneSucceeds() throws InterruptedException {
                Event event = eventRepository.findByID(String.valueOf(e1.getEventID()));
                event.activateEvent();
                eventRepository.save(event);

                CountDownLatch readyLatch = new CountDownLatch(2);
                CountDownLatch startLatch = new CountDownLatch(1);

                AtomicReference<Result<Boolean>> result1 = new AtomicReference<>();
                AtomicReference<Result<Boolean>> result2 = new AtomicReference<>();

                Runnable deactivateEventTask1 = () -> {
                        try {
                                readyLatch.countDown();
                                startLatch.await();
                                result1.set(eventService.deactivateEvent(e1.getEventID(), "user1"));
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                        }
                };

                Runnable deactivateEventTask2 = () -> {
                        try {
                                readyLatch.countDown();
                                startLatch.await();
                                result2.set(eventService.deactivateEvent(e1.getEventID(), "user2"));
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                        }
                };

                Thread thread1 = new Thread(deactivateEventTask1);
                Thread thread2 = new Thread(deactivateEventTask2);

                thread1.start();
                thread2.start();

                readyLatch.await();
                startLatch.countDown();

                thread1.join();
                thread2.join();

                int successCount = 0;
                if (result1.get() != null && result1.get().isSuccess()) {
                        successCount++;
                }
                if (result2.get() != null && result2.get().isSuccess()) {
                        successCount++;
                }

                assertTrue(successCount == 1);

                Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
                assertDoesNotThrow(() -> e.getEventStatus());
        }

        @Test
        public void DeactivateEvent_EventNotFound_Failure() {
                assertEquals("Event with ID 500 not found", eventService.deactivateEvent(500, "user1").getError());
        }

        @Test
        public void DeactivateEvent_UserIsNotAuthenticated_Failure() {
                assertEquals("Authentication failed: Invalid session token.",
                                eventService.deactivateEvent(e1.getEventID(), "invalid_token").getError());
                assertEquals(e1.getEventStatus(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStatus());
        }

        @Test
        public void DeactivateEvent_UserIsNotOwner_Failure() {
                assertEquals("user testuser2 dont have high enough permissions in company 1",
                                eventService.deactivateEvent(e1.getEventID(), "user2").getError());
                assertEquals(e1.getEventStatus(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStatus());
        }

        @Test
        public void DeactivateEvent_EventAlreadyDeactivated_Failure() {
                assertEquals("Event is already inactive.",
                                eventService.deactivateEvent(e1.getEventID(), "user1").getError());
                assertEquals(e1.getEventStatus(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStatus());
        }

        @Test // 2.4.1.3
        public void ActivateEvent_Success() {
                assertEquals(true, eventService.activateEvent(e1.getEventID(), "user1").getValue());
        }

        @Test
        public void ActivateEvent_TwoThreadsOneSucceeds() throws InterruptedException {
                CountDownLatch readyLatch = new CountDownLatch(2);
                CountDownLatch startLatch = new CountDownLatch(1);

                AtomicReference<Result<Boolean>> result1 = new AtomicReference<>();
                AtomicReference<Result<Boolean>> result2 = new AtomicReference<>();

                Runnable activateEventTask1 = () -> {
                        try {
                                readyLatch.countDown();
                                startLatch.await();
                                result1.set(eventService.activateEvent(e1.getEventID(), "user1"));
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                        }
                };

                Runnable activateEventTask2 = () -> {
                        try {
                                readyLatch.countDown();
                                startLatch.await();
                                result2.set(eventService.activateEvent(e1.getEventID(), "user2"));
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                        }
                };

                Thread thread1 = new Thread(activateEventTask1);
                Thread thread2 = new Thread(activateEventTask2);

                thread1.start();
                thread2.start();

                readyLatch.await();
                startLatch.countDown();

                thread1.join();
                thread2.join();

                int successCount = 0;
                if (result1.get() != null && result1.get().isSuccess()) {
                        successCount++;
                }
                if (result2.get() != null && result2.get().isSuccess()) {
                        successCount++;
                }

                assertTrue(successCount == 1);

                Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
                assertDoesNotThrow(() -> e.getEventStatus());
        }

        @Test
        public void ActivateEvent_EventNotFound_Failure() {
                assertEquals("Event with ID 500 not found", eventService.activateEvent(500, "user1").getError());
        }

        @Test
        public void ActivateEvent_UserIsNotAuthenticated_Failure() {
                assertEquals("Authentication failed: Invalid session token.",
                                eventService.activateEvent(e1.getEventID(), "invalid_token").getError());
                assertEquals(e1.getEventStatus(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStatus());
        }

        @Test
        public void ActivateEvent_UserIsNotOwner_Failure() {
                assertEquals("user testuser2 dont have high enough permissions in company 1",
                                eventService.activateEvent(e1.getEventID(), "user2").getError());
                assertEquals(e1.getEventStatus(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStatus());
        }

        @Test
        public void ActivateEvent_EventAlreadyActive_Failure() {
                Event event = eventRepository.findByID(String.valueOf(e1.getEventID()));
                event.activateEvent();
                eventRepository.save(event);
                assertEquals("Event is already active.",
                                eventService.activateEvent(e1.getEventID(), "user1").getError());
                assertEquals(event.getEventStatus(),
                                eventRepository.findByID(String.valueOf(e1.getEventID())).getEventStatus());
        }
        @Test
        public void AddEventPrices_Success() {
                Map<String, Double> prices = new TreeMap<>();
                prices.put("segment1", 120.0);

                Result<String> result = eventService.addEventPrices(e1.getEventID(), prices, "user1");

                assertTrue(result.isSuccess());
                assertEquals("Prices added successfully.", result.getValue());

                Venue venue = venueRepository.findByID(e1.getEventVenueID());
                assertEquals(120.0, venue.getPriceForSegment("segment1", e1.getEventID()), 0.001);
        }

        @Test
        public void AddEventPrices_UserIsNotAuthenticated_Failure() {
                Map<String, Double> prices = new TreeMap<>();
                prices.put("segment1", 120.0);

                assertEquals("Authentication failed: Invalid session token.",
                                eventService.addEventPrices(e1.getEventID(), prices, "invalid_token").getError());
        }

        @Test
        public void AddEventPrices_UserDoesNotHavePermission_Failure() {
                Map<String, Double> prices = new TreeMap<>();
                prices.put("segment1", 120.0);

                assertEquals("user testuser2 dont have correct permissions in company 1",
                                eventService.addEventPrices(e1.getEventID(), prices, "user2").getError());
        }

        @Test
        public void AddEventPrices_InvalidSegment_Failure() {
                Map<String, Double> prices = new TreeMap<>();
                prices.put("segment_does_not_exist", 120.0);

                Result<String> result = eventService.addEventPrices(e1.getEventID(), prices, "user1");

                assertFalse(result.isSuccess());
                assertEquals("Segment with ID segment_does_not_exist not found", result.getError());
        }

        @Test
        public void AddEventPrices_NegativePrice_Failure() {
                Map<String, Double> prices = new TreeMap<>();
                prices.put("segment1", -10.0);

                Result<String> result = eventService.addEventPrices(e1.getEventID(), prices, "user1");

                assertFalse(result.isSuccess());
        }

}
