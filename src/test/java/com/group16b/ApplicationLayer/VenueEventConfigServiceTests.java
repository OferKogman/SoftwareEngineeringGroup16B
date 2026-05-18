package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.DTOs.VenueDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Seat;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueEventConfigServiceTests {

    private IVenueRepository mockVenueRepository;
    private IEventRepository mockEventRepository;
    private IRepository<User> mockUserRepository;
    private IAuthenticationService mockAuthService;
    private VenueEventConfigService configService;
    private IProductionCompanyRepository mockProductionCompanyRepository;

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
    void setUp() {
        mockVenueRepository = mock(IVenueRepository.class);
        mockEventRepository = mock(IEventRepository.class);
        mockUserRepository = mock(IRepository.class);
        mockAuthService = mock(IAuthenticationService.class);
        mockProductionCompanyRepository=mock(IProductionCompanyRepository.class);
        
        configService = new VenueEventConfigService(
                mockVenueRepository, 
                mockEventRepository, 
                mockUserRepository, 
                mockAuthService,
                mockProductionCompanyRepository
        );
    }

    private VenueDTO createValidVenueDTO() {
        Location dummyLocation;
        dummyLocation = new Location(
                "Madison Square Garden", "4", "Pennsylvania Plaza",
                "New York", "NY", "USA", 40.75, -73.99
        );
        
        Map<String, Seat> dummySeats = new ConcurrentHashMap<>();
        ChosenSeatingSeg dummySeg = new ChosenSeatingSeg("VIP", dummySeats);
        
        Map<String, Segment> dummySegments = new ConcurrentHashMap<>();
        dummySegments.put(dummySeg.getSegmentID(), dummySeg);
        
        Venue dummyVenue = new Venue(venueName, dummyLocation, dummySegments);

        return new VenueDTO(dummyVenue);
    }

    // ==========================================
    // HAPPY PATHS
    // ==========================================

    @Test
    void configureLayoutAndInventory_ValidOwner_ReturnsOkResult() {
        Event mockEvent = mock(Event.class);
        ProductionCompany mockCompany=mock(ProductionCompany.class);
        User mockUser = mock(User.class);
        VenueDTO validDTO = createValidVenueDTO();

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        
        when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);
        doNothing().when(mockCompany).validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);
        
        // Action
        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        // Assert
        assertTrue(result.isSuccess(), "Expected success for valid owner setup");
        assertEquals("Venue layout configured and saved successfully.", result.getValue());
        
        verify(mockEvent, times(1)).setEventString(venueName);
        // We verify that the repository actually received ANY fully constructed Venue object!
        verify(mockVenueRepository, times(1)).addVenue(eq(venueName), any(Venue.class));
        verify(mockEventRepository, times(1)).save(mockEvent);
    }

    @Test
    void configureLayoutAndInventory_ValidManager_ReturnsOkResult() {
        Event mockEvent = mock(Event.class);
        User mockUser = mock(User.class);
        ProductionCompany mockCompany=mock(ProductionCompany.class);
        VenueDTO validDTO = createValidVenueDTO();

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
        when(mockCompany.isOwner(userID)).thenReturn(false); 
        when(mockCompany.isOwner(userID)).thenReturn(true); // is a manager instead
        
        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        assertTrue(result.isSuccess());
    }

    // ==========================================
    // VALIDATION & ERROR PATHS
    // ==========================================

    @Test
    void configureLayoutAndInventory_InvalidToken_ReturnsFailResult() {
        VenueDTO validDTO = createValidVenueDTO();
        when(mockAuthService.validateToken(validToken)).thenReturn(false);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Please log in again.", result.getError());
        verify(mockEventRepository, never()).findByID(anyString());
    }

    @Test
    void configureLayoutAndInventory_TokenRoleIsNotUser_ReturnsFailResult() {
        VenueDTO validDTO = createValidVenueDTO();
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("Admin"); // Wrong role

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Please log in again.", result.getError());
        verify(mockAuthService, never()).extractSubjectFromToken(anyString());
    }

    @Test
    void configureLayoutAndInventory_UserNotFound_ReturnsFailResult() {
        Event mockEvent = mock(Event.class);
        VenueDTO validDTO = createValidVenueDTO();

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        
        when(mockUserRepository.findByID(userID)).thenReturn(null);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("User not found.", result.getError());
        verify(mockVenueRepository, never()).addVenue(anyString(), any(Venue.class));
    }

    @Test
    void configureLayoutAndInventory_UserNotPermitted_ReturnsFailResult() {
        Event mockEvent = mock(Event.class);
        User mockUser = mock(User.class);
        ProductionCompany mockCompany=mock(ProductionCompany.class);
        VenueDTO validDTO = createValidVenueDTO();

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        
        when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);
        
        doThrow(new IllegalArgumentException("Permission denied. You must be an owner or manager of this company.")).when(mockCompany).validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

        // user exists but is neither owner nor manager
        when(mockCompany.isOwner(userID)).thenReturn(false);
        when(mockCompany.isOwner(userID)).thenReturn(false);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Configuration failed: Permission denied. You must be an owner or manager of this company.", result.getError());
    }

    @Test
    void configureLayoutAndInventory_EventDoesNotExist_ReturnsFailResult() {
        VenueDTO validDTO = createValidVenueDTO();
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenThrow(new IllegalArgumentException("Event with ID " + eventID + " not found"));

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Configuration failed: Event with ID " + eventID + " not found", result.getError());
    }

    @Test
    void configureLayoutAndInventory_EventIsAlreadyActive_ReturnsFailResult() {
        Event mockEvent = mock(Event.class);
        VenueDTO validDTO = createValidVenueDTO();

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(true);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Event is already active and is not in creation process.", result.getError());
    }

    @Test
    void configureLayoutAndInventory_DomainThrowsIllegalArgumentException_ReturnsFailResult() {
        Event mockEvent = mock(Event.class);
        User mockUser = mock(User.class);
        VenueDTO validDTO = createValidVenueDTO();

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        when(mockUserRepository.findByID(userID)).thenReturn(mockUser);

        ProductionCompany mockCompany=mock(ProductionCompany.class);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        when(mockCompany.isOwner(userID)).thenReturn(true);

        LocalDateTime badEndTime = startTime.minusHours(5);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, badEndTime);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Event start time must be before end time!"));
        verify(mockVenueRepository, never()).addVenue(anyString(), any());
    }

    @Test
    void configureLayoutAndInventory_GenericSystemException_ReturnsFailResult() {
        VenueDTO validDTO = createValidVenueDTO();
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn("invalid_id_format");

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, validDTO, startTime, endTime);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Configuration failed: For input string: \"invalid_id_format\""));
    }

    @Test
    void configureLayoutAndInventory_NullVenueLayout_TriggersSystemException_ReturnsFailResult() {
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.isUserToken(validToken)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.findByID(String.valueOf(eventID))).thenReturn(mock(Event.class));
        when(mockUserRepository.findByID(userID)).thenReturn(mock(User.class));

        ProductionCompany mockCompany=mock(ProductionCompany.class);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        when(mockCompany.isOwner(userID)).thenReturn(true);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, null, startTime, endTime);

        assertFalse(result.isSuccess());
        
        assertEquals("An unexpected system error occurred while saving the layout.", result.getError());
    }
}