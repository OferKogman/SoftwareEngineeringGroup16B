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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
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
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;




public class ReserveServiceTest {
        private OrderService orderService;
        private ReserveService reserveService;
        private IRepository<VirtualQueue> queueRepo;
        private VirtualQueue queue;
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

                queueRepo = mock(IRepository.class);
                queue = mock(VirtualQueue.class);

                when(queueRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(queue);

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

                reserveService = new ReserveService(
                        authService,
                        productionCompanyRepo,
                        queueRepo,
                        venueRepo,
                        eventRepo,
                        orderRepo,
                        userRepo
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
                EventRecord eventRecord = new EventRecord(testVenue.getID(), "Test Event", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), "Test Artist", "Test Category", testPCompany.getProductionCompanyID(), 50.0, 4.5);
                
                testEvent = new Event(eventRecord, "owner_1");
                Venue venue = venueRepo.findByID(testVenue.getID());
                venue.bookEvent(eventRecord.startTime(), eventRecord.endTime(), testEvent.getEventID());
                testEvent.activateEvent();
                testVenue = venue;
                eventRepo.save(testEvent);
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

        @Test
        void reserveSeats_validInput_reservesSeatsCreatesOrderAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );
                assertTrue(result.isSuccess());
                assertTrue(result.getValue().contains("new OrderId:"));

                assertEquals(ordersBefore + 1, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }


        @Test
        void reserveSeats_invalidToken_returnsAuthenticationFail() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "invalid"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
                verify(queue, never()).addToQueue(anyString());
                verify(queue, never()).validateUserPassedQueue(anyString());
                verify(queue, never()).removePassed(anyString());
        }

        @Test
        void reserveSeats_adminToken_returnsAuthenticationFail() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "admin"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
                verify(queue, never()).addToQueue(anyString());
                verify(queue, never()).validateUserPassedQueue(anyString());
                verify(queue, never()).removePassed(anyString());
        }

        @Test
        void reserveSeats_eventNotFound_returnsFail() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        999999,
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("not found"));

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
                verify(queue, never()).addToQueue(anyString());
                verify(queue, never()).validateUserPassedQueue(anyString());
                verify(queue, never()).removePassed(anyString());
        }

        @Test
        void reserveSeats_eventInactive_returnsFail() {
                int ordersBefore = orderRepo.getAll().size();

                Event inactiveEvent = eventRepo.findByID(String.valueOf(testEvent.getEventID()));

                // Replace this line with your actual domain method if different:
                inactiveEvent.deactivateEvent();
                eventRepo.save(inactiveEvent);

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        inactiveEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
                verify(queue, never()).addToQueue(anyString());
                verify(queue, never()).validateUserPassedQueue(anyString());
                verify(queue, never()).removePassed(anyString());
        }
/* 
        @Test
        void reserveSeats_eventHasLotteryPolicy_returnsFail() {
                int ordersBefore = orderRepo.getAll().size();

                Event lotteryEvent = eventRepo.findByID(String.valueOf(testEvent.getEventID()));

                // TODO add lottery policy to the event

                eventRepo.save(lotteryEvent);

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        lotteryEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
                verify(queue, never()).addToQueue(anyString());
                verify(queue, never()).validateUserPassedQueue(anyString());
                verify(queue, never()).removePassed(anyString());
        }*/

        @Test
        void reserveSeats_queueNotFound_returnsFail() {
                int ordersBefore = orderRepo.getAll().size();

                when(queueRepo.findByID(String.valueOf(testEvent.getEventID())))
                        .thenThrow(new IllegalArgumentException("Queue not found"));

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Queue not found"));

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue, never()).addToQueue(anyString());
                verify(queue, never()).validateUserPassedQueue(anyString());
                verify(queue, never()).removePassed(anyString());
                verify(queueRepo, never()).save(queue);
        }

        @Test
        void reserveSeats_userDoesNotPassQueue_returnsFailAndRemovesFromQueue() {
                int ordersBefore = orderRepo.getAll().size();

                doThrow(new IllegalStateException("User did not pass queue"))
                        .when(queue)
                        .validateUserPassedQueue(testUser.getEmail());

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("User did not pass queue"));

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());

                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_venueNotFound_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        "missingVenue",
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: No venue found for id: missingVenue", result.getError());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_segmentNotFound_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveSeats(
                        "missingSegment",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Segment with ID missingSegment not found", result.getError());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_selectedSeatsDoNotExist_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("Z-999"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_selectedSeatsUnavailable_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                // A-1 and A-2 were already reserved in seedOrders()
                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("A-1", "A-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_purchasePolicyFails_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                PurchasePolicy failingPolicy = mock(PurchasePolicy.class);

                try {
                        doThrow(new PurchasePolicyException("blocked by policy"))
                                .when(failingPolicy)
                                .validatePurchase(any(PurchaseContext.class));
                } catch (PurchasePolicyException e) {
                        throw new RuntimeException(e);
                }

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                Event eventSpy = spy(testEvent);

                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(eventSpy);
                when(eventSpy.getEventPurchasePolicy()).thenReturn(new HashSet<>(Set.of(failingPolicy)));

                ReserveService service = new ReserveService(
                        authService,
                        productionCompanyRepo,
                        queueRepo,
                        venueRepo,
                        mockedEventRepo,
                        orderRepo,
                        userRepo
                );

                Result<String> result = service.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: User did not meet purchase policy requirements", result.getError());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_eventDiscountPolicyFails_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                DiscountPolicy failingDiscountPolicy = mock(DiscountPolicy.class);

                when(failingDiscountPolicy.calculateDiscount(anyDouble()))
                        .thenThrow(new IllegalArgumentException("Event discount policy failed"));

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                Event eventSpy = spy(testEvent);

                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(eventSpy);
                when(eventSpy.getEventDiscountPolicy()).thenReturn(new HashSet<>(Set.of(failingDiscountPolicy)));

                ReserveService service = new ReserveService(
                        authService,
                        productionCompanyRepo,
                        queueRepo,
                        venueRepo,
                        mockedEventRepo,
                        orderRepo,
                        userRepo
                );

                Result<String> result = service.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Event discount policy failed", result.getError());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_companyDiscountPolicyFails_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                DiscountPolicy failingDiscountPolicy = mock(DiscountPolicy.class);

                when(failingDiscountPolicy.calculateDiscount(anyDouble()))
                        .thenThrow(new IllegalArgumentException("Company discount policy failed"));

                IProductionCompanyRepository mockedCompanyRepo = mock(IProductionCompanyRepository.class);
                ProductionCompany companySpy = spy(testPCompany);

                when(mockedCompanyRepo.findByID(String.valueOf(testPCompany.getProductionCompanyID()))).thenReturn(companySpy);
                when(companySpy.getDiscountPolicy()).thenReturn(new HashSet<>(Set.of(failingDiscountPolicy)));

                ReserveService service = new ReserveService(
                        authService,
                        mockedCompanyRepo,
                        queueRepo,
                        venueRepo,
                        eventRepo,
                        orderRepo,
                        userRepo
                );

                Result<String> result = service.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Company discount policy failed", result.getError());

                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }


        @Test
        void reserveSeats_nullSeatIds_returnsFail() {
                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1", null,
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Invalid input parameters for seat reservation", result.getError());
        }

        @Test
        void reserveSeats_emptySeatIds_returnsFail() {
                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1", List.of(),
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                // Replace exact string after first run if needed.
                assertEquals("Invalid input parameters for seat reservation", result.getError());
        }

        @Test
        void reserveSeats_duplicateSeatIds_returnsFail() {
                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1", List.of("B-1", "B-1"),
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Failed to reserve seat with ID B-1", result.getError());
        }

        @Test
        void reserveSeats_nullSegmentId_returnsFail() {
                Result<String> result = reserveService.reserveSeats(
                        null, List.of("B-1"),
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Invalid input parameters for seat reservation", result.getError());
        }

        @Test
        void reserveSeats_blankSegmentId_returnsFail() {
                Result<String> result = reserveService.reserveSeats(
                        "   ", List.of("B-1"),
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Segment with ID     not found", result.getError());
        }

        @Test
        void reserveSeats_invalidEventId_returnsFail() {
                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1", List.of("B-1"),
                        -1, testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Event with ID -1 not found", result.getError());
        }

        @Test
        void reserveSeats_nullVenueId_returnsFail() {
                Result<String> result = reserveService.reserveSeats(
                        "seatingSeg1", List.of("B-1"),
                        testEvent.getEventID(), null, "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Invalid input parameters for seat reservation", result.getError());
        }

        
        @Test
        void reserveSeats_venueSaveFails_returnsFailAndClearsQueue() {
                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);

                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);
                doThrow(new RuntimeException("DB exploded")).when(mockedVenueRepo).save(any(Venue.class));

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, eventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveSeats(
                        "seatingSeg1", List.of("B-1", "B-2"),
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("An unexpected error occurred: DB exploded", result.getError());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_orderSaveFails_cancelsSeatReservationAndClearsQueue() {
                IOrderRepository mockedOrderRepo = mock(IOrderRepository.class);
                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));

                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);

                doThrow(new RuntimeException("Order save failed"))
                        .when(mockedOrderRepo)
                        .save(any(Order.class));

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, eventRepo, mockedOrderRepo, userRepo
                );

                Result<String> result = service.reserveSeats(
                        "seatingSeg1", List.of("B-1", "B-2"),
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("An unexpected error occurred: Order save failed", result.getError());

                verify(venueSpy).reserveTickets("seatingSeg1", List.of("B-1", "B-2"), testEvent.getEventID());
                verify(venueSpy).cancelSeatReservation("seatingSeg1", List.of("B-1", "B-2"), testEvent.getEventID());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveSeats_optimisticLockingFailure_cancelsSeatReservationAndClearsQueue() {
                IOrderRepository mockedOrderRepo = mock(IOrderRepository.class);
                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));

                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);

                doThrow(new OptimisticLockingFailureException("version mismatch"))
                        .when(mockedOrderRepo)
                        .save(any(Order.class));

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, eventRepo, mockedOrderRepo, userRepo
                );

                Result<String> result = service.reserveSeats(
                        "seatingSeg1", List.of("B-1", "B-2"),
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Optimistic locking failure: version mismatch", result.getError());

                verify(venueSpy).cancelSeatReservation("seatingSeg1", List.of("B-1", "B-2"), testEvent.getEventID());
                verify(queue).removePassed(testUser.getEmail());
        }

        @Test
        void reserveFieldSeats_validInput_reservesFieldTicketsCreatesOrderAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertTrue(result.isSuccess());
                assertTrue(result.getValue().contains("new OrderId:"));
                assertEquals(ordersBefore + 1, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveFieldSeats_invalidToken_returnsAuthenticationFail() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "invalid"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
                verify(queue, never()).addToQueue(anyString());
        }

        @Test
        void reserveFieldSeats_adminToken_returnsAuthenticationFail() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "admin"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("Authentication failed"));
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
                verify(queue, never()).addToQueue(anyString());
        }

        @Test
        void reserveFieldSeats_eventNotFound_returnsFail() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        999999,
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("not found"));
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
        }

        @Test
        void reserveFieldSeats_eventInactive_returnsFail() {
                int ordersBefore = orderRepo.getAll().size();

                Event inactiveEvent = eventRepo.findByID(String.valueOf(testEvent.getEventID()));
                inactiveEvent.deactivateEvent();
                eventRepo.save(inactiveEvent);

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        inactiveEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queueRepo, never()).findByID(anyString());
        }

        @Test
        void reserveFieldSeats_queueNotFound_returnsFail() {
                int ordersBefore = orderRepo.getAll().size();

                when(queueRepo.findByID(String.valueOf(testEvent.getEventID())))
                        .thenThrow(new IllegalArgumentException("Queue not found"));

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Queue not found", result.getError());
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue, never()).addToQueue(anyString());
                verify(queueRepo, never()).save(queue);
        }

        @Test
        void reserveFieldSeats_userDoesNotPassQueue_returnsFailAndRemovesFromQueue() {
                int ordersBefore = orderRepo.getAll().size();

                doThrow(new IllegalStateException("User did not pass queue"))
                        .when(queue)
                        .validateUserPassedQueue(testUser.getEmail());

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("User did not pass queue"));
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).addToQueue(testUser.getEmail());
                verify(queue).validateUserPassedQueue(testUser.getEmail());
                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveFieldSeats_venueNotFound_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        "missingVenue",
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: No venue found for id: missingVenue", result.getError());
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveFieldSeats_segmentNotFound_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveFieldSeats(
                        "missingSegment", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Segment with ID missingSegment not found", result.getError());
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveFieldSeats_notEnoughFieldTickets_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 500,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }
        @Test
        void reserveFieldSeats_purchasePolicyFails_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                PurchasePolicy failingPolicy = mock(PurchasePolicy.class);

                try {
                        doThrow(new PurchasePolicyException("blocked by policy"))
                                .when(failingPolicy)
                                .validatePurchase(any(PurchaseContext.class));
                } catch (PurchasePolicyException e) {
                        throw new RuntimeException(e);
                }

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                Event eventSpy = spy(testEvent);

                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(eventSpy);
                when(eventSpy.getEventPurchasePolicy()).thenReturn(new HashSet<>(Set.of(failingPolicy)));

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        venueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: User did not meet purchase policy requirements", result.getError());
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveFieldSeats_eventDiscountPolicyFails_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                DiscountPolicy failingDiscountPolicy = mock(DiscountPolicy.class);

                when(failingDiscountPolicy.calculateDiscount(anyDouble()))
                        .thenThrow(new IllegalArgumentException("Event discount policy failed"));

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                Event eventSpy = spy(testEvent);

                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(eventSpy);
                when(eventSpy.getEventDiscountPolicy()).thenReturn(new HashSet<>(Set.of(failingDiscountPolicy)));

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        venueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Event discount policy failed", result.getError());
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveFieldSeats_companyDiscountPolicyFails_returnsFailAndClearsQueue() {
                int ordersBefore = orderRepo.getAll().size();

                DiscountPolicy failingDiscountPolicy = mock(DiscountPolicy.class);

                when(failingDiscountPolicy.calculateDiscount(anyDouble()))
                        .thenThrow(new IllegalArgumentException("Company discount policy failed"));

                IProductionCompanyRepository mockedCompanyRepo = mock(IProductionCompanyRepository.class);
                ProductionCompany companySpy = spy(testPCompany);

                when(mockedCompanyRepo.findByID(String.valueOf(testPCompany.getProductionCompanyID()))).thenReturn(companySpy);
                when(companySpy.getDiscountPolicy()).thenReturn(new HashSet<>(Set.of(failingDiscountPolicy)));

                ReserveService service = new ReserveService(
                        authService, mockedCompanyRepo, queueRepo,
                        venueRepo, eventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Company discount policy failed", result.getError());
                assertEquals(ordersBefore, orderRepo.getAll().size());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }




        @Test
        void reserveFieldSeats_zeroAmount_returnsFail() {
                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 0,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Invalid input parameters for field reservation", result.getError());
        }

        @Test
        void reserveFieldSeats_negativeAmount_returnsFail() {
                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", -1,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Invalid input parameters for field reservation", result.getError());
        }

        @Test
        void reserveFieldSeats_nullSegmentId_returnsFail() {
                Result<String> result = reserveService.reserveFieldSeats(
                        null, 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Invalid input parameters for field reservation", result.getError());
        }

        @Test
        void reserveFieldSeats_blankSegmentId_returnsFail() {
                Result<String> result = reserveService.reserveFieldSeats(
                        "   ", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Segment with ID     not found", result.getError());
        }

        @Test
        void reserveFieldSeats_invalidEventId_returnsFail() {
                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        -1,
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: Event with ID -1 not found", result.getError());
        }

        @Test
        void reserveFieldSeats_nullVenueId_returnsFail() {
                Result<String> result = reserveService.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        null,
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Invalid input parameters for field reservation", result.getError());
        }

        @Test
        void reserveFieldSeats_venueSaveFails_returnsFailAndClearsQueue() {
                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);

                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);
                doThrow(new RuntimeException("DB exploded")).when(mockedVenueRepo).save(any(Venue.class));

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, eventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("An unexpected error occurred: DB exploded", result.getError());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveFieldSeats_orderSaveFails_cancelsFieldReservationAndClearsQueue() {
                IOrderRepository mockedOrderRepo = mock(IOrderRepository.class);
                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));

                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);

                doThrow(new RuntimeException("Order save failed"))
                        .when(mockedOrderRepo)
                        .save(any(Order.class));

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, eventRepo, mockedOrderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("An unexpected error occurred: Order save failed", result.getError());

                verify(venueSpy).reserveTickets("fieldSeg1", 2, testEvent.getEventID());

                // Replace method name if your rollback method for field tickets has a different name.
                verify(venueSpy).cancelFieldReservation("fieldSeg1", 2, testEvent.getEventID());

                verify(queue).removePassed(testUser.getEmail());
                verify(queueRepo, atLeast(2)).save(queue);
        }

        @Test
        void reserveFieldSeats_optimisticLockingFailure_cancelsFieldReservationAndClearsQueue() {
                IOrderRepository mockedOrderRepo = mock(IOrderRepository.class);
                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));

                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);

                doThrow(new OptimisticLockingFailureException("version mismatch"))
                        .when(mockedOrderRepo)
                        .save(any(Order.class));

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, eventRepo, mockedOrderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Optimistic locking failure: version mismatch", result.getError());

                verify(venueSpy).cancelFieldReservation("fieldSeg1", 2, testEvent.getEventID());
                verify(queue).removePassed(testUser.getEmail());
        }

        @Test
        void reserveFieldSeats_purchasePolicyFails_returnsFailBeforeFieldReservation() {
                PurchasePolicy failingPolicy = mock(PurchasePolicy.class);

                try {
                        doThrow(new PurchasePolicyException("blocked"))
                                .when(failingPolicy)
                                .validatePurchase(any(PurchaseContext.class));
                } catch (PurchasePolicyException e) {
                        throw new RuntimeException(e);
                }

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                Event eventSpy = spy(testEvent);

                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(eventSpy);
                when(eventSpy.getEventPurchasePolicy()).thenReturn(new HashSet<>(Set.of(failingPolicy)));

                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));
                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal argument: User did not meet purchase policy requirements", result.getError());

                verify(venueSpy, never()).reserveTickets(anyString(), any(Integer.class), any(Integer.class));
                verify(queue).removePassed(testUser.getEmail());
        }

        @Test
        void reserveSeats_eventHasLotteryPolicyWithoutCode_returnsFailBeforeQueue() {
                Event lotteryEvent = mockLotteryEvent();

                doThrow(new IllegalStateException("Event has a lottery purchase policy."))
                        .when(lotteryEvent)
                        .verifyDoesNotHaveLotteryPolicy();

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(lotteryEvent);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        venueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveSeats(
                        "seatingSeg1", List.of("B-1", "B-2"),
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal state encountered: Event has a lottery purchase policy.", result.getError());

                verify(queueRepo, never()).findByID(anyString());
        }

        @Test
        void reserveSeatsWithLottery_validCode_reservesSeatsCreatesOrderAndUsesCode() {
                int ordersBefore = orderRepo.getAll().size();
                Event lotteryEvent = mockLotteryEvent();

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(lotteryEvent);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        venueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveSeatsWithLottery(
                        "seatingSeg1", List.of("B-1", "B-2"),
                        testEvent.getEventID(), testVenue.getID(), "LOTTO123", "user1"
                );

                assertTrue(result.isSuccess());
                assertEquals(ordersBefore + 1, orderRepo.getAll().size());

                verify(lotteryEvent).validateLotteryCode("LOTTO123");
                verify(lotteryEvent).lotteryUseCode("LOTTO123");
                verify(mockedEventRepo).save(lotteryEvent);
                verify(queue).removePassed(testUser.getEmail());
        }

        @Test
        void reserveSeatsWithLottery_invalidCode_returnsFailBeforeQueue() {
                Event lotteryEvent = mockLotteryEvent();

                doThrow(new IllegalStateException("Invalid lottery code"))
                        .when(lotteryEvent)
                        .validateLotteryCode("BADCODE");

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(lotteryEvent);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        venueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveSeatsWithLottery(
                        "seatingSeg1", List.of("B-1", "B-2"),
                        testEvent.getEventID(), testVenue.getID(), "BADCODE", "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal state encountered: Invalid lottery code", result.getError());

                verify(queueRepo, never()).findByID(anyString());
                verify(lotteryEvent, never()).lotteryUseCode(anyString());
        }

        @Test
        void reserveSeatsWithLottery_orderSaveFails_renewsLotteryCodeAndCancelsSeatReservation() {
                Event lotteryEvent = mockLotteryEvent();

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(lotteryEvent);

                IOrderRepository mockedOrderRepo = mock(IOrderRepository.class);
                doThrow(new RuntimeException("Order save failed"))
                        .when(mockedOrderRepo)
                        .save(any(Order.class));

                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));
                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, mockedEventRepo, mockedOrderRepo, userRepo
                );

                Result<String> result = service.reserveSeatsWithLottery(
                        "seatingSeg1", List.of("B-1", "B-2"),
                        testEvent.getEventID(), testVenue.getID(), "LOTTO123", "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("An unexpected error occurred: Order save failed", result.getError());

                verify(lotteryEvent).lotteryUseCode("LOTTO123");
                verify(lotteryEvent).renewLotteryCode("LOTTO123");
                verify(venueSpy).cancelSeatReservation("seatingSeg1", List.of("B-1", "B-2"), testEvent.getEventID());
                verify(queue).removePassed(testUser.getEmail());
        }
        @Test
        void reserveFieldSeats_eventHasLotteryPolicyWithoutCode_returnsFailBeforeQueue() {
                Event lotteryEvent = mockLotteryEvent();

                doThrow(new IllegalStateException("Event has a lottery purchase policy."))
                        .when(lotteryEvent)
                        .verifyDoesNotHaveLotteryPolicy();

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(lotteryEvent);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        venueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeats(
                        "fieldSeg1", 2,
                        testEvent.getEventID(), testVenue.getID(), "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal state encountered: Event has a lottery purchase policy.", result.getError());

                verify(queueRepo, never()).findByID(anyString());
        }

        @Test
        void reserveFieldSeatsWithLottery_validCode_reservesFieldTicketsCreatesOrderAndUsesCode() {
                int ordersBefore = orderRepo.getAll().size();
                Event lotteryEvent = mockLotteryEvent();

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(lotteryEvent);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        venueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeatsWithLottery(
                        "fieldSeg1", 2,
                        testEvent.getEventID(), testVenue.getID(), "LOTTO123", "user1"
                );

                assertTrue(result.isSuccess());
                assertEquals(ordersBefore + 1, orderRepo.getAll().size());

                verify(lotteryEvent).validateLotteryCode("LOTTO123");
                verify(lotteryEvent).lotteryUseCode("LOTTO123");
                verify(mockedEventRepo).save(lotteryEvent);
                verify(queue).removePassed(testUser.getEmail());
        }

        @Test
        void reserveFieldSeatsWithLottery_invalidCode_returnsFailBeforeQueue() {
                Event lotteryEvent = mockLotteryEvent();

                doThrow(new IllegalStateException("Invalid lottery code"))
                        .when(lotteryEvent)
                        .validateLotteryCode("BADCODE");

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(lotteryEvent);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        venueRepo, mockedEventRepo, orderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeatsWithLottery(
                        "fieldSeg1", 2,
                        testEvent.getEventID(), testVenue.getID(), "BADCODE", "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("Illegal state encountered: Invalid lottery code", result.getError());

                verify(queueRepo, never()).findByID(anyString());
                verify(lotteryEvent, never()).lotteryUseCode(anyString());
        }

        @Test
        void reserveFieldSeatsWithLottery_orderSaveFails_renewsLotteryCodeAndCancelsFieldReservation() {
                Event lotteryEvent = mockLotteryEvent();

                IEventRepository mockedEventRepo = mock(IEventRepository.class);
                when(mockedEventRepo.findByID(String.valueOf(testEvent.getEventID()))).thenReturn(lotteryEvent);

                IOrderRepository mockedOrderRepo = mock(IOrderRepository.class);
                doThrow(new RuntimeException("Order save failed"))
                        .when(mockedOrderRepo)
                        .save(any(Order.class));

                Venue venueSpy = spy(venueRepo.findByID(testVenue.getID()));
                IRepository<Venue> mockedVenueRepo = mock(IRepository.class);
                when(mockedVenueRepo.findByID(testVenue.getID())).thenReturn(venueSpy);

                ReserveService service = new ReserveService(
                        authService, productionCompanyRepo, queueRepo,
                        mockedVenueRepo, mockedEventRepo, mockedOrderRepo, userRepo
                );

                Result<String> result = service.reserveFieldSeatsWithLottery(
                        "fieldSeg1", 2,
                        testEvent.getEventID(), testVenue.getID(), "LOTTO123", "user1"
                );

                assertFalse(result.isSuccess());
                assertEquals("An unexpected error occurred: Order save failed", result.getError());

                verify(lotteryEvent).lotteryUseCode("LOTTO123");
                verify(lotteryEvent).renewLotteryCode("LOTTO123");
                verify(venueSpy).cancelFieldReservation("fieldSeg1", 2, testEvent.getEventID());
                verify(queue).removePassed(testUser.getEmail());
        }

        private Event mockLotteryEvent() {
                Event lotteryEvent = mock(Event.class);
                LotteryPolicy lotteryPolicy = mock(LotteryPolicy.class);

                when(lotteryEvent.getEventProductionCompanyID()).thenReturn(testPCompany.getProductionCompanyID());
                when(lotteryEvent.getEventPurchasePolicy()).thenReturn(new HashSet<>(Set.of(lotteryPolicy)));
                when(lotteryEvent.getEventDiscountPolicy()).thenReturn(new HashSet<>());

                return lotteryEvent;
        }

@Test
        void reserveSeats_twoUsersTrySameSeats_onlyOneSucceeds() throws Exception {
                seedSecondUser();
                int ordersBefore = orderRepo.getAll().size();

                ExecutorService executor = Executors.newFixedThreadPool(2);
                CountDownLatch startGate = new CountDownLatch(1);

                Future<Result<String>> first = executor.submit(() -> {
                        startGate.await();
                        return reserveService.reserveSeats(
                                "seatingSeg1",
                                List.of("B-1", "B-2"),
                                testEvent.getEventID(),
                                testVenue.getID(),
                                "user1"
                        );
                });

                Future<Result<String>> second = executor.submit(() -> {
                        startGate.await();
                return reserveService.reserveSeats(
                        "seatingSeg1",
                        List.of("B-1", "B-2"),
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user2"
                );
        });

        startGate.countDown();

        Result<String> result1 = first.get();
        Result<String> result2 = second.get();

        executor.shutdown();

        int successCount = 0;
        if (result1.isSuccess()) successCount++;
        if (result2.isSuccess()) successCount++;

        assertEquals(1, successCount);
        assertEquals(ordersBefore + 1, orderRepo.getAll().size());

        verify(queue, atLeast(2)).addToQueue(anyString());
        verify(queue, atLeast(2)).validateUserPassedQueue(anyString());
        verify(queue, atLeast(2)).removePassed(anyString());
        }
        @Test
        void reserveFieldSeats_twoUsersTryReserveLastFieldTickets_onlyOneSucceeds() throws Exception {
        seedSecondUser();
        int ordersBefore = orderRepo.getAll().size();

        // fieldSeg1 starts with 100, seedOrders already reserved 3, so 97 remain.
        int remainingFieldTickets = 97;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);

        Future<Result<String>> first = executor.submit(() -> {
                startGate.await();
                return reserveService.reserveFieldSeats(
                        "fieldSeg1",
                        remainingFieldTickets,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user1"
                );
        });

        Future<Result<String>> second = executor.submit(() -> {
                startGate.await();
                return reserveService.reserveFieldSeats(
                        "fieldSeg1",
                        remainingFieldTickets,
                        testEvent.getEventID(),
                        testVenue.getID(),
                        "user2"
                );
        });

        startGate.countDown();

        Result<String> result1 = first.get();
        Result<String> result2 = second.get();

        executor.shutdown();

        int successCount = 0;
        if (result1.isSuccess()) successCount++;
        if (result2.isSuccess()) successCount++;

        assertEquals(1, successCount);
        assertEquals(ordersBefore + 1, orderRepo.getAll().size());

        verify(queue, atLeast(2)).addToQueue(anyString());
        verify(queue, atLeast(2)).validateUserPassedQueue(anyString());
        verify(queue, atLeast(2)).removePassed(anyString());
        }

        private void seedSecondUser() {
                User user2 = new User("user2@test.com", "password");
                userRepo.save(user2);

                when(authService.validateToken("user2")).thenReturn(true);
                when(authService.extractSubjectFromToken("user2")).thenReturn(user2.getEmail());
                when(authService.isAdminToken("user2")).thenReturn(false);
        }

}
