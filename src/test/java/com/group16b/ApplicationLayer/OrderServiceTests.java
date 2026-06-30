/*
package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.ApplicationLayer.Exceptions.IllegalPaymentInfoException;
import com.group16b.ApplicationLayer.Exceptions.IllegalTicketInfoException;
import com.group16b.ApplicationLayer.Exceptions.IssueTicketStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Exceptions.PaymentStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.TicketGenerationException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchaseContext;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicyException;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.Entrance;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.GridRectangle;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Seat;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Stage;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Venue.VenueGrid;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;

public class OrderServiceTests {
        private OrderService orderService;
        private IAuthenticationService authService;
        private IPaymentGateway paymentGateway;
        private ITicketGateway ticketGateway;

        private IOrderRepository orderRepo;
        private IRepository<Venue> venueRepo;
        private IEventRepository eventRepo;
        private IRepository<User> userRepo;
        private IProductionCompanyRepository productionCompanyRepo;


        // example objects:
        private Venue testVenue;
        private User testUser;
        private User testAdmin;
        private Event testEvent;
        private ProductionCompany testPCompany;
        private Order seatOrder;
        private Order fieldOrder;

        private final int TRANSACTION_ID=12345;
        private final String SEATING_TICKET="amogus";
        private final String FILED_TIKET="me and my monkey";
        
        @BeforeEach
        void setUp() {
        authService = mock(IAuthenticationService.class);
        paymentGateway = mock(IPaymentGateway.class);
        ticketGateway = mock(ITicketGateway.class);

        orderRepo = new OrderRepositoryMapImpl();
        venueRepo = new VenueRepositoryMapImpl();
        eventRepo = new EventRepositoryMapImpl();
        userRepo = new UserRepositoryMapImpl();
        productionCompanyRepo = new ProductionCompanyRepositoryMapImpl();

        seedUsers();
        seedCompany();
        seedVenue();
        seedEvent();
        seedOrders();

        setUpAuthMocks();
        setUpPaymentMocks();
        setUpTicketMocks();

        orderService = new OrderService(
                authService,
                productionCompanyRepo,
                paymentGateway,
                venueRepo,
                eventRepo,
                userRepo,
                orderRepo,
                ticketGateway
        );
        }
        private void seedUsers() {
                testUser = new User("user1@test.com", "password");
                testAdmin = new User("admin@test.com", "password");

                userRepo.save(testUser);
                userRepo.save(testAdmin);
        }
        private void seedCompany() {
                testPCompany = ProductionCompany.createNewCompany("Test Company", testAdmin.getEmail(), 1);
                productionCompanyRepo.save(testPCompany);
        }
        private void seedVenue() {
                Map<String, Seat> seats = new HashMap<>();
                for (char row = 'A'; row <= 'C'; row++) {
                        for (int num = 1; num <= 5; num++) {
                                String seatId = row + "-" + num;
                                seats.put(seatId, new Seat(row, num));
                        }
                }
                ChosenSeatingSeg seatingSeg1 = new ChosenSeatingSeg("seatingSeg1", seats, new GridRectangle(1, 2, 3 , 4));
                FieldSeg fieldSeg1 = new FieldSeg("fieldSeg1", 100, new GridRectangle(6, 7, 8, 9));
                Location location = new Location("Test Location", "123", "Test Street", "Test City", "Test State", "Test Country", 0.0, 0.0);
                HashMap<String, Segment> segments = new HashMap<>();
                segments.put(seatingSeg1.getSegmentID(), seatingSeg1);
                segments.put(fieldSeg1.getSegmentID(), fieldSeg1);
                testVenue = new Venue("Test Venue", location, segments, "1", new VenueGrid(6, 7), new ConcurrentHashMap<String, Stage>(), new ConcurrentHashMap<String, Entrance>(),1);

                venueRepo.save(testVenue);
        }
        private void seedEvent() {
                EventRecord eventRecord = new EventRecord(testVenue.getID(), "Test Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), "Test Artist", "Test Category", testPCompany.getProductionCompanyID(), 4.5);
                
                testEvent = new Event(eventRecord, "owner_1");

                eventRepo.save(testEvent); 

                Venue venue = venueRepo.findByID(testVenue.getID());

                venue.bookEvent(eventRecord.startTime(), eventRecord.endTime(), testEvent.getEventID());

                testVenue = venue;
                venueRepo.save(venue);
        }
        private void seedOrders() {
                //Order(String segmentId, List<String> seats, double totalPrice, int eventId, String subjectID);
                //Order(String segmentId, int amount, double totalPrice, int eventId, String subjectID);
                seatOrder = new Order("seatingSeg1", List.of("A-1", "A-2"), 100.0, testEvent.getEventID(), testUser.getEmail());
                fieldOrder = new Order("fieldSeg1", 3, 150.0, testEvent.getEventID(), testUser.getEmail());

                Venue venue = venueRepo.findByID(testVenue.getID());
                venue.reserveSeats(ReservationRequest.forSeats(testEvent.getEventID(), List.of("A-1", "A-2"), "seatingSeg1"));
                venue.reserveSeats(ReservationRequest.forField(testEvent.getEventID(), 3, "fieldSeg1"));
                venueRepo.save(venue);

                orderRepo.save(seatOrder);
                orderRepo.save(fieldOrder);
        }
        private void setUpAuthMocks() {
                when(authService.validateToken("user1")).thenReturn(true);
                when(authService.extractSubjectFromToken("user1")).thenReturn(testUser.getEmail());
                when(authService.isAdminToken("user1")).thenReturn(false);
                when(authService.validateToken("invalid")).thenReturn(false);

                when(authService.validateToken("admin")).thenReturn(true);
                when(authService.extractSubjectFromToken("admin")).thenReturn(testAdmin.getEmail());
                when(authService.isAdminToken("admin")).thenReturn(true);
        }
        private void setUpPaymentMocks()
        {
                when(paymentGateway.processPayment(any(), anyDouble())).thenReturn(TRANSACTION_ID);
        }
        private void setUpTicketMocks()
        {
                when(ticketGateway.generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(), anyInt())).thenReturn(FILED_TIKET);
                when(ticketGateway.generateSeatingTicket(anyInt(), anyString(), anyString(), any())).thenReturn(SEATING_TICKET);
        }

        // _______________CompleteActiveOrder tests:_________________

        @Test
        void completeActiveOrder_validSeatOrder_completesOrderGeneratesTicketsAndKeepsReservation() {
                Result<String> result = orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());
                assertTrue(result.isSuccess());
                assertEquals(SEATING_TICKET, result.getValue());
                assertOrderIsCompleted(seatOrder);

                verify(paymentGateway, times(1)).processPayment(any(), eq(100.0));
                verify(paymentGateway, never()).cancelPayment(anyInt());
                verify(ticketGateway, times(1)).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_validFieldOrder_completesOrderGeneratesTickets() {
                Result<String> result = orderService.CompleteActiveOrder(fieldOrder.getOrderId(), "user1", validPaymentInfo());

                assertTrue(result.isSuccess());
                assertEquals(FILED_TIKET, result.getValue());
                assertOrderIsCompleted(fieldOrder);

                verify(paymentGateway, times(1)).processPayment(any(), eq(150.0));
                verify(paymentGateway, never()).cancelPayment(anyInt());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,times(1)).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_invalidPaymentInfo_failAndDontCharge() {
                doThrow(new IllegalPaymentInfoException("banana")).when(paymentGateway).processPayment(any(), anyDouble());
                Result<String> result = orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());
                assertFalse(result.isSuccess());
                assertEquals("Bad payment Info: banana",result.getError());
                assertOrderIsActive(seatOrder);

                verify(paymentGateway,times(1)).processPayment(any(), eq(100.0));
                verify(paymentGateway, never()).cancelPayment(anyInt());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_invalidTicketInfo_failAndRefund() {
                doThrow(new IllegalTicketInfoException("banana")).when(ticketGateway).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                Result<String> result = orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertEquals("Bad ticket Info: banana",result.getError());
                assertOrderIsActive(seatOrder);

                verify(paymentGateway,times(1)).processPayment(any(), eq(100.0));
                verify(paymentGateway, times(1)).cancelPayment(TRANSACTION_ID);
                verify(ticketGateway, times(1)).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_invalidToken_failsAndDoesNotChangeOrder() {
                Result<String> result =orderService.CompleteActiveOrder(seatOrder.getOrderId(), "invalid", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertOrderIsActive(seatOrder);

                verify(paymentGateway, never()).processPayment(any(), anyDouble());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_adminToken_failsAndDoesNotChangeOrder() {
                Result<String> result =orderService.CompleteActiveOrder(seatOrder.getOrderId(), "admin", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertOrderIsActive(seatOrder);

                verify(paymentGateway, never()).processPayment(any(), anyDouble());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_orderNotFound_fails() {
                Result<String> result =orderService.CompleteActiveOrder("missing-order-id", "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Invalid argument"));

                verify(paymentGateway, never()).processPayment(any(), anyDouble());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_paymentFails_orderStaysActiveAndNoTicketsGenerated() {
                doThrow(new PaymentFailedException("card declined"))
                        .when(paymentGateway)
                        .processPayment(any(), anyDouble());

                Result<String> result =
                        orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Payment failed"));
                assertOrderIsActive(seatOrder);

                verify(paymentGateway, never()).cancelPayment(anyInt());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_ticketGenerationFails_paymentCancelledAndOrderStaysActive() {
                when(ticketGateway.generateSeatingTicket(anyInt(), anyString(), anyString(), any()))
                        .thenThrow(new TicketGenerationException("ticket system failed"));

                Result<String> result =
                        orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Ticket generation failed"));
                assertOrderIsActive(seatOrder);

                verify(paymentGateway, times(1)).cancelPayment(anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }
//------
        @Test
void completeActiveOrder_orderBelongsToDifferentUser_failsAndDoesNotPay() {
        Order otherUserOrder = new Order(
                "seatingSeg1",
                List.of("A-3", "A-4"),
                100.0,
                testEvent.getEventID(),
                "other@test.com"
        );

        orderRepo.save(otherUserOrder);

        Result<String> result =
                orderService.CompleteActiveOrder(otherUserOrder.getOrderId(), "user1", validPaymentInfo());

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Invalid argument"));
        assertTrue(orderRepo.findByID(otherUserOrder.getOrderId()).isActive());

        verify(paymentGateway, never()).processPayment(any(), anyDouble());
        verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
        verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
        verify(ticketGateway,never()).revokeTicket(anyString());
}

        @Test
        void completeActiveOrder_completedOrder_failsAndDoesNotPay() {
                Order order = orderRepo.findByID(seatOrder.getOrderId());
                order.CompleteOrder();
                orderRepo.save(order);

                Result<String> result = orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Illegal state"));

                verify(paymentGateway, never()).processPayment(any(), anyDouble());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_paymentStatusUnknown_failsWithoutRollback() {
                doThrow(new PaymentStatusUnknownException("provider timeout")).when(paymentGateway).processPayment(any(), anyDouble());

                Result<String> result =orderService.CompleteActiveOrder(seatOrder.getOrderId(),"user1",validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Payment could not be verified"));

                assertOrderIsActive(seatOrder);

                verify(paymentGateway, never()).cancelPayment(anyInt());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway, never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(), anyInt());
                verify(ticketGateway, never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_ticketStatusUnknown_failsWithoutRollback() {
                doThrow(new IssueTicketStatusUnknownException("provider timeout")).when(ticketGateway).generateSeatingTicket(anyInt(), anyString(), anyString(), any());

                Result<String> result =orderService.CompleteActiveOrder(seatOrder.getOrderId(),"user1",validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Ticket could not be verified"));

                assertOrderIsActive(seatOrder);

                verify(paymentGateway, never()).cancelPayment(anyInt());
                verify(ticketGateway, never()).revokeTicket(anyString());
        }
        @Test
        void completeActiveOrder_ticketGenerationFails_refundFails_stillReturnsFailure() {
                doThrow(new TicketGenerationException("ticket failed")).when(ticketGateway).generateSeatingTicket(anyInt(), anyString(), anyString(), any());

                doThrow(new RuntimeException("refund failed")).when(paymentGateway).cancelPayment(anyInt());

                Result<String> result =orderService.CompleteActiveOrder(seatOrder.getOrderId(),"user1",validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Ticket generation failed"));

                verify(paymentGateway).cancelPayment(anyInt());
        }

        @Test
        void completeActiveOrder_expiredOrder_failsCancelsOrderAndFreesReservation() {
                Order expiredOrder = mock(Order.class);

                when(expiredOrder.getOrderId()).thenReturn("expired-order");
                doThrow(new com.group16b.ApplicationLayer.Exceptions.OrderExpiredException("expired"))
                        .when(expiredOrder)
                        .validiteOrderIsActive();

                IOrderRepository mockOrderRepo = mock(IOrderRepository.class);
                when(mockOrderRepo.findByID("expired-order")).thenReturn(expiredOrder);

                OrderService service = new OrderService(
                        authService,
                        productionCompanyRepo,
                        paymentGateway,
                        venueRepo,
                        eventRepo,
                        userRepo,
                        mockOrderRepo,
                        ticketGateway
                );

                Result<String> result =
                        service.CompleteActiveOrder("expired-order", "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Order expired"));

                verify(paymentGateway, never()).processPayment(any(), anyDouble());
                verify(paymentGateway, never()).cancelPayment(anyInt());
                verify(ticketGateway, never()).generateSeatingTicket(anyInt(), anyString(), anyString(), any());
                verify(ticketGateway,never()).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(),anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
                verify(mockOrderRepo, times(1)).delete("expired-order");
        }

        @Test
        void completeActiveOrder_optimisticLockFailsOnce_retriesAndCompletesOrder() {
                IOrderRepository mockOrderRepo = mock(IOrderRepository.class);

                Order firstOrder = mock(Order.class);
                Order retryOrder1 = mock(Order.class);
                Order retryOrder2 = mock(Order.class);

                when(firstOrder.getOrderId()).thenReturn("lock-order");
                when(firstOrder.getEventId()).thenReturn(testEvent.getEventID());
                when(firstOrder.getSegmentId()).thenReturn("seatingSeg1");
                when(firstOrder.getSeats()).thenReturn(List.of("A-1", "A-2"));
                when(firstOrder.getNumOfTickets()).thenReturn(2);
                when(firstOrder.getTotalOrderprice()).thenReturn(100.0);

                when(mockOrderRepo.findByID("lock-order"))
                        .thenReturn(firstOrder)
                        .thenReturn(retryOrder1)
                        .thenReturn(retryOrder2);

                when(retryOrder1.CompleteOrder())
                        .thenThrow(new OptimisticLockingFailureException("conflict"));

                when(retryOrder2.CompleteOrder())
                        .thenReturn(true);

                OrderService service = new OrderService(
                        authService,
                        productionCompanyRepo,
                        paymentGateway,
                        venueRepo,
                        eventRepo,
                        userRepo,
                        mockOrderRepo,
                        ticketGateway
                );

                Result<String> result =
                        service.CompleteActiveOrder("lock-order", "user1", validPaymentInfo());

                assertTrue(result.isSuccess());
                assertEquals(FILED_TIKET, result.getValue());

                verify(paymentGateway, times(1)).processPayment(any(), eq(100.0));
                verify(paymentGateway, never()).cancelPayment(anyInt());
                verify(ticketGateway, times(1)).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(), anyInt());
                verify(mockOrderRepo, times(3)).findByID("lock-order");
                verify(retryOrder1, times(1)).CompleteOrder();
                verify(retryOrder2, times(1)).CompleteOrder();
                verify(paymentGateway,never()).cancelPayment(anyInt());
                verify(ticketGateway,never()).revokeTicket(anyString());
        }

        @Test
        void completeActiveOrder_optimisticLockFailsMaxRetries_paymentCancelledAndOrderStaysActive() {
                IOrderRepository mockOrderRepo = mock(IOrderRepository.class);

                Order firstOrder = mock(Order.class);
                Order retryOrder1 = mock(Order.class);
                Order retryOrder2 = mock(Order.class);
                Order retryOrder3 = mock(Order.class);

                when(firstOrder.getOrderId()).thenReturn("lock-fail-order");
                when(firstOrder.getEventId()).thenReturn(testEvent.getEventID());
                when(firstOrder.getSegmentId()).thenReturn("seatingSeg1");
                when(firstOrder.getSeats()).thenReturn(List.of("A-1", "A-2"));
                when(firstOrder.getNumOfTickets()).thenReturn(2);
                when(firstOrder.getTotalOrderprice()).thenReturn(100.0);

                when(mockOrderRepo.findByID("lock-fail-order"))
                        .thenReturn(firstOrder)
                        .thenReturn(retryOrder1)
                        .thenReturn(retryOrder2)
                        .thenReturn(retryOrder3);

                doThrow(new org.springframework.dao.OptimisticLockingFailureException("conflict"))
                        .when(retryOrder1)
                        .CompleteOrder();

                doThrow(new org.springframework.dao.OptimisticLockingFailureException("conflict"))
                        .when(retryOrder2)
                        .CompleteOrder();

                doThrow(new org.springframework.dao.OptimisticLockingFailureException("conflict"))
                        .when(retryOrder3)
                        .CompleteOrder();

                OrderService service = new OrderService(
                        authService,
                        productionCompanyRepo,
                        paymentGateway,
                        venueRepo,
                        eventRepo,
                        userRepo,
                        mockOrderRepo,
                        ticketGateway
                );

                Result<String> result =
                        service.CompleteActiveOrder("lock-fail-order", "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("concurrent update"));

                verify(paymentGateway, times(1)).processPayment(any(), eq(100.0));
                verify(paymentGateway, times(1)).cancelPayment(anyInt());
                verify(ticketGateway, times(1)).generateGeneralAdmissionTicket(anyInt(), anyString(), anyString(), anyInt());
                verify(mockOrderRepo, times(4)).findByID("lock-fail-order");
                verify(ticketGateway,times(1)).revokeTicket(anyString());
        }
        
        @Test
void completeActiveOrder_twoThreadsSameOrder_onlyOneCompletesSuccessfully() throws Exception {
    CountDownLatch readyLatch = new CountDownLatch(2);
    CountDownLatch startLatch = new CountDownLatch(1);

    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
        Future<Result<String>> firstAttempt = executor.submit(() -> {
            readyLatch.countDown();
            startLatch.await();
            return orderService.CompleteActiveOrder(
                    seatOrder.getOrderId(),
                    "user1",
                    validPaymentInfo());
        });

        Future<Result<String>> secondAttempt = executor.submit(() -> {
            readyLatch.countDown();
            startLatch.await();
            return orderService.CompleteActiveOrder(
                    seatOrder.getOrderId(),
                    "user1",
                    validPaymentInfo());
        });

        readyLatch.await();
        startLatch.countDown();

        Result<String> firstResult = firstAttempt.get();
        Result<String> secondResult = secondAttempt.get();

        int successCount = 0;
        int failureCount = 0;
        String failureMessage = "";

        if (firstResult.isSuccess()) {
            successCount++;
            assertEquals(SEATING_TICKET, firstResult.getValue());
        } else {
            failureCount++;
            failureMessage = firstResult.getError();
        }

        if (secondResult.isSuccess()) {
            successCount++;
            assertEquals(SEATING_TICKET, secondResult.getValue());
        } else {
            failureCount++;
            failureMessage = secondResult.getError();
        }

        assertEquals(1, successCount);
        assertEquals(1, failureCount);

        assertOrderIsCompleted(seatOrder);

        verify(paymentGateway, atLeastOnce())
                .processPayment(any(), eq(100.0));

        verify(paymentGateway, atMost(2))
                .processPayment(any(), eq(100.0));

        verify(ticketGateway, atLeastOnce())
                .generateSeatingTicket(
                        anyInt(),
                        anyString(),
                        anyString(),
                        anyList());

        verify(ticketGateway, atMost(2))
                .generateSeatingTicket(
                        anyInt(),
                        anyString(),
                        anyString(),
                        anyList());

        verify(ticketGateway, never())
                .generateGeneralAdmissionTicket(
                        anyInt(),
                        anyString(),
                        anyString(),
                        anyInt());

        verify(paymentGateway, atMostOnce())
                .cancelPayment(TRANSACTION_ID);

        verify(ticketGateway, atMostOnce())
                .revokeTicket(SEATING_TICKET);
    } finally {
        executor.shutdownNow();
    }
}

        private PaymentInfo validPaymentInfo() {
                return new PaymentInfo("shekel","12345678910",1,2026,"moshe rabenu","012","215000000");
        }

        private void assertOrderIsActive(Order order) {
                assertTrue(orderRepo.findByID(order.getOrderId()).isActive());
        }

        private void assertOrderIsCompleted(Order order) {
                assertTrue(orderRepo.findByID(order.getOrderId()).isCompleted());
        }

        


        // _______________ changeSeatsToOrder tests:_________________
        @Test
        void changeSeatsToOrder_validChange_updatesOrderSeatsAndPrice() {
                Result<List<String>> result = orderService.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-3", "A-4"));

                String error = result.getError();
                assertTrue(result.isSuccess());
                assertEquals(List.of("A-3", "A-4"), result.getValue());
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-3", "A-4"));
        }

        @Test
        void changeSeatsToOrder_partialOverlap_reservesOnlyNewSeatsAndFreesOnlyRemovedSeats() {
                Result<List<String>> result =
                        orderService.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-2", "A-3"));

                assertTrue(result.isSuccess());
                assertEquals(List.of("A-2", "A-3"), result.getValue());
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-2", "A-3"));
        }

        @Test
        void changeSeatsToOrder_sameSeats_returnsOkWithoutChangingAnything() {
                Result<List<String>> result =
                        orderService.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-1", "A-2"));

                assertTrue(result.isSuccess());
                assertEquals(List.of("A-1", "A-2"), result.getValue());
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        @Test
        void changeSeatsToOrder_nullSeats_failsAndDoesNotChangeOrder() {
                Result<List<String>> result =
                        orderService.changeSeatsToOrder(seatOrder.getOrderId(), "user1", null);

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("cannot be null or empty"));
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        @Test
        void changeSeatsToOrder_emptySeats_failsAndDoesNotChangeOrder() {
                Result<List<String>> result =
                        orderService.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("cannot be null or empty"));
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        @Test
        void changeSeatsToOrder_invalidToken_failsAndDoesNotChangeOrder() {
                Result<List<String>> result =
                        orderService.changeSeatsToOrder(seatOrder.getOrderId(), "invalid", List.of("A-3", "A-4"));

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        @Test
        void changeSeatsToOrder_adminToken_failsAndDoesNotChangeOrder() {
                Result<List<String>> result =
                        orderService.changeSeatsToOrder(seatOrder.getOrderId(), "admin", List.of("A-3", "A-4"));

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        @Test
        void changeSeatsToOrder_wrongUser_failsAndDoesNotChangeOrder() {
                Order otherUserOrder = new Order(
                        "seatingSeg1",
                        List.of("A-3", "A-4"),
                        100.0,
                        testEvent.getEventID(),
                        "other@test.com"
                );

                orderRepo.save(otherUserOrder);

                Result<List<String>> result =
                        orderService.changeSeatsToOrder(otherUserOrder.getOrderId(), "user1", List.of("A-5", "B-1"));

                assertFalse(result.isSuccess());
                assertOrderSeats(otherUserOrder.getOrderId(), List.of("A-3", "A-4"));
        }

        @Test
        void changeSeatsToOrder_orderNotFound_fails() {
                Result<List<String>> result =
                        orderService.changeSeatsToOrder("missing-order-id", "user1", List.of("A-3", "A-4"));

                assertFalse(result.isSuccess());
        }

        @Test
        void changeSeatsToOrder_completedOrder_failsAndDoesNotChangeOrder() {
                Order order = orderRepo.findByID(seatOrder.getOrderId());
                order.CompleteOrder();
                orderRepo.save(order);

                Result<List<String>> result =
                        orderService.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-3", "A-4"));

                assertFalse(result.isSuccess());
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        @Test
        void changeSeatsToOrder_fieldOrder_failsAndDoesNotChangeOrder() {
                Result<List<String>> result =
                        orderService.changeSeatsToOrder(fieldOrder.getOrderId(), "user1", List.of("A-3", "A-4"));

                assertFalse(result.isSuccess());
        }

        @Test
        void changeSeatsToOrder_requestedSeatAlreadyReserved_failsAndDoesNotChangeOrder() {
                Venue venue = venueRepo.findByID(testVenue.getID());
                venue.reserveSeats(ReservationRequest.forSeats(testEvent.getEventID(), List.of("A-3"), "seatingSeg1"));
                venueRepo.save(venue);

                Result<List<String>> result =
                        orderService.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-2", "A-3"));

                assertFalse(result.isSuccess());
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        @Test
        void changeSeatsToOrder_eventNotFound_failsAndDoesNotChangeOrder() {
                IEventRepository mockEventRepo = mock(IEventRepository.class);
                when(mockEventRepo.findByID(String.valueOf(testEvent.getEventID())))
                        .thenThrow(new IllegalArgumentException("Event not found"));

                OrderService service = new OrderService(
                        authService,
                        productionCompanyRepo,
                        paymentGateway,
                        venueRepo,
                        mockEventRepo,
                        userRepo,
                        orderRepo,
                        ticketGateway
                );

                Result<List<String>> result =
                        service.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-3", "A-4"));

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Event not found"));
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        @Test
        void changeSeatsToOrder_venueNotFound_failsAndDoesNotChangeOrder() {
                IRepository<Venue> mockVenueRepo = mock(IRepository.class);
                when(mockVenueRepo.findByID(testVenue.getID()))
                        .thenThrow(new IllegalArgumentException("Venue not found"));

                OrderService service = new OrderService(
                        authService,
                        productionCompanyRepo,
                        paymentGateway,
                        mockVenueRepo,
                        eventRepo,
                        userRepo,
                        orderRepo,
                        ticketGateway
                );

                Result<List<String>> result =
                        service.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-3", "A-4"));

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Venue not found"));
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }

        private void assertOrderSeats(String orderId, List<String> expectedSeats) {
                Order order = orderRepo.findByID(orderId);
                assertEquals(expectedSeats, order.getSeats());
        }

        // _______________changeNumOfSeatsInFieldOrder tests:_________________
        @Test
        void changeNumOfSeatsInFieldOrder_validIncrease_reservesMoreTicketsAndUpdatesOrder() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "user1", 5);

                assertTrue(result.isSuccess());
                assertEquals(5, result.getValue());
                assertOrderTicketAmount(fieldOrder.getOrderId(), 5);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_validDecrease_freesTicketsAndUpdatesOrder() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "user1", 2);

                assertTrue(result.isSuccess());
                assertEquals(2, result.getValue());
                assertOrderTicketAmount(fieldOrder.getOrderId(), 2);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_sameAmount_returnsOkWithoutChangingReservation() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "user1", 3);

                assertTrue(result.isSuccess());
                assertEquals(3, result.getValue());
                assertOrderTicketAmount(fieldOrder.getOrderId(), 3);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_zeroAmount_failsAndDoesNotChangeOrder() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "user1", 0);

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("greater than zero"));
                assertOrderTicketAmount(fieldOrder.getOrderId(), 3);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_negativeAmount_failsAndDoesNotChangeOrder() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "user1", -1);

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("greater than zero"));
                assertOrderTicketAmount(fieldOrder.getOrderId(), 3);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_invalidToken_failsAndDoesNotChangeOrder() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "invalid", 5);

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertOrderTicketAmount(fieldOrder.getOrderId(), 3);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_adminToken_failsAndDoesNotChangeOrder() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "admin", 5);

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertOrderTicketAmount(fieldOrder.getOrderId(), 3);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_wrongUser_failsAndDoesNotChangeOrder() {
                Order otherUserFieldOrder = new Order(
                        "fieldSeg1",
                        4,
                        200.0,
                        testEvent.getEventID(),
                        "other@test.com"
                );

                orderRepo.save(otherUserFieldOrder);

                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(otherUserFieldOrder.getOrderId(), "user1", 5);

                assertFalse(result.isSuccess());
                assertOrderTicketAmount(otherUserFieldOrder.getOrderId(), 4);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_orderNotFound_fails() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder("missing-order-id", "user1", 5);

                assertFalse(result.isSuccess());
        }

        @Test
        void changeNumOfSeatsInFieldOrder_completedOrder_failsAndDoesNotChangeOrder() {
                Order order = orderRepo.findByID(fieldOrder.getOrderId());
                order.CompleteOrder();
                orderRepo.save(order);

                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "user1", 5);

                assertFalse(result.isSuccess());
                assertOrderTicketAmount(fieldOrder.getOrderId(), 3);
        }

        @Test
        void changeNumOfSeatsInFieldOrder_seatOrder_failsAndDoesNotChangeOrder() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(seatOrder.getOrderId(), "user1", 5);

                assertFalse(result.isSuccess());
                assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
        }
        @Test
        void changeNumOfSeatsInFieldOrder_notEnoughFieldCapacity_failsAndDoesNotChangeOrder() {
                Result<Integer> result =
                        orderService.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "user1", 101);

                assertFalse(result.isSuccess());
                assertOrderTicketAmount(fieldOrder.getOrderId(), 3);
        }

        private void assertOrderTicketAmount(String orderId, int expectedAmount) {
                Order order = orderRepo.findByID(orderId);
                assertEquals(expectedAmount, order.getNumOfTickets());
        }

        // _______________cancelOrder tests:_________________

        @Test
        void cancelOrder_validActiveSeatOrder_deletesOrderAndFreesSeats() {
                String orderId = seatOrder.getOrderId();

                Result<Boolean> result = orderService.cancelOrder(orderId, "user1");

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());
                assertOrderDoesNotExist(orderId);
                assertSeatsCanBeReservedAgain(List.of("A-1", "A-2"), "seatingSeg1");
        }

        @Test
        void cancelOrder_validActiveFieldOrder_deletesOrderAndFreesFieldTickets() {
                String orderId = fieldOrder.getOrderId();

                Result<Boolean> result = orderService.cancelOrder(orderId, "user1");

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());
                assertOrderDoesNotExist(orderId);
                assertFieldTicketsCanBeReservedAgain(100, "fieldSeg1");
        }

        @Test
        void cancelOrder_orderNotFound_returnsFail() {
                Result<Boolean> result = orderService.cancelOrder("missing-order-id", "user1");

                assertFalse(result.isSuccess());
                assertEquals("Order with ID missing-order-id not found", result.getError());
        }

        @Test
        void cancelOrder_completedOrder_returnsFailAndDoesNotDeleteOrder() {
                Order order = orderRepo.findByID(seatOrder.getOrderId());
                order.CompleteOrder();
                orderRepo.save(order);

                Result<Boolean> result = orderService.cancelOrder(seatOrder.getOrderId(), "user1");

                assertFalse(result.isSuccess());
                assertEquals("Order " + seatOrder.getOrderId() + " is not active", result.getError());
                assertOrderIsCompleted(seatOrder);
        }

        @Test
        void cancelOrder_eventNotFound_deletesOrderButReturnsOkOrFailAccordingToSpec() {
                IEventRepository mockEventRepo = mock(IEventRepository.class);
                when(mockEventRepo.findByID(String.valueOf(testEvent.getEventID())))
                        .thenThrow(new IllegalArgumentException("Event not found"));

                OrderService service = new OrderService(
                        authService,
                        productionCompanyRepo,
                        paymentGateway,
                        venueRepo,
                        mockEventRepo,
                        userRepo,
                        orderRepo,
                        ticketGateway
                );

                String orderId = seatOrder.getOrderId();

                Result<Boolean> result = service.cancelOrder(orderId, "user1");

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());
                assertOrderDoesNotExist(orderId);
        }

        @Test
        void cancelOrder_venueNotFound_deletesOrderButReturnsOkOrFailAccordingToSpec() {
                IRepository<Venue> mockVenueRepo = mock(IRepository.class);
                when(mockVenueRepo.findByID(testVenue.getID()))
                        .thenThrow(new IllegalArgumentException("Venue not found"));

                OrderService service = new OrderService(
                        authService,
                        productionCompanyRepo,
                        paymentGateway,
                        mockVenueRepo,
                        eventRepo,
                        userRepo,
                        orderRepo,
                        ticketGateway
                );

                String orderId = seatOrder.getOrderId();

                Result<Boolean> result = service.cancelOrder(orderId, "user1");

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());
                assertOrderDoesNotExist(orderId);
        }

        @Test
        void cancelOrder_segmentNotFound_deletesOrderButReturnsOkOrFailAccordingToSpec() {
                Order badSegmentOrder = new Order(
                        "missingSeg",
                        List.of("A-3"),
                        50.0,
                        testEvent.getEventID(),
                        testUser.getEmail()
                );

                orderRepo.save(badSegmentOrder);

                Result<Boolean> result = orderService.cancelOrder(badSegmentOrder.getOrderId(), "user1");

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());
                assertOrderDoesNotExist(badSegmentOrder.getOrderId());
        }

        @Test
        void cancelOrder_invalidToken_returnsFailAndDoesNotDeleteOrder() {
                Result<Boolean> result = orderService.cancelOrder(seatOrder.getOrderId(), "invalid");

                assertFalse(result.isSuccess());
                assertEquals("Authentication failed: Invalid Token", result.getError());
                assertOrderIsActive(seatOrder);
        }

        @Test
        void cancelOrder_adminToken_returnsFailAndDoesNotDeleteOrder() {
                Result<Boolean> result = orderService.cancelOrder(seatOrder.getOrderId(), "admin");

                assertFalse(result.isSuccess());
                assertEquals("Authentication failed: Admins are not allowed to perform operation", result.getError());
                assertOrderIsActive(seatOrder);
        }

        @Test
        void cancelOrder_orderBelongsToDifferentUser_shouldFailAndNotDeleteOrder() {
                Order otherUserOrder = new Order(
                        "seatingSeg1",
                        List.of("A-3", "A-4"),
                        100.0,
                        testEvent.getEventID(),
                        "other@test.com"
                );

                orderRepo.save(otherUserOrder);

                Result<Boolean> result = orderService.cancelOrder(otherUserOrder.getOrderId(), "user1");

                assertFalse(result.isSuccess());
                assertEquals("Order " + otherUserOrder.getOrderId() + " does not belong to subject user1@test.com", result.getError());
                assertTrue(orderRepo.findByID(otherUserOrder.getOrderId()).isActive());
        }

        @Test
        void cancelOrder_nullOrderId_returnsFailAndDoesNotDeleteOrder() {
                Result<Boolean> result = orderService.cancelOrder(null, "user1");

                assertFalse(result.isSuccess());
                assertOrderIsActive(seatOrder);
                assertOrderIsActive(fieldOrder);
        }

        @Test
        void cancelOrder_nullToken_returnsFailAndDoesNotDeleteOrder() {
                Result<Boolean> result = orderService.cancelOrder(seatOrder.getOrderId(), null);

                assertFalse(result.isSuccess());
                assertEquals("Authentication failed: Invalid Token", result.getError());
                assertOrderIsActive(seatOrder);
        }
        private void assertOrderDoesNotExist(String orderId) {
                try {
                        orderRepo.findByID(orderId);
                        assertTrue(false, "Expected order to be deleted, but it still exists.");
                } catch (Exception e) {
                        assertTrue(true);
                }
        }

        private void assertSeatsCanBeReservedAgain(List<String> seats, String segmentId) {
                Venue venue = venueRepo.findByID(testVenue.getID());

                venue.reserveSeats(
                        ReservationRequest.forSeats(testEvent.getEventID(), seats, segmentId)
                );

                venueRepo.save(venue);
        }

        private void assertFieldTicketsCanBeReservedAgain(int amount, String segmentId) {
                Venue venue = venueRepo.findByID(testVenue.getID());

                venue.reserveSeats(
                        ReservationRequest.forField(testEvent.getEventID(), amount, segmentId)
                );

                venueRepo.save(venue);
        }


        // _______________ getOrderPrice tests:_________________

        @Test
        void getOrderPrice_validSeatOrder_returnsPriceAfterDiscountPolicy() {
                Result<Double> result = orderService.getOrderPrice(seatOrder.getOrderId(), "user1");

                assertTrue(result.isSuccess());
                assertEquals(100.0, result.getValue());
        }

        @Test
        void getOrderPrice_validFieldOrder_returnsPriceAfterDiscountPolicy() {
                Result<Double> result =
                        orderService.getOrderPrice(fieldOrder.getOrderId(), "user1");

                assertTrue(result.isSuccess());
                assertEquals(150.0, result.getValue());
        }

        @Test
        void getOrderPrice_invalidToken_fails() {
                Result<Double> result =
                        orderService.getOrderPrice(seatOrder.getOrderId(), "invalid");

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
        }

        @Test
        void getOrderPrice_adminToken_fails() {
                Result<Double> result =
                        orderService.getOrderPrice(seatOrder.getOrderId(), "admin");

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
        }

        @Test
        void getOrderPrice_orderNotFound_fails() {
                Result<Double> result =
                        orderService.getOrderPrice("missing-order-id", "user1");

                assertFalse(result.isSuccess());
        }

        @Test
        void getOrderPrice_orderBelongsToDifferentUser_fails() {
                Order otherUserOrder = new Order(
                        "seatingSeg1",
                        List.of("A-3", "A-4"),
                        100.0,
                        testEvent.getEventID(),
                        "other@test.com"
                );

                orderRepo.save(otherUserOrder);

                Result<Double> result =
                        orderService.getOrderPrice(otherUserOrder.getOrderId(), "user1");

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("does not belong to subject"));
        }

        @Test
        void getOrderPrice_nullOrderId_fails() {
                Result<Double> result =
                        orderService.getOrderPrice(null, "user1");

                assertFalse(result.isSuccess());
        }

        @Test
        void getOrderPrice_nullToken_fails() {
                Result<Double> result =
                        orderService.getOrderPrice(seatOrder.getOrderId(), null);

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
        }

        @Test
        void changeNumOfSeatsInFieldOrder_validIncrease_savesVenueAfterCapacityChange() {
        IRepository<Venue> mockVenueRepo = mock(IRepository.class);
        when(mockVenueRepo.findByID(testVenue.getID())).thenReturn(testVenue);

        OrderService service = new OrderService(
                authService,
                productionCompanyRepo,
                paymentGateway,
                mockVenueRepo,
                eventRepo,
                userRepo,
                orderRepo,
                ticketGateway
        );

        Result<Integer> result =
                service.changeNumOfSeatsInFieldOrder(fieldOrder.getOrderId(), "user1", 5);

        assertTrue(result.isSuccess());

        verify(mockVenueRepo, times(1)).save(testVenue);
}

        @Test
        void changeSeatsToOrder_policyFailsAfterReservingNewSeats_rollsBackNewReservation() {
        Event mockEvent = mock(Event.class);
        when(mockEvent.getEventID()).thenReturn(testEvent.getEventID());
        when(mockEvent.getEventVenueID()).thenReturn(testVenue.getID());
        when(mockEvent.getEventProductionCompanyID()).thenReturn(testPCompany.getProductionCompanyID());

        PurchasePolicy failingPolicy = mock(PurchasePolicy.class);
        doThrow(new PurchasePolicyException("blocked"))
                .when(failingPolicy)
                .validatePurchase(any(PurchaseContext.class));

        when(mockEvent.getEventPurchasePolicy()).thenReturn(new HashSet<>(Set.of(failingPolicy)));

        IEventRepository mockEventRepo = mock(IEventRepository.class);
        when(mockEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(mockEvent);

        OrderService service = new OrderService(
                authService,
                productionCompanyRepo,
                paymentGateway,
                venueRepo,
                mockEventRepo,
                userRepo,
                orderRepo,
                ticketGateway
        );

        Result<List<String>> result =
                service.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-3", "A-4"));

        assertFalse(result.isSuccess());

        assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));

        assertDoesNotThrow(() ->
                assertSeatsCanBeReservedAgain(List.of("A-3", "A-4"), "seatingSeg1")
        );
}

        @Test
        void getOrderPrice_eventDiscountPolicyNull_companyDiscountPolicyExists_doesNotCrash() {
        DiscountPolicy discountPolicy = mock(DiscountPolicy.class);
        when(discountPolicy.calculateDiscount(anyDouble())).thenAnswer(inv -> inv.getArgument(0));

        ProductionCompany mockCompany = mock(ProductionCompany.class);
        when(mockCompany.getDiscountPolicy()).thenReturn(new HashSet<>(Set.of(discountPolicy)));

        IProductionCompanyRepository mockCompanyRepo = mock(IProductionCompanyRepository.class);
        when(mockCompanyRepo.findByID(String.valueOf(testPCompany.getProductionCompanyID())))
                .thenReturn(mockCompany);

        Event mockEvent = mock(Event.class);
        when(mockEvent.getEventID()).thenReturn(testEvent.getEventID());
        when(mockEvent.getEventProductionCompanyID()).thenReturn(testPCompany.getProductionCompanyID());
        when(mockEvent.getEventDiscountPolicy()).thenReturn(null);

        IEventRepository mockEventRepo = mock(IEventRepository.class);
        when(mockEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(mockEvent);

        OrderService service = new OrderService(
                authService,
                mockCompanyRepo,
                paymentGateway,
                venueRepo,
                mockEventRepo,
                userRepo,
                orderRepo,
                ticketGateway
        );

        Result<Double> result = service.getOrderPrice(seatOrder.getOrderId(), "user1");

        assertTrue(result.isSuccess());
}

        @Test
        void changeSeatsToOrder_duplicateSeatIds_failsAndDoesNotChangeOrder() {
        Result<List<String>> result =
                orderService.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-3", "A-3"));

        assertFalse(result.isSuccess());
        assertOrderSeats(seatOrder.getOrderId(), List.of("A-1", "A-2"));
}

        @Test
        void getOrderPrice_seatOrder_returnsStoredTotalPriceNotTotalTimesAmount() {
        Result<Double> result = orderService.getOrderPrice(seatOrder.getOrderId(), "user1");

        assertTrue(result.isSuccess());

        assertEquals(100.0, result.getValue());
}

        @Test
        void getOrderPrice_fieldOrder_returnsStoredTotalPriceNotTotalTimesAmount() {
                Result<Double> result = orderService.getOrderPrice(fieldOrder.getOrderId(), "user1");

                assertTrue(result.isSuccess());

                assertEquals(150.0, result.getValue());
        }

        @Test
        void changeSeatsToOrder_orderSaveFails_rollsBackVenueChanges() {
        IOrderRepository mockOrderRepo = mock(IOrderRepository.class);
        when(mockOrderRepo.findByID(seatOrder.getOrderId())).thenReturn(seatOrder);

        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(mockOrderRepo)
                .save(any(Order.class));

        OrderService service = new OrderService(
                authService,
                productionCompanyRepo,
                paymentGateway,
                venueRepo,
                eventRepo,
                userRepo,
                mockOrderRepo,
                ticketGateway
        );

        Result<List<String>> result =
                service.changeSeatsToOrder(seatOrder.getOrderId(), "user1", List.of("A-3", "A-4"));

        assertFalse(result.isSuccess());

        assertDoesNotThrow(() ->
                assertSeatsCanBeReservedAgain(List.of("A-3", "A-4"), "seatingSeg1")
        );
}



}


 */