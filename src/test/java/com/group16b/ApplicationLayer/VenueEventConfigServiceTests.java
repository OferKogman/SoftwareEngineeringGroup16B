package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import java.util.Arrays;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.EventScheduleDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.ChosenSeatingSegRecord;
import com.group16b.ApplicationLayer.Records.EntranceRecord;
import com.group16b.ApplicationLayer.Records.FieldSegRecord;
import com.group16b.ApplicationLayer.Records.GridRectangleRecord;
import com.group16b.ApplicationLayer.Records.SeatRecord;
import com.group16b.ApplicationLayer.Records.StageRecord;
import com.group16b.ApplicationLayer.Records.VenueGridRecord;
import com.group16b.ApplicationLayer.Records.VenueRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Entrance;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Stage;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Venue.VenueGrid;
import com.group16b.InfrastructureLayer.Security.Role;

public class VenueEventConfigServiceTests {

        private IRepository<Venue> mockVenueRepository;
        private IEventRepository mockEventRepository;
        private IRepository<User> mockUserRepository;
        private IAuthenticationService mockAuthService;
        private VenueEventConfigService configService;
        private IProductionCompanyRepository mockProductionCompanyRepository;
        private ILocationService mockLocationService;
        private IOrderRepository mockOrderRepository;
        private IPaymentGateway mockPaymentService;
        private ITicketGateway mockTicketGateway;

        // Test Data
        private final String validToken = "valid.user.token";
        private final String userIDString = "1";
        private final String userID = "1";
        private final int companyID = 10;
        private final int eventID = 100;
        private final String venueName = "Madison Square Garden";
        private final LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        private final LocalDateTime endTime = startTime.plusHours(3);

        @BeforeEach
        void setUp() throws IOException, InterruptedException {
                mockVenueRepository = mock(IRepository.class);
                mockEventRepository = mock(IEventRepository.class);
                mockUserRepository = mock(IRepository.class);
                mockAuthService = mock(IAuthenticationService.class);
                mockProductionCompanyRepository = mock(IProductionCompanyRepository.class);
                mockLocationService = mock(ILocationService.class);
                mockOrderRepository = mock(IOrderRepository.class);
                mockPaymentService = mock(IPaymentGateway.class);
                mockTicketGateway = mock(ITicketGateway.class);

                Location dummyLocation;
                dummyLocation = new Location(
                                "Madison Square Garden", "4", "Pennsylvania Plaza",
                                "New York", "NY", "USA", 40.75, -73.99);

                when(mockLocationService.search("Madison Square Garden")).thenReturn(dummyLocation);

                configService = new VenueEventConfigService(
                                mockVenueRepository,
                                mockEventRepository,
                                mockUserRepository,
                                mockAuthService,
                                mockProductionCompanyRepository,
                                mockLocationService,
                                mockOrderRepository, mockPaymentService, mockTicketGateway
                                );
        }

        private VenueRecord createValidVenueRecord() {
                List<SeatRecord> dummySeats = new ArrayList<>();
                ChosenSeatingSegRecord dummySeg = new ChosenSeatingSegRecord("VIP", dummySeats,
                                new GridRectangleRecord(5, 4, 6, 5));

                List<FieldSegRecord> dummyField = new ArrayList<>();
                List<ChosenSeatingSegRecord> dummySeatSeg = new ArrayList<>();
                dummySeatSeg.add(dummySeg);

                VenueRecord dummyVenue;
                dummyVenue = new VenueRecord(venueName, "Madison Square Garden", dummyField, dummySeatSeg,
                                new ArrayList<StageRecord>(),
                                new ArrayList<EntranceRecord>(), new VenueGridRecord(6, 7),
                                new ArrayList<EventScheduleDTO>());

                return dummyVenue;
        }

        // ==========================================
        // HAPPY PATHS
        // ==========================================

        @Test
        void configureNewLayoutAndInventory_ValidOwner_ReturnsOkResult() {
                Event mockEvent = mock(Event.class);
                when(mockEvent.getEventStartTime()).thenReturn(startTime);
                when(mockEvent.getEventEndTime()).thenReturn(endTime);
                ProductionCompany mockCompany = mock(ProductionCompany.class);
                User mockUser = mock(User.class);
                VenueRecord validRecord = createValidVenueRecord();

                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.extractRoleFromToken(validToken)).thenReturn(Role.SIGNED);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

                when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
                when(mockEvent.getEventStatus()).thenReturn(false);

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);
                doNothing().when(mockCompany).validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

                // Action
                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, validRecord);

                // Assert
                assertTrue(result.isSuccess(), "Expected success for valid owner setup");
                assertEquals("Venue layout configured and saved successfully.", result.getValue());

                // We verify that the repository actually received ANY fully constructed Venue
                // object!
                verify(mockVenueRepository, times(1)).save(any(Venue.class));
        }

        @Test
        void configureNewLayoutAndInventory_ValidManager_ReturnsOkResult() {
                Event mockEvent = mock(Event.class);
                when(mockEvent.getEventStartTime()).thenReturn(startTime);
                when(mockEvent.getEventEndTime()).thenReturn(endTime);
                User mockUser = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);
                VenueRecord validRecord = createValidVenueRecord();

                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.extractRoleFromToken(validToken)).thenReturn(Role.SIGNED);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

                when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
                when(mockEvent.getEventStatus()).thenReturn(false);

                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockCompany.isOwner(userID)).thenReturn(false);
                when(mockCompany.isOwner(userID)).thenReturn(true); // is a manager instead

                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, validRecord);

                assertTrue(result.isSuccess());
        }

        // ==========================================
        // VALIDATION & ERROR PATHS
        // ==========================================

        @Test
        void configureNewLayoutAndInventory_InvalidToken_ReturnsFailResult() {
                VenueRecord validRecord = createValidVenueRecord();
                when(mockAuthService.validateToken(validToken)).thenReturn(false);

                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, validRecord);

                assertFalse(result.isSuccess());
                assertEquals("Authentication failed. Please log in again.", result.getError());
                verify(mockEventRepository, never()).findByID(anyString());
        }

        @Test
        void configureNewLayoutAndInventory_TokenRoleIsNotUser_ReturnsFailResult() {
                VenueRecord validRecord = createValidVenueRecord();
                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.extractRoleFromToken(validToken)).thenReturn(Role.SIGNED); // Wrong role

                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, validRecord);

                assertFalse(result.isSuccess());
                assertEquals("Authentication failed. Please log in again.", result.getError());
                verify(mockAuthService, never()).extractSubjectFromToken(anyString());
        }

        @Test
        void configureNewLayoutAndInventory_UserNotFound_ReturnsFailResult() {
                Event mockEvent = mock(Event.class);
                when(mockEvent.getEventStartTime()).thenReturn(startTime);
                when(mockEvent.getEventEndTime()).thenReturn(endTime);
                VenueRecord validRecord = createValidVenueRecord();

                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.extractRoleFromToken(validToken)).thenReturn(Role.SIGNED);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

                when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
                when(mockEvent.getEventStatus()).thenReturn(false);

                when(mockUserRepository.findByID(userID))
                                .thenThrow(new IllegalArgumentException("User with ID " + userID + " not found."));

                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, validRecord);

                assertFalse(result.isSuccess());
                assertEquals("Configuration failed: User with ID 1 not found.", result.getError());
                verify(mockVenueRepository, never()).save(any(Venue.class));
        }

        @Test
        void configureNewLayoutAndInventory_UserNotPermitted_ReturnsFailResult() {
                Event mockEvent = mock(Event.class);
                when(mockEvent.getEventStartTime()).thenReturn(startTime);
                when(mockEvent.getEventEndTime()).thenReturn(endTime);
                User mockUser = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);
                VenueRecord validRecord = createValidVenueRecord();

                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.extractRoleFromToken(validToken)).thenReturn(Role.SIGNED);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

                when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
                when(mockEvent.getEventStatus()).thenReturn(false);

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

                doThrow(new IllegalArgumentException(
                                "Permission denied. You must be an owner or manager of this company."))
                                .when(mockCompany)
                                .validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

                // user exists but is neither owner nor manager
                when(mockCompany.isOwner(userID)).thenReturn(false);
                when(mockCompany.isOwner(userID)).thenReturn(false);

                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, validRecord);

                assertFalse(result.isSuccess());
                assertEquals("Configuration failed: Permission denied. You must be an owner or manager of this company.",
                                result.getError());
        }

        @Test
        void configureNewLayoutAndInventory_EventDoesNotExist_ReturnsFailResult() {
                VenueRecord validRecord = createValidVenueRecord();
                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.extractRoleFromToken(validToken)).thenReturn(Role.SIGNED);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
                when(mockEventRepository.findByID(String.valueOf(eventID)))
                                .thenThrow(new IllegalArgumentException("Event with ID " + eventID + " not found"));

                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, validRecord);

                assertFalse(result.isSuccess());
        }

        @Test
        void configureNewLayoutAndInventory_GenericSystemException_ReturnsFailResult() {
                VenueRecord validRecord = createValidVenueRecord();
                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.extractRoleFromToken(validToken)).thenReturn(Role.SIGNED);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);

                when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn("invalid_id_format");

                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, validRecord);

                assertFalse(result.isSuccess());
                assertEquals("An unexpected system error occurred while saving the layout.", result.getError());
        }

        @Test
        void configureNewLayoutAndInventory_NullVenueLayout_TriggersSystemException_ReturnsFailResult() {
                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.extractRoleFromToken(validToken)).thenReturn(Role.SIGNED);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

                when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mock(Event.class));
                when(mockUserRepository.findByID(userID)).thenReturn(mock(User.class));

                ProductionCompany mockCompany = mock(ProductionCompany.class);
                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

                when(mockCompany.isOwner(userID)).thenReturn(true);

                Result<String> result = configService.configureNewLayoutAndInventory(
                                validToken, companyID, null);

                assertFalse(result.isSuccess());

                assertEquals("An unexpected system error occurred while saving the layout.", result.getError());
        }

        @Test
        void getVenue_ValidTokenAndVenueExists_ReturnsOkResultWithDTO() {
                String targetVenueID = "venue-123";

                Location dummyLocation = new Location(
                                "Madison Square Garden", "4", "Pennsylvania Plaza",
                                "New York", "NY", "USA", 40.75, -73.99);

                Venue realVenue = new Venue("Madison Square Garden", dummyLocation, new ConcurrentHashMap<>(),
                                targetVenueID, new VenueGrid(6, 7), new ConcurrentHashMap<String, Stage>(),
                                new ConcurrentHashMap<String, Entrance>(),1);

                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);

                when(mockVenueRepository.findByID(targetVenueID)).thenReturn(realVenue);

                Result<?> result = configService.getVenue(validToken, targetVenueID);

                assertTrue(result.isSuccess(), "Expected success when fetching a valid venue.");
                assertTrue(result.getValue() != null, "Expected a VenueDTO to be returned in the Result payload.");
                verify(mockVenueRepository, times(1)).findByID(targetVenueID);
        }

        @Test
        void getVenue_InvalidToken_ReturnsFailResult() {
                String targetVenueID = "venue-123";
                when(mockAuthService.validateToken("bad.token")).thenReturn(false);

                Result<?> result = configService.getVenue("bad.token", targetVenueID);

                assertFalse(result.isSuccess());
                assertEquals("Authentication failed. Please log in again.", result.getError());
                verify(mockVenueRepository, never()).findByID(anyString());
        }

        @Test
        void getVenue_VenueDoesNotExist_ReturnsFailResult() {
                String missingVenueID = "ghost-venue";
                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);

                when(mockVenueRepository.findByID(missingVenueID))
                                .thenThrow(new IllegalArgumentException("Venue not found"));

                Result<?> result = configService.getVenue(validToken, missingVenueID);

                assertFalse(result.isSuccess());
                assertEquals("Configuration failed: Venue not found", result.getError());
        }

        @Test
        void getVenue_SystemException_ReturnsFailResult() {
                String targetVenueID = "venue-123";
                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);

                when(mockVenueRepository.findByID(targetVenueID))
                                .thenThrow(new RuntimeException("Database connection lost"));

                Result<?> result = configService.getVenue(validToken, targetVenueID);

                assertFalse(result.isSuccess());
                assertEquals("An unexpected system error occurred", result.getError());
        }
        private void mockValidAuthAndPermission(Venue venue, ProductionCompany company) {
                User mockUser = mock(User.class);

                when(mockAuthService.validateToken(validToken)).thenReturn(true);
                when(mockAuthService.isUserToken(validToken)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userID);

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockVenueRepository.findByID("venue-123")).thenReturn(venue);
                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(company);

                doNothing().when(company).validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);
                doNothing().when(venue).validateCompanyID(companyID);
        }
        @Test
        void editVenueSegments_InvalidInput_ReturnsFail() {
        Result<Boolean> result = configService.editVenueSegments(
                0,
                "venue-123",
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                validToken
        );

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed: Invalid session token.", result.getError());
        verify(mockVenueRepository, never()).save(any(Venue.class));
        }
        @Test
        void editVenueSegments_InvalidToken_ReturnsFail() {
        when(mockAuthService.validateToken(validToken)).thenReturn(false);

        Result<Boolean> result = configService.editVenueSegments(
                companyID,
                "venue-123",
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                validToken
        );

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed: Invalid session token.", result.getError());
        verify(mockVenueRepository, never()).findByID(anyString());
        }
        @Test
        void editVenueSegments_UserWithoutPermission_ReturnsFail() {
        Venue venue = mock(Venue.class);
        ProductionCompany company = mock(ProductionCompany.class);

        mockValidAuthAndPermission(venue, company);

        doThrow(new IllegalArgumentException("Permission denied."))
                .when(company)
                .validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

        Result<Boolean> result = configService.editVenueSegments(
                companyID,
                "venue-123",
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                validToken
        );

        assertFalse(result.isSuccess());
        assertEquals("Permission denied.", result.getError());
        verify(mockVenueRepository, never()).save(any(Venue.class));
        }
        @Test
        void editVenueSegments_VenueBelongsToDifferentCompany_ReturnsFail() {
        Venue venue = mock(Venue.class);
        ProductionCompany company = mock(ProductionCompany.class);

        mockValidAuthAndPermission(venue, company);

        doThrow(new IllegalArgumentException("Venue does not belong to this company."))
                .when(venue)
                .validateCompanyID(companyID);

        Result<Boolean> result = configService.editVenueSegments(
                companyID,
                "venue-123",
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                validToken
        );

        assertFalse(result.isSuccess());
        assertEquals("Venue does not belong to this company.", result.getError());
        verify(mockVenueRepository, never()).save(any(Venue.class));
        }
        @Test
        void editVenueSegments_NoFutureEvents_EditsVenueAndSaves_NoRefunds() {
        Venue venue = mock(Venue.class);
        ProductionCompany company = mock(ProductionCompany.class);

        mockValidAuthAndPermission(venue, company);

        when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of());

        Result<Boolean> result = configService.editVenueSegments(
                companyID,
                "venue-123",
                Map.of("field-a", 50),
                Map.of("seat-a", List.of("A1", "A2")),
                List.of(),
                List.of(),
                validToken
        );

        assertTrue(result.isSuccess());
        assertTrue(result.getValue());

        verify(venue).setNewFieldStock("field-a", 50);
        verify(venue).setNewSeatingStock("seat-a", List.of("A1", "A2"));
        verify(mockVenueRepository).save(venue);

        verify(mockOrderRepository, never()).getCompletedByEventIdSeatIds(any(Integer.class), anyString(), any());
        verify(mockOrderRepository, never()).getCompletedByEventIdField(any(Integer.class), anyString());
        }
        @Test
        void editVenueSegments_SeatingRefund_CancelsPaymentRevokesTicketCancelsOrderAndSavesOrder() throws Exception {
        Venue venue = mock(Venue.class);
        ProductionCompany company = mock(ProductionCompany.class);
        Event futureEvent = mock(Event.class);
        Order order = mock(Order.class);

        mockValidAuthAndPermission(venue, company);

        when(futureEvent.getEventID()).thenReturn(eventID);
        when(futureEvent.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(2));
        when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(futureEvent));

        when(venue.getStockRefundForEvent(eventID, "seat-a", List.of("A1")))
                .thenReturn(List.of("A1"));

        when(mockOrderRepository.getCompletedByEventIdSeatIds(eventID, "seat-a", List.of("A1")))
                .thenReturn(List.of(order));

        when(order.getTransactionId()).thenReturn(11);
        when(order.getExternalTicket()).thenReturn("ticket-1");

        Result<Boolean> result = configService.editVenueSegments(
                companyID,
                "venue-123",
                Map.of(),
                Map.of("seat-a", List.of("A1")),
                List.of(),
                List.of(),
                validToken
        );

        assertTrue(result.isSuccess());

        verify(mockVenueRepository).save(venue);
        verify(mockPaymentService).cancelPayment(11);
        verify(mockTicketGateway).revokeTicket("ticket-1");
        verify(order).CancelOrder();
        verify(mockOrderRepository).save(order);
        }
        @Test
                void editVenueSegments_FieldRefund_RefundsSmallestOrdersUntilAmountCovered() throws Exception {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);
                Event futureEvent = mock(Event.class);

                Order smallOrder = mock(Order.class);
                Order bigOrder = mock(Order.class);

                mockValidAuthAndPermission(venue, company);

                when(futureEvent.getEventID()).thenReturn(eventID);
                when(futureEvent.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(2));
                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(futureEvent));

                when(venue.getReservedStockBySegmentEventField(eventID, "field-a")).thenReturn(5);

                when(smallOrder.getNumOfTickets()).thenReturn(2);
                when(bigOrder.getNumOfTickets()).thenReturn(5);

                when(smallOrder.getTransactionId()).thenReturn(22);
                when(smallOrder.getExternalTicket()).thenReturn("ticket-small");

                when(bigOrder.getTransactionId()).thenReturn(33);
                when(bigOrder.getExternalTicket()).thenReturn("ticket-big");

                when(mockOrderRepository.getCompletedByEventIdField(eventID, "field-a"))
                        .thenReturn(new ArrayList<>(Arrays.asList(bigOrder, smallOrder)));

                Result<Boolean> result = configService.editVenueSegments(
                        companyID,
                        "venue-123",
                        Map.of("field-a", 3),
                        Map.of(),
                        List.of(),
                        List.of(),
                        validToken
                );

                assertTrue(result.isSuccess());

                verify(mockPaymentService).cancelPayment(22);
                verify(mockTicketGateway).revokeTicket("ticket-small");
                verify(smallOrder).CancelOrder();
                verify(mockOrderRepository).save(smallOrder);

                verify(mockPaymentService, never()).cancelPayment(33);
                verify(mockTicketGateway, never()).revokeTicket("ticket-big");
                verify(bigOrder, never()).CancelOrder();
        }
        @Test
                void editVenueSegments_FutureEventNoRefundNeeded_EditsAndSavesSuccessfully() {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);
                Event futureEvent = mock(Event.class);

                mockValidAuthAndPermission(venue, company);

                when(futureEvent.getEventID()).thenReturn(eventID);
                when(futureEvent.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(2));
                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(futureEvent));

                when(venue.getReservedStockBySegmentEventField(eventID, "field-a")).thenReturn(3);
                when(venue.getStockRefundForEvent(eventID, "seat-a", List.of("A1", "A2")))
                        .thenReturn(List.of());

                Result<Boolean> result = configService.editVenueSegments(
                        companyID,
                        "venue-123",
                        Map.of("field-a", 5),
                        Map.of("seat-a", List.of("A1", "A2")),
                        List.of(),
                        List.of(),
                        validToken
                );

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                verify(venue).setNewFieldStock("field-a", 5);
                verify(venue).setNewSeatingStock("seat-a", List.of("A1", "A2"));
                verify(mockVenueRepository).save(venue);

                verify(mockOrderRepository, never()).getCompletedByEventIdField(any(Integer.class), anyString());
                verify(mockOrderRepository, never()).getCompletedByEventIdSeatIds(any(Integer.class), anyString(), any());
                }
        @Test
                void editVenueSegments_PastEventIgnored_NoRefundsButEditsVenue() {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);
                Event pastEvent = mock(Event.class);

                mockValidAuthAndPermission(venue, company);

                when(pastEvent.getEventID()).thenReturn(eventID);
                when(pastEvent.getEventStartTime()).thenReturn(LocalDateTime.now().minusDays(2));
                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(pastEvent));

                Result<Boolean> result = configService.editVenueSegments(
                        companyID,
                        "venue-123",
                        Map.of("field-a", 10),
                        Map.of("seat-a", List.of("A1")),
                        List.of(),
                        List.of(),
                        validToken
                );

                assertTrue(result.isSuccess());

                verify(venue).setNewFieldStock("field-a", 10);
                verify(venue).setNewSeatingStock("seat-a", List.of("A1"));
                verify(mockVenueRepository).save(venue);

                verify(venue, never()).getReservedStockBySegmentEventField(any(Integer.class), anyString());
                verify(venue, never()).getStockRefundForEvent(any(Integer.class), anyString(), any());
                
        }
        @Test
        void editVenueSegments_EmptyChanges_StillSavesSuccessfully() {
        Venue venue = mock(Venue.class);
        ProductionCompany company = mock(ProductionCompany.class);

        mockValidAuthAndPermission(venue, company);

        when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of());

        Result<Boolean> result = configService.editVenueSegments(
                companyID,
                "venue-123",
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                validToken
        );

        assertTrue(result.isSuccess());
        assertTrue(result.getValue());

        verify(mockVenueRepository).save(venue);
        verify(venue, never()).setNewFieldStock(anyString(), any(Integer.class));
        verify(venue, never()).setNewSeatingStock(anyString(), any());
        }
        @Test
        void editVenueSegments_MultipleFutureEvents_ChecksRefundsForAllAndSucceeds() {
        Venue venue = mock(Venue.class);
        ProductionCompany company = mock(ProductionCompany.class);
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);

        mockValidAuthAndPermission(venue, company);

        when(event1.getEventID()).thenReturn(101);
        when(event1.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(1));

        when(event2.getEventID()).thenReturn(102);
        when(event2.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(3));

        when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(event1, event2));

        when(venue.getReservedStockBySegmentEventField(101, "field-a")).thenReturn(2);
        when(venue.getReservedStockBySegmentEventField(102, "field-a")).thenReturn(4);

        when(venue.getStockRefundForEvent(101, "seat-a", List.of("A1"))).thenReturn(List.of());
        when(venue.getStockRefundForEvent(102, "seat-a", List.of("A1"))).thenReturn(List.of());

        Result<Boolean> result = configService.editVenueSegments(
                companyID,
                "venue-123",
                Map.of("field-a", 10),
                Map.of("seat-a", List.of("A1")),
                List.of(),
                List.of(),
                validToken
        );

        assertTrue(result.isSuccess());

        verify(venue).getReservedStockBySegmentEventField(101, "field-a");
        verify(venue).getReservedStockBySegmentEventField(102, "field-a");

        verify(venue).getStockRefundForEvent(101, "seat-a", List.of("A1"));
        verify(venue).getStockRefundForEvent(102, "seat-a", List.of("A1"));

        verify(mockVenueRepository).save(venue);
        }
}