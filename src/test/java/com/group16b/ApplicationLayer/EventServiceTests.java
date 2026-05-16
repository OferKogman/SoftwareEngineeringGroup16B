package com.group16b.ApplicationLayer;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocatoinService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.ProductionCompanyPolicy.IProductionCompanyPolicyRepository;
import com.group16b.DomainLayer.ProductionCompanyPolicy.ProductionCompanyPolicy;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Founder;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.IVirtualQueueRepository;

public class EventServiceTests {

    static private EventService eventService;
    static private IAuthenticationService mockTokenService;
    static private ILocatoinService mockLocationService;
    static private EventFilteringService eventFilteringService;
    static private IProductionCompanyPolicyRepository mockProductionCompanyPolicyRepository;
    static private IUserRepository mockUserRepository;
    static private IVenueRepository mockVenueRepository;
    static private IEventRepository mockEventRepository;
    static private IVirtualQueueRepository mockVirtualQueueRepository;
    static private User user;
    static private User user2;
    static private Event e1;
    static private Location location1;
    static private Segment segment1;
    
    @BeforeAll
    static public void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        mockTokenService = mock(IAuthenticationService.class);
        mockLocationService = mock(ILocatoinService.class);
        eventFilteringService = new EventFilteringService();
        mockProductionCompanyPolicyRepository = mock(IProductionCompanyPolicyRepository.class);
        mockVirtualQueueRepository = mock(IVirtualQueueRepository.class);
        mockEventRepository = mock(IEventRepository.class);
        mockVenueRepository = mock(IVenueRepository.class);
        mockUserRepository = mock(IUserRepository.class);

        eventService = new EventService(mockTokenService, mockLocationService, eventFilteringService);

        Field PCPR = eventService.getClass().getDeclaredField("productionCompanyPolicyRepository");
        PCPR.setAccessible(true);
        PCPR.set(eventService, mockProductionCompanyPolicyRepository);

        Field PCPR2 = eventFilteringService.getClass().getDeclaredField("productionCompanyPolicyRepository");
        PCPR2.setAccessible(true);
        PCPR2.set(eventFilteringService, mockProductionCompanyPolicyRepository);

        Field VPR = eventService.getClass().getDeclaredField("queueRepository");
        VPR.setAccessible(true);
        VPR.set(eventService, mockVirtualQueueRepository);

        Field ER = eventService.getClass().getDeclaredField("eventRepository");
        ER.setAccessible(true);
        ER.set(eventService, mockEventRepository);

        Field ER2 = eventFilteringService.getClass().getDeclaredField("eventRepository");
        ER2.setAccessible(true);
        ER2.set(eventFilteringService, mockEventRepository);

        Field UPR = eventService.getClass().getDeclaredField("userRepository");
        UPR.setAccessible(true);
        UPR.set(eventService, mockUserRepository);

        Field VR = eventService.getClass().getDeclaredField("venueRepository");
        VR.setAccessible(true);
        VR.set(eventService, mockVenueRepository);

        Field VR2 = eventFilteringService.getClass().getDeclaredField("venueRepository");
        VR2.setAccessible(true);
        VR2.set(eventFilteringService, mockVenueRepository);

        when(mockProductionCompanyPolicyRepository.getProductionCompanyByID(1)).thenReturn(new ProductionCompanyPolicy());
        when(mockTokenService.validateToken("invalid_token")).thenReturn(false);

        user = new User("testuser", "password");
        when(mockUserRepository.getUserByEmail(user.getEmail())).thenReturn(user);
        when(mockTokenService.validateToken("user1")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user1")).thenReturn("Signed");
        when(mockTokenService.isUserToken("user1")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user1")).thenReturn(String.valueOf(user.getUserID()));
        
        user.addRole(1, new Founder(user.getUserID()));

        user2 = new User("testuser2", "password");
        when(mockUserRepository.getUserByEmail(user2.getEmail())).thenReturn(user2);
        when(mockTokenService.validateToken("user2")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user2")).thenReturn("Signed");
        when(mockTokenService.isUserToken("user2")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user2")).thenReturn(String.valueOf(user2.getUserID()));

        when(mockEventRepository.getEventByID(500)).thenThrow(new IllegalArgumentException("Event with ID 500 not found"));

        location1 = new Location("location1", "1", "street", "city", "state", "country", 0.00, 0.00);
        
        segment1 = new FieldSeg("segment1", 50);
        Map<String, Segment> segmentMap = new TreeMap<>();
        segmentMap.put("segment1", segment1);

        Venue venue1 = new Venue("Test Venue", location1, segmentMap);
        when(mockVenueRepository.getVenueByID("venue1")).thenReturn(venue1);
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);
        
        e1 = new Event(new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 1, 5.0, 3.5), user.getUserID());
        when(mockEventRepository.getEventByID(e1.getEventID())).thenReturn(e1);
        when(mockEventRepository.searchEvents(List.of("empty"), null, null, null, null, null, null, null, null, null)).thenReturn(new ArrayList<>(List.of()));
        venue1.bookEvent(e1.getEventStartTime(), e1.getEventEndTime(), 1);
    }




    @Test //2.2.1.ii //2.2
    public void ReceiveEventInfo_Success() {
        assertEquals(new EventDTO(e1), eventService.viewEvent(e1.getEventID()).getValue());
    }

    @Test
    public void ReceiveEventInfo_EventNotExists_Failure() {
        assertEquals("Event with ID 500 not found", eventService.viewEvent(500).getError());
    }

    @Test //2.2.3.i
    public void EditEvents_Success() {
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("name", "Updated Event");
        assertEquals(new EventDTO(e1), eventService.editEvent(editParams, e1.getEventID(), "user1").getValue());
    }

    @Test
    public void EditEvents_InvalidParameterName_Failure() {
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("name", 123);
        assertEquals("Invalid type for parameter 'name'. Expected: String", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test
    public void EditEvents_InvalidParameterArtist_Failure() {
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("artist", 123);
        assertEquals("Invalid type for parameter 'artist'. Expected: String", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test
    public void EditEvents_InvalidParameterCategory_Failure() {
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("category", 123);
        assertEquals("Invalid type for parameter 'category'. Expected: String", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test
    public void EditEvents_InvalidParameterStartTime_Failure() {
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("startTime", "not_a_date");
        editParams.put("endTime", LocalDateTime.now().plusDays(1));
        assertEquals("Invalid type for parameter 'startTime'. Expected: LocalDateTime", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test
    public void SearchEvents_InvalidParameterOnlyStartTime_Failure() {
        LocalDateTime now = LocalDateTime.now();
        when(mockEventRepository.searchEvents(null, null, null, null, null, null, null, List.of(now, now.plusDays(1)), null, null)).thenThrow(new IllegalArgumentException("End time filter must have exactly one value."));
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("startTime", now.plusDays(1));
        assertEquals("Must edit both start and end time together to update event time !", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test
    public void EditEvents_InvalidParameterEndTime_Failure() {
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("startTime", LocalDateTime.now());
        editParams.put("endTime", "not_a_date");
        assertEquals("Invalid type for parameter 'endTime'. Expected: LocalDateTime", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test
    public void SearchEvents_InvalidParameterOnlyEndTime_Failure() {
        LocalDateTime now = LocalDateTime.now();
        when(mockEventRepository.searchEvents(null, null, null, null, null, null, null, List.of(now, now.plusDays(1)), null, null)).thenThrow(new IllegalArgumentException("End time filter must have exactly one value."));
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("endTime", now.plusDays(1));
        assertEquals("Must edit both start and end time together to update event time !", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test
    public void EditEvents_InvalidParameterEventRating_Failure() {
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("eventRating", "not_a_double");
        assertEquals("Invalid type for parameter 'eventRating'. Expected: Double", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test
    public void EditEvents_InvalidParameterVenue_Failure() {
        Map<String, Object> editParams = new TreeMap<>();
        editParams.put("venue", 123);
        assertEquals("Invalid type for parameter 'venue'. Expected: String", eventService.editEvent(editParams, e1.getEventID(), "user1").getError());
    }

    @Test //2.2.3.i
    public void SearchEvents_Success() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("name", List.of("empty"));
        assertEquals(new ArrayList<EventDTO>(), eventService.searchEvents(searchParams).getValue());
    }

    @Test
    public void SearchEvents_InvalidParameterName_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("name", List.of(123));
        assertEquals("Invalid search parameters: Invalid type for parameter 'name'. Expected: String", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterArtist_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("artist", List.of(123));
        assertEquals("Invalid search parameters: Invalid type for parameter 'artist'. Expected: String", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterCategory_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("category", List.of(123));
        assertEquals("Invalid search parameters: Invalid type for parameter 'category'. Expected: String", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterKeyword_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("keyword", List.of(123));
        assertEquals("Invalid search parameters: Invalid type for parameter 'keyword'. Expected: String", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterMinPrice_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("minPrice", List.of("not_a_number"));
        assertEquals("Invalid search parameters: Invalid type for parameter 'minPrice'. Expected: Double", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterMinPriceMoreThanOne_Failure() {
        when(mockEventRepository.searchEvents(null, null, null, null, List.of(123.45, 67.89), null, null, null, null, null)).thenThrow(new IllegalArgumentException("Min price filter must have exactly one value."));
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("minPrice", List.of(123.45, 67.89));
        assertEquals("Invalid search parameters: Min price filter must have exactly one value.", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterMaxPrice_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("maxPrice", List.of("not_a_number"));
        assertEquals("Invalid search parameters: Invalid type for parameter 'maxPrice'. Expected: Double", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterMaxPriceMoreThanOne_Failure() {
        when(mockEventRepository.searchEvents(null, null, null, null, null, List.of(123.45, 67.89), null, null, null, null)).thenThrow(new IllegalArgumentException("Max price filter must have exactly one value."));
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("maxPrice", List.of(123.45, 67.89));
        assertEquals("Invalid search parameters: Max price filter must have exactly one value.", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterStartTime_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("startTime", List.of("not_a_date"));
        assertEquals("Invalid search parameters: Invalid type for parameter 'startTime'. Expected: LocalDateTime", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterStartTimeMoreThanOne_Failure() {
        LocalDateTime now = LocalDateTime.now();
        when(mockEventRepository.searchEvents(null, null, null, null, null, null, List.of(now, now.plusDays(1)), null, null, null)).thenThrow(new IllegalArgumentException("Start time filter must have exactly one value."));
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("startTime", List.of(now, now.plusDays(1)));
        assertEquals("Invalid search parameters: Start time filter must have exactly one value.", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterEndTime_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("endTime", List.of("not_a_date"));
        assertEquals("Invalid search parameters: Invalid type for parameter 'endTime'. Expected: LocalDateTime", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterEndTimeMoreThanOne_Failure() {
        LocalDateTime now = LocalDateTime.now();
        when(mockEventRepository.searchEvents(null, null, null, null, null, null, null, List.of(now, now.plusDays(1)), null, null)).thenThrow(new IllegalArgumentException("End time filter must have exactly one value."));
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("endTime", List.of(now, now.plusDays(1)));
        assertEquals("Invalid search parameters: End time filter must have exactly one value.", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterEventRating_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("eventRating", List.of("not_a_double"));
        assertEquals("Invalid search parameters: Invalid type for parameter 'eventRating'. Expected: Double", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterEventRatingMoreThanOne_Failure() {
        when(mockEventRepository.searchEvents(null, null, null, null, null, null, null, null, List.of(123.45, 67.89), null)).thenThrow(new IllegalArgumentException("Event rating filter must have exactly one value."));
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("eventRating", List.of(123.45, 67.89));
        assertEquals("Invalid search parameters: Event rating filter must have exactly one value.", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterPCRating_Failure() {
       Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("productionCompanyRating", List.of("not_a_double"));
        assertEquals("Invalid search parameters: Invalid type for parameter 'productionCompanyRating'. Expected: Double", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterPCRatingMoreThanOne_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("productionCompanyRating", List.of(123.45, 67.89));
        assertEquals("Invalid search parameters: Production company rating filter must have exactly one value.", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterPCID_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("productionCompany", List.of(123));
        assertEquals("Invalid search parameters: Invalid type for parameter 'productionCompany'. Expected: String", eventService.searchEvents(searchParams).getError());
    }

    @Test
    public void SearchEvents_InvalidParameterLocation_Failure() {
        Map<String, List<Object>> searchParams = new TreeMap<>();
        searchParams.put("location", List.of(123));
        assertEquals("Invalid search parameters: Invalid type for parameter 'location'. Expected: String", eventService.searchEvents(searchParams).getError());
    }

    @Test //2.4.1.1
    public void CreateEvent_Success() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now.plusDays(6), now.plusDays(7), "artist2", "category2", 1, 67.0, 4.5);
        EventDTO e2 = new EventDTO(new Event(record, user.getUserID()));
        Field eid = e2.getClass().getDeclaredField("eventID");
        eid.setAccessible(true);
        eid.set(e2, e2.getEventID() + 1);
        assertEquals(e2, eventService.createEvent(record, "user1").getValue());
    }

    @Test
    public void CreateEvent_UserIsNotAuthenticated_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2", 1, 67.0, 4.5);
        assertEquals("Invalid session token.", eventService.createEvent(record, "invalid_token").getError());
    }
    
    @Test
    public void CreateEvent_CompanyNotFound_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2", 999, 67.0, 4.5);
        assertEquals("Invalid production company ID. Please provide a valid production company ID to create an event.", eventService.createEvent(record, "user1").getError());
    }

    @Test
    public void CreateEvent_UserIsNotOwner_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2", 1, 67.0, 4.5);
        assertEquals("User does not have a role for this company.", eventService.createEvent(record, "user2").getError());
    }

    @Test
    public void CreateEvent_InvalidName_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "", now, now.plusDays(1), "artist2", "category2", 1, 67.0, 4.5);
        assertEquals("Event name cannot be null or empty.", eventService.createEvent(record, "user1").getError());
        record = new EventRecord("venue1", null, now, now.plusDays(1), "artist2", "category2", 1, 67.0, 4.5);
        assertEquals("Event name cannot be null or empty.", eventService.createEvent(record, "user1").getError());
    }
    
    @Test
    public void CreateEvent_InvalidDates_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now.minusDays(2), now.minusDays(1), "artist2", "category2", 1, 67.0, 4.5);
        assertEquals("End time must be in the future.", eventService.createEvent(record, "user1").getError());
        record = new EventRecord("venue1", "event2", now.plusDays(2), now.plusDays(1), "artist2", "category2", 1, 67.0, 4.5);
        assertEquals("Start time must be before end time.", eventService.createEvent(record, "user1").getError());
        record = new EventRecord("venue1", "event2", null, now.plusDays(1), "artist2", "category2", 1, 67.0, 4.5);
        assertEquals("Start time and end time cannot be null.", eventService.createEvent(record, "user1").getError());
        record = new EventRecord("venue1", "event2", now.plusDays(1), null, "artist2", "category2", 1, 67.0, 4.5);
        assertEquals("Start time and end time cannot be null.", eventService.createEvent(record, "user1").getError());
    }

    @Test
    public void CreateEvent_InvalidArtist_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "", "category2", 1, 67.0, 4.5);
        assertEquals("Event artist cannot be null or empty.", eventService.createEvent(record, "user1").getError());
        record = new EventRecord("venue1", "event2", now, now.plusDays(1), null, "category2", 1, 67.0, 4.5);
        assertEquals("Event artist cannot be null or empty.", eventService.createEvent(record, "user1").getError());
    }

    @Test
    public void CreateEvent_InvalidCategory_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "", 1, 67.0, 4.5);
        assertEquals("Event category cannot be null or empty.", eventService.createEvent(record, "user1").getError());
        record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", null, 1, 67.0, 4.5);
        assertEquals("Event category cannot be null or empty.", eventService.createEvent(record, "user1").getError());
    }

    @Test
    public void CreateEvent_InvalidPrice_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2", 1, -1.0, 4.5);
        assertEquals("Event price cannot be negative.", eventService.createEvent(record, "user1").getError());
    }

    @Test
    public void CreateEvent_InvalidRating_Failure() {
        LocalDateTime now = LocalDateTime.now();
        EventRecord record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2", 1, 67.0, -1.0);
        assertEquals("Event rating must be between 0 and 5.", eventService.createEvent(record, "user1").getError());
        record = new EventRecord("venue1", "event2", now, now.plusDays(1), "artist2", "category2", 1, 67.0, 6.0);
        assertEquals("Event rating must be between 0 and 5.", eventService.createEvent(record, "user1").getError());
    }

    @Test
    public void CreateEvent_VenueIsUnavailable_Failure() {
        EventRecord record = new EventRecord("venue1", "event2", e1.getEventStartTime(), e1.getEventEndTime(), "artist2", "category2", 1, 67.0, 4.5);
        Result<EventDTO> result = eventService.createEvent(record, "user1");
        assertFalse(result.isSuccess());
        assertTrue(result.getError().startsWith("Venue is already booked during this time frame!"));
    }

    //update event //2.4.1.2

    @Test //2.4.1.3
    public void DeactivateEvent_Success() {
        e1.activateEvent();
        assertEquals(true, eventService.deactivateEvent(e1.getEventID(), "user1").getValue());
    }

    @Test
    public void DeactivateEvent_EventNotFound_Failure() {
        assertEquals("Event with ID 500 not found", eventService.deactivateEvent(500, "user1").getError());
    }

    @Test
    public void DeactivateEvent_UserIsNotAuthenticated_Failure() {
        assertEquals("Invalid session token.", eventService.deactivateEvent(e1.getEventID(), "invalid_token").getError());
    }

    @Test
    public void DeactivateEvent_UserIsNotOwner_Failure() {
        assertEquals("User does not have a role for this company.", eventService.deactivateEvent(e1.getEventID(), "user2").getError());
    }

    @Test
    public void DeactivateEvent_EventAlreadyDeactivated_Failure() { 
        assertEquals("Event is already inactive.", eventService.deactivateEvent(e1.getEventID(), "user1").getError());
    }
    
    @Test //2.4.1.3
    public void ActivateEvent_Success() {
        assertEquals(true, eventService.activateEvent(e1.getEventID(), "user1").getValue());
    }

    @Test
    public void ActivateEvent_EventNotFound_Failure() {
        assertEquals("Event with ID 500 not found", eventService.activateEvent(500, "user1").getError());
    }

    @Test
    public void ActivateEvent_UserIsNotAuthenticated_Failure() {
        assertEquals("Invalid session token.", eventService.activateEvent(e1.getEventID(), "invalid_token").getError());
    }

    @Test
    public void ActivateEvent_UserIsNotOwner_Failure() {
        assertEquals("User does not have a role for this company.", eventService.activateEvent(e1.getEventID(), "user2").getError());
    }

    @Test
    public void ActivateEvent_EventAlreadyActive_Failure() { 
        assertEquals("Event is already active.", eventService.activateEvent(e1.getEventID(), "user1").getError());
    }

}
