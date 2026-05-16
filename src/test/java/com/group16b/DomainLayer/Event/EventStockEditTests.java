package com.group16b.DomainLayer.Event;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.EventService;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocatoinService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.IVirtualQueueRepository;


// this class is here since eventTests checks ecent itself and not this feature with relevant mocks
public class EventStockEditTests {
    private EventService eventService;

    // Injected via Constructor
    private IAuthenticationService mockAuthService;
    private ILocatoinService mockLocationService;
    private EventFilteringService mockEventFilteringService;

    // Injected via Reflection (Singletons)
    private IUserRepository mockUserRepo;
    private IVenueRepository mockVenueRepo;
    private IEventRepository mockEventRepo;
    private IVirtualQueueRepository mockQueueRepo;
    private IProductionCompanyRepository mockPolicyRepo;

    private final String VALID_TOKEN = "valid-session-token";
    private final int USER_ID = 1;
    private final int EVENT_ID = 10;
    private final int VENUE_ID = 50;
    private final int COMPANY_ID = 100;

    @BeforeEach
    void setUp() throws Exception {
        mockAuthService = mock(IAuthenticationService.class);
        mockLocationService = mock(ILocatoinService.class);
        mockEventFilteringService = mock(EventFilteringService.class);
        
        mockUserRepo = mock(IUserRepository.class);
        mockVenueRepo = mock(IVenueRepository.class);
        mockEventRepo = mock(IEventRepository.class);
        mockQueueRepo = mock(IVirtualQueueRepository.class);
        mockPolicyRepo = mock(IProductionCompanyRepository.class);

        eventService = new EventService(mockAuthService, mockLocationService, mockEventFilteringService,mockPolicyRepo);

        // 3. Inject Singleton Repositories via Reflection
        setPrivateField(eventService, "userRepository", mockUserRepo);
        setPrivateField(eventService, "venueRepository", mockVenueRepo);
        setPrivateField(eventService, "eventRepository", mockEventRepo);
        setPrivateField(eventService, "queueRepository", mockQueueRepo);
        setPrivateField(eventService, "productionCompanyRepository", mockPolicyRepo);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(USER_ID));
    }

    //helper for singelton injection in mocks
    private void setPrivateField(Object targetObject, String fieldName, Object valueToSet) throws Exception {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, valueToSet);
    }

    @Test
    void editStockInSegmentsForEvent_Success() {
        User mockUser = mock(User.class);
        when(mockUserRepo.getUserByID(USER_ID)).thenReturn(mockUser);
        doNothing().when(mockUser).validatePermissions(COMPANY_ID, Owner.class);

        Event mockEvent = mock(Event.class);
        when(mockEventRepo.getEventByID(EVENT_ID)).thenReturn(mockEvent);
        when(mockEvent.getEventProductionCompanyID()).thenReturn(COMPANY_ID);
        when(mockEvent.getEventVenueID()).thenReturn(String.valueOf(VENUE_ID));

        Venue mockVenue = mock(Venue.class);
        when(mockVenueRepo.getVenueByID(String.valueOf(VENUE_ID))).thenReturn(mockVenue);

        Segment mockSegmentA = mock(Segment.class);
        Segment mockSegmentB = mock(Segment.class);
        when(mockVenue.getSegmentByID("SEG_A")).thenReturn(mockSegmentA);
        when(mockVenue.getSegmentByID("SEG_B")).thenReturn(mockSegmentB);

        Map<String, Integer> segmentsAndNewStock = new HashMap<>();
        segmentsAndNewStock.put("SEG_A", 100);
        segmentsAndNewStock.put("SEG_B", 50);

        Result<String> result = eventService.editStockInSegmentsForEvent(segmentsAndNewStock, EVENT_ID, VALID_TOKEN);

        assertTrue(result.isSuccess(), "Stock edit should succeed. Error: " + result.getError());
        verify(mockSegmentA, times(1)).setStockForEvent(EVENT_ID, 100);
        verify(mockSegmentB, times(1)).setStockForEvent(EVENT_ID, 50);
    }

    @Test
    void editStockInSegmentsForEvent_InvalidToken_Fails() {
        when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(false);

        Map<String, Integer> stockMap = new HashMap<>();
        Result<String> result = eventService.editStockInSegmentsForEvent(stockMap, EVENT_ID, VALID_TOKEN);

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());
    }

    @Test
    void editStockInSegmentsForEvent_NotUserToken_Fails() {
        when(mockAuthService.isUserToken(VALID_TOKEN)).thenReturn(false);

        Map<String, Integer> stockMap = new HashMap<>();
        Result<String> result = eventService.editStockInSegmentsForEvent(stockMap, EVENT_ID, VALID_TOKEN);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Only signed-in users are allowed to create events"));
    }

    @Test
    void editStockInSegmentsForEvent_PermissionDenied_CaughtByCatchBlock() {
        User mockUser = mock(User.class);
        when(mockUserRepo.getUserByID(USER_ID)).thenReturn(mockUser);
        
        Event mockEvent = mock(Event.class);
        when(mockEventRepo.getEventByID(EVENT_ID)).thenReturn(mockEvent);
        when(mockEvent.getEventProductionCompanyID()).thenReturn(COMPANY_ID);

        doThrow(new IllegalArgumentException("Unauthorized action")).when(mockUser).validatePermissions(COMPANY_ID, Owner.class);

        Map<String, Integer> stockMap = new HashMap<>();
        Result<String> result = eventService.editStockInSegmentsForEvent(stockMap, EVENT_ID, VALID_TOKEN);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("An unexpected error occurred: Unauthorized action"));
    }
}
