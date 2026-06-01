package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
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
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Seat;
import com.group16b.DomainLayer.Venue.Venue;
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
        setUpTicketGatewayMock();

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
                testPCompany = new ProductionCompany(1, "Test Company", 4.5, "admin@test.com");

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
                ChosenSeatingSeg seg1 = new ChosenSeatingSeg("seatingSeg1", seats);
                FieldSeg seg2 = new FieldSeg("fieldSeg1", 100);
                Location location = new Location("Test Location", "123", "Test Street", "Test City", "Test State", "Test Country", 0.0, 0.0);
                testVenue = new Venue("Test Venue", location, Map.of("seatingSeg1", seg1, "fieldSeg1", seg2), "venue1");

                venueRepo.save(testVenue);
        }
        private void seedEvent() {
                EventRecord eventRecord = new EventRecord(testVenue.getID(), "Test Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), "Test Artist", "Test Category", testPCompany.getProductionCompanyID(), 50.0, 4.5);
                testEvent = new Event(eventRecord, "owner_1");

                eventRepo.save(testEvent);
        }
        private void seedOrders() {
                //Order(String segmentId, List<String> seats, double totalPrice, int eventId, String subjectID);
                //Order(String segmentId, int amount, double totalPrice, int eventId, String subjectID);
                seatOrder = new Order("seatingSeg1", List.of("A-1", "A-2"), 100.0, testEvent.getEventID(), testUser.getEmail());
                fieldOrder = new Order("fieldSeg1", 3, 150.0, testEvent.getEventID(), testUser.getEmail());

                testVenue.reserveSeats(ReservationRequest.forSeats(testEvent.getEventID(), List.of("A-1", "A-2"), "seg1"));
                testVenue.reserveSeats(ReservationRequest.forField(testEvent.getEventID(), 3, "seg2"));

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
        private void setUpTicketGatewayMock() {
        when(ticketGateway.generateTicket(
                anyInt(),
                anyString(),
                anyString(),
                any(),
                anyDouble()
        )).thenAnswer(invocation -> new TicketDTO(
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2),
                invocation.getArgument(3),
                invocation.getArgument(4)
        ));
}
        

        // _______________CompleteActiveOrder tests:_________________

        @Test
        void completeActiveOrder_validSeatOrder_completesOrderGeneratesTicketsAndKeepsReservation() {
                Result<List<TicketDTO>> result =
                        orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());

                assertTrue(result.isSuccess());
                assertEquals(2, result.getValue().size());
                assertOrderIsCompleted(seatOrder);

                verify(paymentGateway, times(1)).processPayment(any(), eq(100.0));
                verify(paymentGateway, never()).cancelPayment();
                verify(ticketGateway, times(2)).generateTicket(anyInt(), anyString(), anyString(), any(), anyDouble());
        }

        @Test
        void completeActiveOrder_validFieldOrder_completesOrderGeneratesTickets() {
                Result<List<TicketDTO>> result =
                        orderService.CompleteActiveOrder(fieldOrder.getOrderId(), "user1", validPaymentInfo());

                assertTrue(result.isSuccess());
                assertEquals(3, result.getValue().size());
                assertOrderIsCompleted(fieldOrder);

                verify(paymentGateway, times(1)).processPayment(any(), eq(150.0));
                verify(paymentGateway, never()).cancelPayment();
                verify(ticketGateway, times(3)).generateTicket(anyInt(), anyString(), anyString(), any(), anyDouble());
        }

        @Test
        void completeActiveOrder_invalidToken_failsAndDoesNotChangeOrder() {
                Result<List<TicketDTO>> result =
                        orderService.CompleteActiveOrder(seatOrder.getOrderId(), "invalid", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertOrderIsActive(seatOrder);

                verify(paymentGateway, never()).processPayment(any(), anyDouble());
                verify(ticketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), any(), anyDouble());
        }

        @Test
        void completeActiveOrder_adminToken_failsAndDoesNotChangeOrder() {
                Result<List<TicketDTO>> result =
                        orderService.CompleteActiveOrder(seatOrder.getOrderId(), "admin", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertOrderIsActive(seatOrder);

                verify(paymentGateway, never()).processPayment(any(), anyDouble());
                verify(ticketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), any(), anyDouble());
        }

        @Test
        void completeActiveOrder_orderNotFound_fails() {
                Result<List<TicketDTO>> result =
                        orderService.CompleteActiveOrder("missing-order-id", "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Invalid argument"));

                verify(paymentGateway, never()).processPayment(any(), anyDouble());
                verify(ticketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), any(), anyDouble());
        }

        @Test
        void completeActiveOrder_paymentFails_orderStaysActiveAndNoTicketsGenerated() {
                doThrow(new PaymentFailedException("card declined"))
                        .when(paymentGateway)
                        .processPayment(any(), anyDouble());

                Result<List<TicketDTO>> result =
                        orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Payment failed"));
                assertOrderIsActive(seatOrder);

                verify(paymentGateway, never()).cancelPayment();
                verify(ticketGateway, never()).generateTicket(anyInt(), anyString(), anyString(), any(), anyDouble());
        }

        @Test
        void completeActiveOrder_ticketGenerationFails_paymentCancelledAndOrderStaysActive() {
                when(ticketGateway.generateTicket(anyInt(), anyString(), anyString(), any(), anyDouble()))
                        .thenThrow(new TicketGenerationException("ticket system failed"));

                Result<List<TicketDTO>> result =
                        orderService.CompleteActiveOrder(seatOrder.getOrderId(), "user1", validPaymentInfo());

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Ticket generation failed"));
                assertOrderIsActive(seatOrder);

                verify(paymentGateway, times(1)).cancelPayment();
        }
//------
        @Test
        void completeActiveOrder_expiredOrder_failsCancelsOrderAndFreesReservation() {}

        @Test
        void completeActiveOrder_optimisticLockFailsOnce_retriesAndCompletesOrder() {}

        @Test
        void completeActiveOrder_optimisticLockFailsMaxRetries_paymentCancelledAndOrderStaysActive() {}


        private PaymentInfo validPaymentInfo() {
                return new PaymentInfo("4111111111111111", "Ran Test", "12/30", "123");
        }

        private void assertOrderIsActive(Order order) {
                assertTrue(orderRepo.findByID(order.getOrderId()).isActive());
        }

        private void assertOrderIsCompleted(Order order) {
                assertFalse(orderRepo.findByID(order.getOrderId()).isActive());
        }

        // _______________getUserOrders tests:_________________
        @Test
        void getUserOrders_validUser_returnsOnlyUserOrders() {}

        @Test
        void getUserOrders_invalidToken_returnsAuthFailure() {}

        @Test
        void getUserOrders_adminToken_returnsAuthFailure() {}

        @Test
        void getUserOrders_staleUser_returnsFailure() {}

        @Test
        void getUserOrders_userWithNoOrders_returnsEmptyList() {}


        // _______________ changeSeatsToOrder tests:_________________
        @Test
        void changeSeatsToOrder_validChange_updatesOrderSeatsAndPrice() {}

        @Test
        void changeSeatsToOrder_sameSeats_returnsOkWithoutChangingAnything() {}

        @Test
        void changeSeatsToOrder_nullSeats_failsAndDoesNotChangeOrder() {}

        @Test
        void changeSeatsToOrder_emptySeats_failsAndDoesNotChangeOrder() {}

        @Test
        void changeSeatsToOrder_invalidToken_failsAndDoesNotChangeOrder() {}

        @Test
        void changeSeatsToOrder_adminToken_failsAndDoesNotChangeOrder() {}

        @Test
        void changeSeatsToOrder_wrongUser_failsAndDoesNotChangeOrder() {}

        @Test
        void changeSeatsToOrder_orderNotFound_fails() {}

        @Test
        void changeSeatsToOrder_completedOrder_failsAndDoesNotChangeOrder() {}

        @Test
        void changeSeatsToOrder_fieldOrder_failsAndDoesNotChangeOrder() {}

        @Test
        void changeSeatsToOrder_requestedSeatAlreadyReserved_failsAndDoesNotChangeOrder() {}

        @Test
        void changeSeatsToOrder_purchasePolicyFails_failsAndDoesNotChangeOrderOrVenue() {}

        @Test
        void changeSeatsToOrder_discountPolicyApplied_updatesTotalPrice() {}

        // _______________changeNumOfSeatsInFieldOrder tests:_________________
        @Test
        void changeNumOfSeatsInFieldOrder_validIncrease_reservesMoreTicketsAndUpdatesOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_validDecrease_freesTicketsAndUpdatesOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_sameAmount_returnsOkWithoutChangingReservation() {}

        @Test
        void changeNumOfSeatsInFieldOrder_zeroAmount_failsAndDoesNotChangeOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_negativeAmount_failsAndDoesNotChangeOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_invalidToken_failsAndDoesNotChangeOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_adminToken_failsAndDoesNotChangeOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_wrongUser_failsAndDoesNotChangeOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_orderNotFound_fails() {}

        @Test
        void changeNumOfSeatsInFieldOrder_completedOrder_failsAndDoesNotChangeOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_seatOrder_failsAndDoesNotChangeOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_notEnoughFieldCapacity_failsAndDoesNotChangeOrder() {}

        @Test
        void changeNumOfSeatsInFieldOrder_purchasePolicyFails_failsAndDoesNotChangeOrderOrVenue() {}

        @Test
        void changeNumOfSeatsInFieldOrder_discountPolicyApplied_updatesTotalPrice() {}

        // _______________cancelOrder tests:_________________
        @Test
        void cancelOrder_validActiveSeatOrder_deletesOrderAndFreesSeats() {}

        @Test
        void cancelOrder_validActiveFieldOrder_deletesOrderAndFreesFieldTickets() {}

        @Test
        void cancelOrder_orderNotFound_returnsFail() {}

        @Test
        void cancelOrder_completedOrder_returnsFailAndDoesNotDeleteOrder() {}

        @Test
        void cancelOrder_eventNotFound_deletesOrderButReturnsOkOrFailAccordingToSpec() {}

        @Test
        void cancelOrder_venueNotFound_deletesOrderButReturnsOkOrFailAccordingToSpec() {}

        @Test
        void cancelOrder_segmentNotFound_deletesOrderButReturnsOkOrFailAccordingToSpec() {}


        // _______________ Lock tests:_________________ ?
        @Test
        void completeActiveOrder_optimisticLockConflict_retriesUntilSuccess() {}

        @Test
        void completeActiveOrder_pessimisticLock_blocksSecondUpdateUntilFirstFinishes() {}

        @Test
        void changeSeatsToOrder_optimisticLockConflict_retriesUntilSuccess() {}

        @Test
        void changeSeatsToOrder_pessimisticLock_blocksSecondUpdateUntilFirstFinishes() {}

        @Test
        void changeNumOfSeatsInFieldOrder_optimisticLockConflict_retriesUntilSuccess() {}

        @Test
        void changeNumOfSeatsInFieldOrder_pessimisticLock_blocksSecondUpdateUntilFirstFinishes() {}

        @Test
        void cancelOrder_optimisticLockConflict_retriesUntilSuccess() {}

        @Test
        void cancelOrder_pessimisticLock_blocksSecondUpdateUntilFirstFinishes() {}
}