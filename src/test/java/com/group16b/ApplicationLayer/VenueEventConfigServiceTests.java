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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.ChosenSeatingSegRecord;
import com.group16b.ApplicationLayer.Records.FieldSegRecord;
import com.group16b.ApplicationLayer.Records.SeatRecord;
import com.group16b.ApplicationLayer.Records.VenueRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueEventConfigServiceTests {

    private IRepository<Venue> mockVenueRepository;
    private IEventRepository mockEventRepository;
    private IRepository<User> mockUserRepository;
    private IAuthenticationService mockAuthService;
    private VenueEventConfigService configService;
    private IProductionCompanyRepository mockProductionCompanyRepository;
    private ILocationService mockLocationService;

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
                mockLocationService);
    }

    private VenueRecord createValidVenueRecord() {
        List<SeatRecord> dummySeats = new ArrayList<>();
        ChosenSeatingSegRecord dummySeg = new ChosenSeatingSegRecord("VIP", dummySeats);

        List<FieldSegRecord> dummyField = new ArrayList<>();
        List<ChosenSeatingSegRecord> dummySeatSeg = new ArrayList<>();
        dummySeatSeg.add(dummySeg);

        VenueRecord dummyVenue = new VenueRecord(venueName, "Madison Square Garden", dummyField, dummySeatSeg);

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
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
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
                validToken, companyID, eventID, validRecord);

        // Assert
        assertTrue(result.isSuccess(), "Expected success for valid owner setup");
        assertEquals("Venue layout configured and saved successfully.", result.getValue());

        verify(mockEvent, times(1)).setEventVenue(venueName);
        // We verify that the repository actually received ANY fully constructed Venue
        // object!
        verify(mockVenueRepository, times(1)).save(any(Venue.class));
        verify(mockEventRepository, times(1)).save(mockEvent);
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
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.getEventStatus()).thenReturn(false);

        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
        when(mockCompany.isOwner(userID)).thenReturn(false);
        when(mockCompany.isOwner(userID)).thenReturn(true); // is a manager instead

        Result<String> result = configService.configureNewLayoutAndInventory(
                validToken, companyID, eventID, validRecord);

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
                validToken, companyID, eventID, validRecord);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Please log in again.", result.getError());
        verify(mockEventRepository, never()).findByID(anyString());
    }

    @Test
    void configureNewLayoutAndInventory_TokenRoleIsNotUser_ReturnsFailResult() {
        VenueRecord validRecord = createValidVenueRecord();
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("Admin"); // Wrong role

        Result<String> result = configService.configureNewLayoutAndInventory(
                validToken, companyID, eventID, validRecord);

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
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.getEventStatus()).thenReturn(false);

        when(mockUserRepository.findByID(userID))
                .thenThrow(new IllegalArgumentException("User with ID " + userID + " not found."));

        Result<String> result = configService.configureNewLayoutAndInventory(
                validToken, companyID, eventID, validRecord);

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
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.getEventStatus()).thenReturn(false);

        when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        doThrow(new IllegalArgumentException("Permission denied. You must be an owner or manager of this company."))
                .when(mockCompany).validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

        // user exists but is neither owner nor manager
        when(mockCompany.isOwner(userID)).thenReturn(false);
        when(mockCompany.isOwner(userID)).thenReturn(false);

        Result<String> result = configService.configureNewLayoutAndInventory(
                validToken, companyID, eventID, validRecord);

        assertFalse(result.isSuccess());
        assertEquals("Configuration failed: Permission denied. You must be an owner or manager of this company.",
                result.getError());
    }

    @Test
    void configureNewLayoutAndInventory_EventDoesNotExist_ReturnsFailResult() {
        VenueRecord validRecord = createValidVenueRecord();
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        when(mockEventRepository.findByID(String.valueOf(eventID)))
                .thenThrow(new IllegalArgumentException("Event with ID " + eventID + " not found"));

        Result<String> result = configService.configureNewLayoutAndInventory(
                validToken, companyID, eventID, validRecord);

        assertFalse(result.isSuccess());
        assertEquals("Configuration failed: Event with ID " + eventID + " not found", result.getError());
    }

    @Test
    void configureNewLayoutAndInventory_DomainThrowsIllegalArgumentException_ReturnsFailResult() {
        LocalDateTime badEndTime = startTime.minusHours(5);
        Event mockEvent = mock(Event.class);
        when(mockEvent.getEventStartTime()).thenReturn(startTime);
        when(mockEvent.getEventEndTime()).thenReturn(badEndTime);
        User mockUser = mock(User.class);
        VenueRecord validRecord = createValidVenueRecord();

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.getEventStatus()).thenReturn(false);
        when(mockUserRepository.findByID(userID)).thenReturn(mockUser);

        ProductionCompany mockCompany = mock(ProductionCompany.class);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        when(mockCompany.isOwner(userID)).thenReturn(true);

        Result<String> result = configService.configureNewLayoutAndInventory(
                validToken, companyID, eventID, validRecord);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Event start time must be before end time!"));
        verify(mockVenueRepository, never()).save(any());
    }

    @Test
    void configureNewLayoutAndInventory_GenericSystemException_ReturnsFailResult() {
        VenueRecord validRecord = createValidVenueRecord();
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);

        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn("invalid_id_format");

        Result<String> result = configService.configureNewLayoutAndInventory(
                validToken, companyID, eventID, validRecord);

        assertFalse(result.isSuccess());
        assertEquals("An unexpected system error occurred while saving the layout.", result.getError());
    }

    @Test
    void configureNewLayoutAndInventory_NullVenueLayout_TriggersSystemException_ReturnsFailResult() {
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);

        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mock(Event.class));
        when(mockUserRepository.findByID(userID)).thenReturn(mock(User.class));

        ProductionCompany mockCompany = mock(ProductionCompany.class);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        when(mockCompany.isOwner(userID)).thenReturn(true);

        Result<String> result = configService.configureNewLayoutAndInventory(
                validToken, companyID, eventID, null);

        assertFalse(result.isSuccess());

        assertEquals("An unexpected system error occurred while saving the layout.", result.getError());
    }
}