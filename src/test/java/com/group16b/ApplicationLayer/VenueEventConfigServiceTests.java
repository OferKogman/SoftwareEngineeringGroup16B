package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import com.group16b.ApplicationLayer.Records.EventRecord;
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
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.GridRectangle;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Stage;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Venue.VenueGrid;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
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
                                mockOrderRepository, mockPaymentService, mockTicketGateway);
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
                                new ConcurrentHashMap<String, Entrance>(), 1);

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

        private FieldSegRecord fieldRecord(String segmentID, int size) {
                return new FieldSegRecord(
                                segmentID,
                                size,
                                new GridRectangleRecord(0, 0, 1, 1));
        }

        private ChosenSeatingSegRecord seatRecord(String segmentID, SeatRecord... seats) {
                return new ChosenSeatingSegRecord(
                                segmentID,
                                List.of(seats),
                                new GridRectangleRecord(0, 0, 1, seats.length));
        }

        private VenueRecord editVenueRecord(
                        List<FieldSegRecord> fieldSeg,
                        List<ChosenSeatingSegRecord> seatSeg) {
                return new VenueRecord(
                                venueName,
                                "Madison Square Garden",
                                fieldSeg,
                                seatSeg,
                                List.of(),
                                List.of(),
                                new VenueGridRecord(6, 7),
                                List.of());
        }

        private void mockExistingSegments(Venue venue, String... segmentIDs) {
                Map<String, Segment> segments = new ConcurrentHashMap<>();

                for (String segmentID : segmentIDs) {
                        segments.put(segmentID, mock(Segment.class));
                }

                when(venue.getSegments()).thenReturn(segments);
        }

        @Test
        void editVenueSegments_NullEditedVenue_ReturnsFail() {
                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                null);

                assertFalse(result.isSuccess());
                assertEquals("Invalid input parameters.", result.getError());
                verify(mockVenueRepository, never()).save(any(Venue.class));
        }

        @Test
        void editVenueSegments_InvalidToken_ReturnsFail() {
                when(mockAuthService.validateToken(validToken)).thenReturn(false);

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editVenueRecord(List.of(), List.of()));

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
                                validToken,
                                editVenueRecord(List.of(), List.of()));

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
                                validToken,
                                editVenueRecord(List.of(), List.of()));

                assertFalse(result.isSuccess());
                assertEquals("Venue does not belong to this company.", result.getError());
                verify(mockVenueRepository, never()).save(any(Venue.class));
        }

        @Test
        void editVenueSegments_NoFutureEvents_EditsVenueAndSaves_NoRefunds() {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);

                mockValidAuthAndPermission(venue, company);
                mockExistingSegments(venue, "field-a", "seat-a");

                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of());

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editVenueRecord(
                                                List.of(fieldRecord("field-a", 50)),
                                                List.of(seatRecord("seat-a", new SeatRecord(1, 1),
                                                                new SeatRecord(1, 2)))));

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                verify(venue).setNewFieldStock("field-a", 50, List.of());
                verify(venue).setNewSeatingStock("seat-a", List.of("1-1", "1-2"), List.of());
                verify(mockVenueRepository).save(venue);

                verify(mockOrderRepository, never()).getCompletedByEventIdSeatIds(anyInt(), anyString(), any());
                verify(mockOrderRepository, never()).getCompletedByEventIdField(anyInt(), anyString());
        }

        @Test
        void editVenueSegments_SeatingRefund_CancelsPaymentRevokesTicketCancelsOrderAndSavesOrder() throws Exception {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);
                Event futureEvent = mock(Event.class);
                Order order = mock(Order.class);

                mockValidAuthAndPermission(venue, company);
                mockExistingSegments(venue, "seat-a");

                when(futureEvent.getEventID()).thenReturn(eventID);
                when(futureEvent.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(2));
                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(futureEvent));
                when(mockEventRepository.findByID(Integer.toString(eventID))).thenReturn(futureEvent);

                when(venue.getStockRefundForEvent(eventID, "seat-a", List.of("1-1")))
                                .thenReturn(List.of("1-2"));

                when(mockOrderRepository.getCompletedByEventIdSeatIds(eventID, "seat-a", List.of("1-2")))
                                .thenReturn(List.of(order));

                when(order.getTransactionId()).thenReturn(11);
                when(order.getExternalTicket()).thenReturn("ticket-1");

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editVenueRecord(
                                                List.of(),
                                                List.of(seatRecord("seat-a", new SeatRecord(1, 1)))));

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
                mockExistingSegments(venue, "field-a");

                when(futureEvent.getEventID()).thenReturn(eventID);
                when(futureEvent.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(2));
                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(futureEvent));
                when(mockEventRepository.findByID(Integer.toString(eventID))).thenReturn(futureEvent);

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
                                validToken,
                                editVenueRecord(
                                                List.of(fieldRecord("field-a", 3)),
                                                List.of()));

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
                mockExistingSegments(venue, "field-a", "seat-a");

                when(futureEvent.getEventID()).thenReturn(eventID);
                when(futureEvent.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(2));
                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(futureEvent));
                when(mockEventRepository.findByID(Integer.toString(eventID))).thenReturn(futureEvent);

                when(venue.getReservedStockBySegmentEventField(eventID, "field-a")).thenReturn(3);
                when(venue.getStockRefundForEvent(eventID, "seat-a", List.of("1-1", "1-2")))
                                .thenReturn(List.of());

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editVenueRecord(
                                                List.of(fieldRecord("field-a", 5)),
                                                List.of(seatRecord("seat-a", new SeatRecord(1, 1),
                                                                new SeatRecord(1, 2)))));

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                verify(venue).setNewFieldStock("field-a", 5, List.of(100));
                verify(venue).setNewSeatingStock("seat-a", List.of("1-1", "1-2"), List.of(eventID));
                verify(mockVenueRepository).save(venue);

                verify(mockOrderRepository, never()).getCompletedByEventIdField(anyInt(), anyString());
                verify(mockOrderRepository, never()).getCompletedByEventIdSeatIds(anyInt(), anyString(), any());
        }

        @Test
        void editVenueSegments_PastEventIgnored_NoRefundsButEditsVenue() {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);
                Event pastEvent = mock(Event.class);

                mockValidAuthAndPermission(venue, company);
                mockExistingSegments(venue, "field-a", "seat-a");

                when(pastEvent.getEventID()).thenReturn(eventID);
                when(pastEvent.getEventStartTime()).thenReturn(LocalDateTime.now().minusDays(2));
                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(pastEvent));

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editVenueRecord(
                                                List.of(fieldRecord("field-a", 10)),
                                                List.of(seatRecord("seat-a", new SeatRecord(1, 1)))));

                assertTrue(result.isSuccess());

                verify(venue).setNewFieldStock("field-a", 10, List.of());
                verify(venue).setNewSeatingStock("seat-a", List.of("1-1"), List.of());
                verify(mockVenueRepository).save(venue);

                verify(venue, never()).getReservedStockBySegmentEventField(anyInt(), anyString());
                verify(venue, never()).getStockRefundForEvent(anyInt(), anyString(), any());
        }

        @Test
        void editVenueSegments_EmptyEditedVenue_DeletesExistingSegmentsAndSaves() {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);

                mockValidAuthAndPermission(venue, company);
                mockExistingSegments(venue, "field-a", "seat-a");

                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of());

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editVenueRecord(List.of(), List.of()));

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                verify(venue).removeSegment("field-a");
                verify(venue).removeSegment("seat-a");
                verify(mockVenueRepository).save(venue);
        }

        @Test
        void editVenueSegments_MultipleFutureEvents_ChecksRefundsForAllAndSucceeds() {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);
                Event event1 = mock(Event.class);
                Event event2 = mock(Event.class);

                mockValidAuthAndPermission(venue, company);
                mockExistingSegments(venue, "field-a", "seat-a");

                when(event1.getEventID()).thenReturn(101);
                when(event1.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(1));

                when(event2.getEventID()).thenReturn(102);
                when(event2.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(3));

                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(event1, event2));
                when(mockEventRepository.findByID(Integer.toString(101))).thenReturn(event1);
                when(mockEventRepository.findByID(Integer.toString(102))).thenReturn(event2);

                when(venue.getReservedStockBySegmentEventField(101, "field-a")).thenReturn(2);
                when(venue.getReservedStockBySegmentEventField(102, "field-a")).thenReturn(4);

                when(venue.getStockRefundForEvent(101, "seat-a", List.of("1-1"))).thenReturn(List.of());
                when(venue.getStockRefundForEvent(102, "seat-a", List.of("1-1"))).thenReturn(List.of());

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editVenueRecord(
                                                List.of(fieldRecord("field-a", 10)),
                                                List.of(seatRecord("seat-a", new SeatRecord(1, 1)))));

                assertTrue(result.isSuccess());

                verify(venue).getReservedStockBySegmentEventField(101, "field-a");
                verify(venue).getReservedStockBySegmentEventField(102, "field-a");

                verify(venue).getStockRefundForEvent(101, "seat-a", List.of("1-1"));
                verify(venue).getStockRefundForEvent(102, "seat-a", List.of("1-1"));

                verify(mockVenueRepository).save(venue);
        }

        @Test
        void editVenueSegments_NewSegments_AddsAndInitializesForFutureEvents() {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);
                Event futureEvent = mock(Event.class);

                mockValidAuthAndPermission(venue, company);
                mockExistingSegments(venue);

                when(futureEvent.getEventID()).thenReturn(eventID);
                when(futureEvent.getEventStartTime()).thenReturn(LocalDateTime.now().plusDays(2));
                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of(futureEvent));
                when(mockEventRepository.findByID(Integer.toString(eventID))).thenReturn(futureEvent);

                FieldSegRecord newField = fieldRecord("field-new", 20);
                ChosenSeatingSegRecord newSeat = seatRecord("seat-new", new SeatRecord(1, 1));

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editVenueRecord(List.of(newField), List.of(newSeat)));

                assertTrue(result.isSuccess());

                verify(venue).addFieldSegment(newField);
                verify(venue).initializeSegmentForEvent("field-new", eventID);

                verify(venue).addChosenSeatingSegment(newSeat);
                verify(venue).initializeSegmentForEvent("seat-new", eventID);

                verify(mockVenueRepository).save(venue);
        }

        @Test
        void editVenueSegments_UpdatesGridStagesAndEntrances() {
                Venue venue = mock(Venue.class);
                ProductionCompany company = mock(ProductionCompany.class);

                mockValidAuthAndPermission(venue, company);
                mockExistingSegments(venue);

                when(mockEventRepository.findAllByVenueID("venue-123")).thenReturn(List.of());

                VenueGridRecord grid = new VenueGridRecord(20, 30);
                StageRecord stage = new StageRecord("ST1", new GridRectangleRecord(1, 1, 2, 2));
                EntranceRecord entrance = new EntranceRecord("EN1", new GridRectangleRecord(3, 3, 1, 1));

                VenueRecord editedVenue = new VenueRecord(
                                venueName,
                                "Madison Square Garden",
                                List.of(),
                                List.of(),
                                List.of(stage),
                                List.of(entrance),
                                grid,
                                List.of());

                Result<Boolean> result = configService.editVenueSegments(
                                companyID,
                                "venue-123",
                                validToken,
                                editedVenue);

                assertTrue(result.isSuccess());

                verify(venue).replaceGrid(grid);
                verify(venue).replaceStages(List.of(stage));
                verify(venue).replaceEntrances(List.of(entrance));
                verify(mockVenueRepository).save(venue);
        }
        @Test
        void editVenueSegments_FieldNoRefund_RealVenueUpdatesAvailableStockFrom7To2() {
        IRepository<Venue> realVenueRepo = new VenueRepositoryMapImpl();
        IEventRepository realEventRepo = new EventRepositoryMapImpl();

        VenueEventConfigService realService = new VenueEventConfigService(
                        realVenueRepo,
                        realEventRepo,
                        mockUserRepository,
                        mockAuthService,
                        mockProductionCompanyRepository,
                        mockLocationService,
                        mockOrderRepository,
                        mockPaymentService,
                        mockTicketGateway);

        User mockUser = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userID);

        when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);
        doNothing().when(mockCompany).validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

        FieldSeg fieldSeg = new FieldSeg(
                        "field-a",
                        10,
                        new GridRectangle(0, 0, 1, 1));

        Map<String, Segment> segments = new ConcurrentHashMap<>();
        segments.put("field-a", fieldSeg);

        Location location = new Location(
                        "Test Location",
                        "1",
                        "Test Street",
                        "Test City",
                        "Test State",
                        "Test Country",
                        0.0,
                        0.0);

        Venue venue = new Venue(
                        "Test Venue",
                        location,
                        segments,
                        "venue-123",
                        new VenueGrid(6, 7),
                        new ConcurrentHashMap<>(),
                        new ConcurrentHashMap<>(),
                        companyID);

        realVenueRepo.save(venue);
        venue = realVenueRepo.findByID("venue-123");

        EventRecord eventRecord = new EventRecord(
                        "venue-123",
                        "Test Event",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2),
                        "Test Artist",
                        "Test Category",
                        companyID,
                        4.5);

        Event event = new Event(eventRecord, userID);
        event.activateEvent();
        realEventRepo.save(event);

        venue.bookEvent(eventRecord.startTime(), eventRecord.endTime(), event.getEventID());
        venue.initializeSegmentForEvent("field-a", event.getEventID());

        realVenueRepo.save(venue);
        venue = realVenueRepo.findByID("venue-123");

        venue.reserveSeats(ReservationRequest.forField(event.getEventID(), 3, "field-a"));
        realVenueRepo.save(venue);
        venue = realVenueRepo.findByID("venue-123");

        FieldSeg beforeEditSeg = (FieldSeg) venue.getSegments().get("field-a");

        assertEquals(10, beforeEditSeg.getFieldSize());
        assertEquals(7, beforeEditSeg.getStock(event.getEventID()));
        assertEquals(3, venue.getReservedStockBySegmentEventField(event.getEventID(), "field-a"));

        Result<Boolean> result = realService.editVenueSegments(
                        companyID,
                        "venue-123",
                        validToken,
                        editVenueRecord(
                                        List.of(fieldRecord("field-a", 5)),
                                        List.of()));

        assertTrue(result.isSuccess());
        assertTrue(result.getValue());

        Venue updatedVenue = realVenueRepo.findByID("venue-123");
        FieldSeg updatedFieldSeg = (FieldSeg) updatedVenue.getSegments().get("field-a");

        assertEquals(5, updatedFieldSeg.getFieldSize());
        assertEquals(2, updatedFieldSeg.getStock(event.getEventID()));
        assertEquals(3, updatedVenue.getReservedStockBySegmentEventField(event.getEventID(), "field-a"));

        verify(mockOrderRepository, never()).getCompletedByEventIdField(anyInt(), anyString());
        verify(mockPaymentService, never()).cancelPayment(anyInt());
        verify(mockTicketGateway, never()).revokeTicket(anyString());
}
}