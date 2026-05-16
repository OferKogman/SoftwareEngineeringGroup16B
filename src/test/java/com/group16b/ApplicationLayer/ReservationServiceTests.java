package com.group16b.ApplicationLayer;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyPolicyRepository;
import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Seat;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.IVirtualQueueRepository;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;




public class ReservationServiceTests {

    private ReserveService reserveService;
    private IAuthenticationService mockAuthenticationService;
    private IVenueRepository mockVenueRepository;
    private IOrderRepository mockOrderRepository;
    private IVirtualQueueRepository mockQueueRepository;
    private IEventRepository mockEventRepository;
    private IProductionCompanyPolicyRepository mockProductionCompanyRepository;


    private static final String SEGMENT_ID = "segment1";
    private static final List<String> SEAT_IDS = List.of("1-1", "1-2");
    private static final int EVENT_ID = 1;
    private static final String VENUE_ID = "venue1";
    private static final String SESSION_TOKEN = "valid-token";
    private static final String USER_ID = "42";
    private static final int PRODUCTION_COMPANY_ID = 1;
    private static final String FIELD_SEGMENT_ID = "fieldSegment1";
    private static final int FIELD_AMOUNT = 2;

    private static Venue fieldVenue;
    private static FieldSeg fieldSegment;

    private static Venue venue;
    private static Event event;
    private static VirtualQueue queue;

    private static Set<PurchasePolicy> companyPurchasePolicies;
    private static Set<DiscountPolicy> companyDiscountPolicies;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        PurchasePolicy alwaysValidPurchasePolicy = new PurchasePolicy() {
            @Override
            public boolean validatePurchase() {
                return true;
            }
        };

        DiscountPolicy noDiscountPolicy = new DiscountPolicy() {
            @Override
            public double calculateDiscount(double price) {
                return price;
            }
        };

        Set<PurchasePolicy> eventPurchasePolicies = new HashSet<>();
        eventPurchasePolicies.add(alwaysValidPurchasePolicy);

        Set<DiscountPolicy> eventDiscountPolicies = new HashSet<>();
        eventDiscountPolicies.add(noDiscountPolicy);

        companyPurchasePolicies = new HashSet<>();
        companyDiscountPolicies = new HashSet<>();

        HashMap<String, Seat> seats = new HashMap<>();
        seats.put("1-1", new Seat(1, 1));
        seats.put("1-2", new Seat(1, 2));
        seats.put("1-3", new Seat(1, 3));

        Segment segment = new ChosenSeatingSeg(SEGMENT_ID, seats);

        fieldSegment = new FieldSeg(FIELD_SEGMENT_ID, 100);

        HashMap<String, Segment> segments = new HashMap<>();
        segments.put(SEGMENT_ID, segment);
        segments.put(FIELD_SEGMENT_ID, fieldSegment);
        venue = new Venue(VENUE_ID, null, segments);

        EventRecord eventRecord = new EventRecord(
                VENUE_ID,
                "event_1",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "artist_1",
                "category_1",
                PRODUCTION_COMPANY_ID,
                50.0,
                4.5
        );

        event = new Event(eventRecord, PRODUCTION_COMPANY_ID);
        event.activateEvent();

        queue = new VirtualQueue(EVENT_ID);
        queue.addToQueue(USER_ID);


        
    }

    @BeforeEach
    void setUp() throws Exception {

        mockAuthenticationService = mock(IAuthenticationService.class);
        mockVenueRepository = mock(IVenueRepository.class);
        mockOrderRepository = mock(IOrderRepository.class);
        mockQueueRepository = mock(IVirtualQueueRepository.class);
        mockEventRepository = mock(IEventRepository.class);
        mockProductionCompanyRepository = mock(IProductionCompanyPolicyRepository.class);

        reserveService = new ReserveService(mockAuthenticationService);

        // inject venue repo
        Field venueRepoField = ReserveService.class.getDeclaredField("venueRepo");
        venueRepoField.setAccessible(true);
        venueRepoField.set(reserveService, mockVenueRepository);

        // inject order repo
        Field orderRepoField = ReserveService.class.getDeclaredField("orderRepo");
        orderRepoField.setAccessible(true);
        orderRepoField.set(reserveService, mockOrderRepository);

        // inject queue repo
        Field queueRepoField = ReserveService.class.getDeclaredField("queueImp");
        queueRepoField.setAccessible(true);
        queueRepoField.set(reserveService, mockQueueRepository);

        // inject event repo
        Field eventRepoField = ReserveService.class.getDeclaredField("eventRepository");
        eventRepoField.setAccessible(true);
        eventRepoField.set(reserveService, mockEventRepository);

        // inject production company repo
        Field productionCompanyRepoField = ReserveService.class.getDeclaredField("productionCompanyRepo");
        productionCompanyRepoField.setAccessible(true);
        productionCompanyRepoField.set(reserveService, mockProductionCompanyRepository);


        when(mockAuthenticationService.validateToken(SESSION_TOKEN)).thenReturn(true);
        when(mockAuthenticationService.isUserToken(SESSION_TOKEN)).thenReturn(true);
        when(mockAuthenticationService.isAdminToken(SESSION_TOKEN)).thenReturn(false);

        when(mockAuthenticationService.extractSubjectFromToken(SESSION_TOKEN)).thenReturn(USER_ID);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(event);
        when(mockQueueRepository.findVirtualQueueById(EVENT_ID)).thenReturn(queue);
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(venue);
        when(mockProductionCompanyRepository.getPurchasePolicyByID(PRODUCTION_COMPANY_ID)).thenReturn(companyPurchasePolicies);
        when(mockProductionCompanyRepository.getDiscountPolicyByID(PRODUCTION_COMPANY_ID)).thenReturn(companyDiscountPolicies);

    }
     // ________ reseurveSeats tests ________
    @Test
    void reserveSeats_validRequest_createsOrderSuccessfully() {

        Result<String> result = reserveService.reserveSeats(SEGMENT_ID, SEAT_IDS, EVENT_ID, VENUE_ID, SESSION_TOKEN);

        assertTrue(result.isSuccess());
        assertTrue(result.getValue().startsWith("new OrderId: "));
        verify(mockVenueRepository).reserveTickets(VENUE_ID, SEGMENT_ID, SEAT_IDS, EVENT_ID);
        verify(mockOrderRepository).addOrder(any(Order.class));
    }
        
    @Test
    void reserveSeats_invalidToken_returnsFail() throws Exception {
        String invalidToken = "invalid-token";

        when(mockAuthenticationService.validateToken(invalidToken)).thenReturn(false);

        Result<String> result = reserveService.reserveSeats(
                SEGMENT_ID,
                SEAT_IDS,
                EVENT_ID,
                VENUE_ID,
                invalidToken
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockOrderRepository, never()).addOrder(any(Order.class));

        verify(mockVenueRepository, never()).reserveTickets(anyString(), anyString(), anyList(), anyInt());
    }

    @Test
    void reserveSeats_adminToken_returnsFail() throws Exception {
        String adminToken = "admin-token";

        when(mockAuthenticationService.validateToken(adminToken)).thenReturn(true);
        when(mockAuthenticationService.isAdminToken(adminToken)).thenReturn(true);
        when(mockAuthenticationService.isUserToken(adminToken)).thenReturn(false);

        Result<String> result = reserveService.reserveSeats(
                SEGMENT_ID,
                SEAT_IDS,
                EVENT_ID,
                VENUE_ID,
                adminToken
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
        verify(mockVenueRepository, never()).reserveTickets(anyString(), anyString(), anyList(), anyInt());
    }

    @Test
    void reserveSeats_inactiveEvent_returnsFail() throws Exception {
        EventRecord inactiveRecord = new EventRecord(
                VENUE_ID,
                "inactive_event",
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(1),
                "artist",
                "category",
                PRODUCTION_COMPANY_ID,
                50.0,
                4.5
        );

        Event inactiveEvent = new Event(inactiveRecord, PRODUCTION_COMPANY_ID);

        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(inactiveEvent);

        Result<String> result = reserveService.reserveSeats(
                SEGMENT_ID,
                SEAT_IDS,
                EVENT_ID,
                VENUE_ID,
                SESSION_TOKEN
        );

        assertFalse(result.isSuccess());
        assertEquals("Event is inactive", result.getError());

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
        verify(mockVenueRepository, never()).reserveTickets(anyString(), anyString(), anyList(), anyInt());
    }
    @Test
    void reserveSeats_eventHasLotteryPolicy_returnsFail() throws Exception {
        LotteryPolicy lotteryPolicy = mock(LotteryPolicy.class);

        Event eventWithLottery = spy(event);
        when(eventWithLottery.getLotteryPolicy()).thenReturn(lotteryPolicy);

        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithLottery);

        Result<String> result = reserveService.reserveSeats(
                SEGMENT_ID,
                SEAT_IDS,
                EVENT_ID,
                VENUE_ID,
                SESSION_TOKEN
        );

        assertFalse(result.isSuccess());
        assertEquals(
                "User did not provide lottery keypass to reserve seats for this event",
                result.getError()
        );

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
        verify(mockVenueRepository, never()).reserveTickets(anyString(), anyString(), anyList(), anyInt());
    }

    @Test
    void reserveSeats_userDidNotPassQueue_returnsFail() throws Exception {
        VirtualQueue queueThatDoesNotPassUser = mock(VirtualQueue.class);

        when(queueThatDoesNotPassUser.isUserPassedQueue(USER_ID)).thenReturn(false);

        when(mockQueueRepository.findVirtualQueueById(EVENT_ID)).thenReturn(queueThatDoesNotPassUser);

        Result<String> result = reserveService.reserveSeats(
                SEGMENT_ID,
                SEAT_IDS,
                EVENT_ID,
                VENUE_ID,
                SESSION_TOKEN
        );

        assertFalse(result.isSuccess());
        assertEquals("User did not pass the queue", result.getError());

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
        verify(mockVenueRepository, never()).reserveTickets(anyString(), anyString(), anyList(), anyInt());
    }

    @Test
    void reserveSeats_noPurchasePolicy_returnsFail() throws Exception {
        Event eventWithoutPurchasePolicy = spy(event);
        when(eventWithoutPurchasePolicy.getEventPurchasePolicy()).thenReturn(null);

        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithoutPurchasePolicy);

        Result<String> result = reserveService.reserveSeats(
                SEGMENT_ID,
                SEAT_IDS,
                EVENT_ID,
                VENUE_ID,
                SESSION_TOKEN
        );

        assertFalse(result.isSuccess());
        assertEquals(
                "No purchase policy found for this event",
                result.getError()
        );

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
    }
    @Test
    void reserveSeats_noDiscountPolicy_returnsFail() throws Exception {
        Event eventWithoutDiscountPolicy = spy(event);
        when(eventWithoutDiscountPolicy.getEventDiscountPolicy()).thenReturn(null);

        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithoutDiscountPolicy);

        Result<String> result = reserveService.reserveSeats(
                SEGMENT_ID,
                SEAT_IDS,
                EVENT_ID,
                VENUE_ID,
                SESSION_TOKEN
        );

        assertFalse(result.isSuccess());
        assertEquals(
                "No discount policy found for this event",
                result.getError()
        );

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
    }






    

    // ________ reserveFieldSeats tests ________
    @Test
    void reserveFieldSeats_validRequest_createsOrderSuccessfully() {
        Result<String> result = reserveService.reserveFieldSeats(
                FIELD_SEGMENT_ID,
                FIELD_AMOUNT,
                EVENT_ID,
                VENUE_ID,
                SESSION_TOKEN
        );
        if (!result.isSuccess()) {
            String errorMessage = result.getError() != null ? result.getError() : "Unknown error";
            System.out.println("Error: " + errorMessage);
        }
        assertTrue(result.isSuccess());
        assertTrue(result.getValue().startsWith("new OrderId: "));

        verify(mockVenueRepository).reserveTickets(VENUE_ID, FIELD_SEGMENT_ID, FIELD_AMOUNT, EVENT_ID);
        verify(mockOrderRepository).addOrder(any(Order.class));
    }
    @Test
    void reserveFieldSeats_invalidAmount_returnsFail() {
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(fieldVenue);

        int invalidAmount = -1;

        Result<String> result = reserveService.reserveFieldSeats(
                FIELD_SEGMENT_ID,
                invalidAmount,
                EVENT_ID,
                VENUE_ID,
                SESSION_TOKEN
        );

        assertFalse(result.isSuccess());

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
    }
    @Test
    void reserveFieldSeats_adminToken_returnsFail() throws Exception {
        String adminToken = "admin-token";

        when(mockAuthenticationService.validateToken(adminToken)).thenReturn(true);
        when(mockAuthenticationService.isAdminToken(adminToken)).thenReturn(true);
        when(mockAuthenticationService.isUserToken(adminToken)).thenReturn(false);

        Result<String> result = reserveService.reserveFieldSeats(
                FIELD_SEGMENT_ID,
                FIELD_AMOUNT,
                EVENT_ID,
                VENUE_ID,
                adminToken
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
        verify(mockVenueRepository, never()).reserveTickets(anyString(), anyString(), anyInt(), anyInt());
    }
    @Test
    void reserveFieldSeats_eventHasLotteryPolicy_returnsFail() {
        LotteryPolicy lotteryPolicy = mock(LotteryPolicy.class);

        Event eventWithLottery = spy(event);
        when(eventWithLottery.getLotteryPolicy()).thenReturn(lotteryPolicy);

        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithLottery);

        Result<String> result = reserveService.reserveFieldSeats(
                FIELD_SEGMENT_ID,
                FIELD_AMOUNT,
                EVENT_ID,
                VENUE_ID,
                SESSION_TOKEN
        );

        assertFalse(result.isSuccess());
        assertEquals(
                "User did not provide lottery keypass to reserve seats for this event",
                result.getError()
        );

        verify(mockOrderRepository, never()).addOrder(any(Order.class));
        verify(mockVenueRepository, never()).reserveTickets(anyString(), anyString(), anyInt(), anyInt());
    }




    // ________ reserveSeatsWithLottery tests ________
    // TODO roleta tests

}
