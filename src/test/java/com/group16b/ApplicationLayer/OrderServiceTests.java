package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompanyPolicy.IProductionCompanyPolicyRepository;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Seat;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.InfrastructureLayer.PaymentService;

public class OrderServiceTests {

    private OrderService orderService;
    private PaymentService mockPaymentService;
    private IAuthenticationService mockAuthenticationService;
    private ITicketGateway mockTicketGateway;
    private IOrderRepository mockOrderRepository;
    private IVenueRepository mockVenueRepository;
    private IEventRepository mockEventRepository;
    private IUserRepository mockUserRepository;
    private IProductionCompanyPolicyRepository mockProductionCompanyRepository;

    private static final String SESSION_TOKEN = "valid-token";
    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_ID_STRING = "42";
    private static final int USER_ID = 42;

    private static final String ORDER_ID = "order1";
    private static final int EVENT_ID = 1;
    private static final String VENUE_ID = "venue1";
    private static final String SEGMENT_ID = "segment1";
    private static final int PRODUCTION_COMPANY_ID = 1;

    private static Event event;
    private static Venue venue;
    private static Segment segment;

    private static Set<PurchasePolicy> companyPurchasePolicies;
    private static Set<DiscountPolicy> companyDiscountPolicies;

    @BeforeAll
    static void beforeAll() {
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

        segment = new ChosenSeatingSeg(SEGMENT_ID, seats);

        Map<String, Segment> segments = new HashMap<>();
        segments.put(SEGMENT_ID, segment);

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
    }

    @BeforeEach
    void setUp() throws Exception {
        mockAuthenticationService = mock(IAuthenticationService.class);
        mockTicketGateway = mock(ITicketGateway.class);
        mockOrderRepository = mock(IOrderRepository.class);
        mockVenueRepository = mock(IVenueRepository.class);
        mockEventRepository = mock(IEventRepository.class);
        mockUserRepository = mock(IUserRepository.class);
        mockPaymentService = mock(PaymentService.class);
        mockProductionCompanyRepository = mock(IProductionCompanyPolicyRepository.class);

        orderService = new OrderService(mockAuthenticationService);

        injectField(orderService, "ticketGateway", mockTicketGateway);
        injectField(orderService, "orderRepo", mockOrderRepository);
        injectField(orderService, "venueRepo", mockVenueRepository);
        injectField(orderService, "eventRepo", mockEventRepository);
        injectField(orderService, "userRepo", mockUserRepository);
        injectField(orderService, "productionCompanyRepo", mockProductionCompanyRepository);

        when(mockAuthenticationService.validateToken(SESSION_TOKEN)).thenReturn(true);
        when(mockAuthenticationService.isUserToken(SESSION_TOKEN)).thenReturn(true);
        when(mockAuthenticationService.extractSubjectFromToken(SESSION_TOKEN)).thenReturn(USER_ID_STRING);

        when(mockAuthenticationService.validateToken(ADMIN_TOKEN)).thenReturn(true);
        when(mockAuthenticationService.isAdminToken(ADMIN_TOKEN)).thenReturn(true);
        when(mockAuthenticationService.isUserToken(ADMIN_TOKEN)).thenReturn(false);


        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(event);
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(venue);

        when(mockProductionCompanyRepository.getPurchasePolicyByID(PRODUCTION_COMPANY_ID))
                .thenReturn(companyPurchasePolicies);

        when(mockProductionCompanyRepository.getDiscountPolicyByID(PRODUCTION_COMPANY_ID))
                .thenReturn(companyDiscountPolicies);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }



    // ________ CompleteActiveOrder tests ________


    @Test
    void CompleteActiveOrder_validRequest_returnsTicketsSuccessfully() {
        // Arrange
        Order activeSeatOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        User user = mock(User.class);

        PaymentInfo paymentInfo = new PaymentInfo(
                "Ran",
                "1234567812345678",
                "123",
                "12/30"
        );

        TicketDTO ticket1 = mock(TicketDTO.class);
        TicketDTO ticket2 = mock(TicketDTO.class);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(activeSeatOrder);
        when(mockUserRepository.getUserByID(USER_ID)).thenReturn(user);

        when(mockPaymentService.processPayment(paymentInfo, 100.0)).thenReturn(true);

        when(mockTicketGateway.generateTicket(
                EVENT_ID,
                USER_ID_STRING,
                SEGMENT_ID,
                "1-1",
                100.0
        )).thenReturn(ticket1);

        when(mockTicketGateway.generateTicket(
                EVENT_ID,
                USER_ID_STRING,
                SEGMENT_ID,
                "1-2",
                100.0
        )).thenReturn(ticket2);

        // Act
        Result<List<TicketDTO>> result = orderService.CompleteActiveOrder(
                USER_ID,
                ORDER_ID,
                SESSION_TOKEN,
                paymentInfo,
                mockPaymentService
        );

        // Assert
        assertTrue(result.isSuccess());

        List<TicketDTO> tickets = result.getValue();

        assertEquals(2, tickets.size());
        assertEquals(ticket1, tickets.get(0));
        assertEquals(ticket2, tickets.get(1));

        verify(mockOrderRepository).getOrder(ORDER_ID);
        verify(mockUserRepository).getUserByID(USER_ID);
        verify(mockPaymentService).processPayment(paymentInfo, 100.0);

        verify(mockTicketGateway).generateTicket(
                EVENT_ID,
                USER_ID_STRING,
                SEGMENT_ID,
                "1-1",
                100.0
        );

        verify(mockTicketGateway).generateTicket(
                EVENT_ID,
                USER_ID_STRING,
                SEGMENT_ID,
                "1-2",
                100.0
        );

        assertFalse(activeSeatOrder.isActive());
    }

    @Test
    void CompleteActiveOrder_orderNotFound_returnsFail() {
        PaymentInfo paymentInfo = mock(PaymentInfo.class);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(null);

        Result<List<TicketDTO>> result = orderService.CompleteActiveOrder(
                USER_ID,
                ORDER_ID,
                SESSION_TOKEN,
                paymentInfo,
                mockPaymentService
        );

        assertFalse(result.isSuccess());
        assertEquals("Order not found", result.getError());

        verify(mockPaymentService, never()).processPayment(any(), anyDouble());
        verify(mockTicketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), anyString(), anyDouble());
    }
    @Test
    void CompleteActiveOrder_orderNotActive_returnsFail() {
        PaymentInfo paymentInfo = mock(PaymentInfo.class);

        Order completedOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        completedOrder.CompleteOrder();

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(completedOrder);

        Result<List<TicketDTO>> result = orderService.CompleteActiveOrder(
                USER_ID,
                ORDER_ID,
                SESSION_TOKEN,
                paymentInfo,
                mockPaymentService
        );

        assertFalse(result.isSuccess());
        assertEquals("Order is not active", result.getError());

        verify(mockPaymentService, never()).processPayment(any(), anyDouble());
        verify(mockTicketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), anyString(), anyDouble());
    }
    @Test
    void CompleteActiveOrder_invalidToken_returnsFail() {
        PaymentInfo paymentInfo = mock(PaymentInfo.class);
        String invalidToken = "invalid-token";

        Order activeOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(activeOrder);
        when(mockAuthenticationService.validateToken(invalidToken)).thenReturn(false);

        Result<List<TicketDTO>> result = orderService.CompleteActiveOrder(
                USER_ID,
                ORDER_ID,
                invalidToken,
                paymentInfo,
                mockPaymentService
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockPaymentService, never()).processPayment(any(), anyDouble());
        verify(mockTicketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), anyString(), anyDouble());
    }
    @Test
    void CompleteActiveOrder_adminToken_returnsFail() {
        PaymentInfo paymentInfo = mock(PaymentInfo.class);

        Order activeOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(activeOrder);

        Result<List<TicketDTO>> result = orderService.CompleteActiveOrder(
                USER_ID,
                ORDER_ID,
                ADMIN_TOKEN,
                paymentInfo,
                mockPaymentService
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockPaymentService, never()).processPayment(any(), anyDouble());
        verify(mockTicketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), anyString(), anyDouble());
    }
    @Test
    void CompleteActiveOrder_orderDoesNotBelongToUser_returnsFail() {
        PaymentInfo paymentInfo = mock(PaymentInfo.class);

        Order activeOrderOfOtherUser = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                "999"
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(activeOrderOfOtherUser);

        Result<List<TicketDTO>> result = orderService.CompleteActiveOrder(
                USER_ID,
                ORDER_ID,
                SESSION_TOKEN,
                paymentInfo,
                mockPaymentService
        );

        assertFalse(result.isSuccess());
        assertEquals("Order does not belong to the given user", result.getError());

        verify(mockPaymentService, never()).processPayment(any(), anyDouble());
        verify(mockTicketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), anyString(), anyDouble());
    }
    @Test
    void CompleteActiveOrder_userNotFound_returnsFail() {
        PaymentInfo paymentInfo = mock(PaymentInfo.class);

        Order activeOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(activeOrder);
        when(mockUserRepository.getUserByID(USER_ID)).thenReturn(null);

        Result<List<TicketDTO>> result = orderService.CompleteActiveOrder(
                USER_ID,
                ORDER_ID,
                SESSION_TOKEN,
                paymentInfo,
                mockPaymentService
        );

        assertFalse(result.isSuccess());
        assertEquals("User not found", result.getError());

        verify(mockPaymentService, never()).processPayment(any(), anyDouble());
        verify(mockTicketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), anyString(), anyDouble());
    }


    // ________ getUserOrders tests ________

    @Test
    void getUserOrders_validUser_returnsOrderDTOsSuccessfully() {
        User user = mock(User.class);

        Order order1 = new Order(SEGMENT_ID, List.of("1-1", "1-2"), 100.0, EVENT_ID, USER_ID_STRING);
        Order order2 = new Order(SEGMENT_ID, List.of("1-3"), 50.0, EVENT_ID, USER_ID_STRING);
        order1.CompleteOrder();
        order2.CompleteOrder();
        when(mockUserRepository.getUserByID(USER_ID)).thenReturn(user);

        when(mockOrderRepository.getOrdersBySubjectID(USER_ID_STRING))
                .thenReturn(List.of(order1, order2));

        Result<List<OrderDTO>> result = orderService.getUserOrders(SESSION_TOKEN);

        if (!result.isSuccess()) {
            System.out.println("Error: " + result.getError());
        }
        assertTrue(result.isSuccess());
        assertEquals(2, result.getValue().size());

        verify(mockUserRepository).getUserByID(USER_ID);
        verify(mockOrderRepository).getOrdersBySubjectID(USER_ID_STRING);
    }
    @Test
    void getUserOrders_invalidToken_returnsFail() {
        String invalidToken = "invalid-token";

        when(mockAuthenticationService.validateToken(invalidToken)).thenReturn(false);

        Result<List<OrderDTO>> result = orderService.getUserOrders(invalidToken);

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockUserRepository, never()).getUserByID(anyInt());
        verify(mockOrderRepository, never()).getOrdersBySubjectID(anyString());
    }
    @Test
    void getUserOrders_nonSignedRole_returnsFail() {
        String customerToken = "customer-token";

        when(mockAuthenticationService.validateToken(customerToken)).thenReturn(true);
        when(mockAuthenticationService.isAdminToken(customerToken)).thenReturn(true);
        when(mockAuthenticationService.isUserToken(customerToken)).thenReturn(false);

        Result<List<OrderDTO>> result = orderService.getUserOrders(customerToken);

        assertFalse(result.isSuccess());
        assertEquals("Only user can get order history.", result.getError());

        verify(mockUserRepository, never()).getUserByID(anyInt());
        verify(mockOrderRepository, never()).getOrdersBySubjectID(anyString());
    }
    @Test
    void getUserOrders_userNotFound_returnsFail() {
        when(mockUserRepository.getUserByID(USER_ID)).thenReturn(null);

        Result<List<OrderDTO>> result = orderService.getUserOrders(SESSION_TOKEN);

        assertFalse(result.isSuccess());
        assertEquals("User not found.", result.getError());

        verify(mockUserRepository).getUserByID(USER_ID);
        verify(mockOrderRepository, never()).getOrdersBySubjectID(anyString());
    }
    @Test
    void getUserOrders_noOrders_returnsEmptyListSuccessfully() {
        User user = mock(User.class);

        when(mockUserRepository.getUserByID(USER_ID)).thenReturn(user);
        when(mockOrderRepository.getOrdersBySubjectID(USER_ID_STRING))
                .thenReturn(List.of());

        Result<List<OrderDTO>> result = orderService.getUserOrders(SESSION_TOKEN);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());

        verify(mockUserRepository).getUserByID(USER_ID);
        verify(mockOrderRepository).getOrdersBySubjectID(USER_ID_STRING);
    }


    // ________ changeSeatsToOrder tests ________


    @Test
    void changeSeatsToOrder_validSeatOrder_updatesSeatsSuccessfully() {
        Order order = createActiveSeatOrder();
        List<String> newSeats = List.of("1-2", "1-3");

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                newSeats
        );

        assertTrue(result.isSuccess());
        assertEquals(newSeats, result.getValue());

        verify(mockVenueRepository).reserveTickets(VENUE_ID, SEGMENT_ID, List.of("1-3"), EVENT_ID);
        verify(mockVenueRepository).freeTickets(VENUE_ID, SEGMENT_ID, List.of("1-1"), EVENT_ID);
        assertEquals(newSeats, order.getSeats());
    }

    @Test
    void changeSeatsToOrder_nullNewSeats_returnsFail() {
        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                null
        );

        assertFalse(result.isSuccess());
        assertEquals("New seat IDs list cannot be null or empty", result.getError());

        verify(mockOrderRepository, never()).getOrder(anyString());
    }

    @Test
    void changeSeatsToOrder_emptyNewSeats_returnsFail() {
        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of()
        );

        assertFalse(result.isSuccess());
        assertEquals("New seat IDs list cannot be null or empty", result.getError());

        verify(mockOrderRepository, never()).getOrder(anyString());
    }

    @Test
    void changeSeatsToOrder_invalidToken_returnsFail() {
        String invalidToken = "invalid-token";
        when(mockAuthenticationService.validateToken(invalidToken)).thenReturn(false);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                invalidToken,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockOrderRepository, never()).getOrder(anyString());
    }

    @Test
    void changeSeatsToOrder_adminToken_returnsFail() {
        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                ADMIN_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockOrderRepository, never()).getOrder(anyString());
    }

    @Test
    void changeSeatsToOrder_orderNotFound_returnsFail() {
        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(null);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Order not found", result.getError());
    }

    @Test
    void changeSeatsToOrder_orderDoesNotBelongToUser_returnsFail() {
        Order otherUserOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                "999"
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(otherUserOrder);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Order does not belong to the given user", result.getError());
    }

    @Test
    void changeSeatsToOrder_nonSeatOrder_returnsFail() {
        Order fieldOrder = new Order(
                SEGMENT_ID,
                2,
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(fieldOrder);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Cannot change seats for a non-seat order", result.getError());
    }

    @Test
    void changeSeatsToOrder_nonActiveOrder_returnsFail() {
        Order completedOrder = createActiveSeatOrder();
        completedOrder.CompleteOrder();

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(completedOrder);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Cannot change seats for a non-active order", result.getError());
    }

    @Test
    void changeSeatsToOrder_eventNotFound_returnsFail() {
        Order order = createActiveSeatOrder();

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(null);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Event not found", result.getError());
    }

    @Test
    void changeSeatsToOrder_venueNotFound_returnsFail() {
        Order order = createActiveSeatOrder();

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(null);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Venue not found", result.getError());
    }

    @Test
    void changeSeatsToOrder_segmentNotFound_returnsFail() {
        Order order = createActiveSeatOrder();

        Venue venueWithoutSegment = new Venue(
                VENUE_ID,
                null,
                Map.of()
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(venueWithoutSegment);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Segment not found", result.getError());
    }

    @Test
    void changeSeatsToOrder_purchasePolicyFails_returnsFail() {
        Order order = createActiveSeatOrder();

        Event eventWithFailingPolicy = spy(event);

        PurchasePolicy failingPolicy = new PurchasePolicy() {
            @Override
            public boolean validatePurchase() {
                return false;
            }
        };

        when(eventWithFailingPolicy.getEventPurchasePolicy())
                .thenReturn(new HashSet<>(Set.of(failingPolicy)));

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithFailingPolicy);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("Purchase policy validation failed for this event", result.getError());
    }

    @Test
    void changeSeatsToOrder_noPurchasePolicy_returnsFail() {
        Order order = createActiveSeatOrder();

        Event eventWithoutPurchasePolicy = spy(event);
        when(eventWithoutPurchasePolicy.getEventPurchasePolicy()).thenReturn(null);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithoutPurchasePolicy);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("No purchase policy found for this event", result.getError());
    }

    @Test
    void changeSeatsToOrder_noDiscountPolicy_returnsFail() {
        Order order = createActiveSeatOrder();

        Event eventWithoutDiscountPolicy = spy(event);
        when(eventWithoutDiscountPolicy.getEventDiscountPolicy()).thenReturn(null);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithoutDiscountPolicy);

        Result<List<String>> result = orderService.changeSeatsToOrder(
                ORDER_ID,
                SESSION_TOKEN,
                List.of("1-3")
        );

        assertFalse(result.isSuccess());
        assertEquals("No discount policy found for this event", result.getError());
    }

    private Order createActiveSeatOrder() {
    return new Order(
            SEGMENT_ID,
            List.of("1-1", "1-2"),
            100.0,
            EVENT_ID,
            USER_ID_STRING
    );
}

    // ________ changeNumOfSeatsInFieldOrder tests ________

    private Order createActiveFieldOrder(int numOfTickets) {
    return new Order(
            SEGMENT_ID,
            numOfTickets,
            100.0,
            EVENT_ID,
            USER_ID_STRING
    );
}

    @Test
    void changeNumOfSeatsInFieldOrder_validIncrease_reservesMoreSeatsAndUpdatesOrder() {
        Order order = createActiveFieldOrder(2);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertTrue(result.isSuccess());
        assertEquals(5, result.getValue());

        verify(mockVenueRepository).reserveTickets(VENUE_ID, SEGMENT_ID, 3, EVENT_ID);

        assertEquals(5, order.getNumOfTickets());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_validDecrease_freesSeatsAndUpdatesOrder() {
        Order order = createActiveFieldOrder(5);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                2
        );

        assertTrue(result.isSuccess());
        assertEquals(2, result.getValue());

        verify(mockVenueRepository).freeTickets(VENUE_ID, SEGMENT_ID, 3, EVENT_ID);

        assertEquals(2, order.getNumOfTickets());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_sameAmount_returnsOkWithoutChangingReservation() {
        Order order = createActiveFieldOrder(3);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                3
        );

        assertTrue(result.isSuccess());
        assertEquals(3, result.getValue());

        verify(mockVenueRepository, never()).reserveTickets(anyString(), anyString(), anyInt(), anyInt());
        verify(mockVenueRepository, never()).freeTickets(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_zeroSeats_returnsFail() {
        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                0
        );

        assertFalse(result.isSuccess());
        assertEquals("New number of seats must be greater than zero", result.getError());

        verify(mockOrderRepository, never()).getOrder(anyString());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_negativeSeats_returnsFail() {
        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                -5
        );

        assertFalse(result.isSuccess());
        assertEquals("New number of seats must be greater than zero", result.getError());

        verify(mockOrderRepository, never()).getOrder(anyString());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_invalidToken_returnsFail() {
        String invalidToken = "invalid-token";

        when(mockAuthenticationService.validateToken(invalidToken)).thenReturn(false);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                invalidToken,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockOrderRepository, never()).getOrder(anyString());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_adminToken_returnsFail() {
        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                ADMIN_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Invalid session token.", result.getError());

        verify(mockOrderRepository, never()).getOrder(anyString());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_orderNotFound_returnsFail() {
        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(null);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Order not found", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_orderDoesNotBelongToUser_returnsFail() {
        Order otherUserOrder = new Order(
                SEGMENT_ID,
                2,
                100.0,
                EVENT_ID,
                "999"
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(otherUserOrder);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Order does not belong to the given user", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_nonFieldOrder_returnsFail() {
        Order seatOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(seatOrder);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Cannot change seats for a non-field order", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_nonActiveOrder_returnsFail() {
        Order completedOrder = createActiveFieldOrder(2);
        completedOrder.CompleteOrder();

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(completedOrder);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Cannot change seats for a non-active order", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_eventNotFound_returnsFail() {
        Order order = createActiveFieldOrder(2);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(null);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Event not found", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_venueNotFound_returnsFail() {
        Order order = createActiveFieldOrder(2);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(null);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Venue not found", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_segmentNotFound_returnsFail() {
        Order order = createActiveFieldOrder(2);

        Venue venueWithoutSegment = new Venue(
                VENUE_ID,
                null,
                Map.of()
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(venueWithoutSegment);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Segment not found", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_purchasePolicyFails_returnsFail() {
        Order order = createActiveFieldOrder(2);

        Event eventWithFailingPolicy = spy(event);

        PurchasePolicy failingPolicy = new PurchasePolicy() {
            @Override
            public boolean validatePurchase() {
                return false;
            }
        };

        when(eventWithFailingPolicy.getEventPurchasePolicy())
                .thenReturn(new HashSet<>(Set.of(failingPolicy)));

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithFailingPolicy);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("Purchase policy validation failed for this event", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_noPurchasePolicy_returnsFail() {
        Order order = createActiveFieldOrder(2);

        Event eventWithoutPurchasePolicy = spy(event);
        when(eventWithoutPurchasePolicy.getEventPurchasePolicy()).thenReturn(null);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithoutPurchasePolicy);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("No purchase policy found for this event", result.getError());
    }

    @Test
    void changeNumOfSeatsInFieldOrder_noDiscountPolicy_returnsFail() {
        Order order = createActiveFieldOrder(2);

        Event eventWithoutDiscountPolicy = spy(event);
        when(eventWithoutDiscountPolicy.getEventDiscountPolicy()).thenReturn(null);

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(order);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(eventWithoutDiscountPolicy);

        Result<Integer> result = orderService.changeNumOfSeatsInFieldOrder(
                ORDER_ID,
                SESSION_TOKEN,
                5
        );

        assertFalse(result.isSuccess());
        assertEquals("No discount policy found for this event", result.getError());
    }


    // _________ cancelOrder tests ________
/* TODO: should work once bookEvent is fixed
    @Test
    void cancelOrder_validActiveSeatOrder_cancelsOrderAndFreesSeats() {
        Order seatOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );
        // reserve the seats to make sure they are marked as reserved before cancellation
        // TODO:
        venue.reserveSeats(ReservationRequest.forSeats(EVENT_ID, List.of("1-1", "1-2"), SEGMENT_ID));

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(seatOrder);

        Result<Boolean> result = orderService.cancelOrder(ORDER_ID);

        if (!result.isSuccess()) {
            String errorMessage = result.getError();
            System.out.println("Error: " + errorMessage);
        }

        assertTrue(result.isSuccess());
        assertTrue(result.getValue());

        verify(mockOrderRepository).cancelOrder(ORDER_ID);

        verify(mockEventRepository).getEventByID(EVENT_ID);
        verify(mockVenueRepository).getVenueByID(VENUE_ID);

        verify(mockOrderRepository, times(2)).getOrder(ORDER_ID);
    }
        */
/*  TODO: should work once bookEvent is fixed
    @Test
    void cancelOrder_validActiveFieldOrder_cancelsOrderAndFreesFieldTickets() {
        Order fieldOrder = new Order(
                SEGMENT_ID,
                5,
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(fieldOrder);

        Result<Boolean> result = orderService.cancelOrder(ORDER_ID);

        assertTrue(result.isSuccess());
        assertTrue(result.getValue());

        verify(mockOrderRepository).cancelOrder(ORDER_ID);

        verify(mockEventRepository).getEventByID(EVENT_ID);
        verify(mockVenueRepository).getVenueByID(VENUE_ID);

        verify(mockOrderRepository, times(2)).getOrder(ORDER_ID);
    } */ 

    @Test
    void cancelOrder_orderNotFound_returnsFail() {
        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(null);

        Result<Boolean> result = orderService.cancelOrder(ORDER_ID);

        assertFalse(result.isSuccess());
        assertEquals("Order not found", result.getError());

        verify(mockOrderRepository, never()).cancelOrder(anyString());
    }

    @Test
    void cancelOrder_orderNotActive_returnsFail() {
        Order completedOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        completedOrder.CompleteOrder();

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(completedOrder);

        Result<Boolean> result = orderService.cancelOrder(ORDER_ID);

        assertFalse(result.isSuccess());
        assertEquals("Order is not active", result.getError());

        verify(mockOrderRepository, never()).cancelOrder(anyString());
    }

    @Test
    void cancelOrder_eventNotFound_stillReturnsOkAfterCancelRepoCall() {
        Order seatOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(seatOrder);
        when(mockEventRepository.getEventByID(EVENT_ID)).thenReturn(null);

        Result<Boolean> result = orderService.cancelOrder(ORDER_ID);

        assertTrue(result.isSuccess());
        assertTrue(result.getValue());

        verify(mockOrderRepository).cancelOrder(ORDER_ID);
    }

    @Test
    void cancelOrder_venueNotFound_stillReturnsOkAfterCancelRepoCall() {
        Order seatOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(seatOrder);
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(null);

        Result<Boolean> result = orderService.cancelOrder(ORDER_ID);

        assertTrue(result.isSuccess());
        assertTrue(result.getValue());

        verify(mockOrderRepository).cancelOrder(ORDER_ID);
    }

    @Test
    void cancelOrder_segmentNotFound_stillReturnsOkAfterCancelRepoCall() {
        Order seatOrder = new Order(
                SEGMENT_ID,
                List.of("1-1", "1-2"),
                100.0,
                EVENT_ID,
                USER_ID_STRING
        );

        Venue venueWithoutSegment = new Venue(
                VENUE_ID,
                null,
                Map.of()
        );

        when(mockOrderRepository.getOrder(ORDER_ID)).thenReturn(seatOrder);
        when(mockVenueRepository.getVenueByID(VENUE_ID)).thenReturn(venueWithoutSegment);

        Result<Boolean> result = orderService.cancelOrder(ORDER_ID);

        assertTrue(result.isSuccess());
        assertTrue(result.getValue());

        verify(mockOrderRepository).cancelOrder(ORDER_ID);
    }
}
