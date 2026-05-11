package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueEventConfigServiceTests {

    private IVenueRepository mockVenueRepository;
    private IEventRepository mockEventRepository;
    private IUserRepository mockUserRepository;
    private IAuthenticationService mockAuthService;
    private VenueEventConfigService configService;

    // Test Data
    private final String validToken = "valid.user.token";
    private final String userIDString = "1";
    private final int userID = 1;
    private final int companyID = 10;
    private final int eventID = 100;
    private final String venueName = "Madison Square Garden";
    private final LocalDateTime startTime = LocalDateTime.now().plusDays(1);
    private final LocalDateTime endTime = startTime.plusHours(3);

    @BeforeEach
    void setUp() {
        mockVenueRepository = mock(IVenueRepository.class);
        mockEventRepository = mock(IEventRepository.class);
        mockUserRepository = mock(IUserRepository.class);
        mockAuthService = mock(IAuthenticationService.class);
        
        configService = new VenueEventConfigService(
                mockVenueRepository, 
                mockEventRepository, 
                mockUserRepository, 
                mockAuthService
        );
    }

    // ==========================================
    // HAPPY PATHS
    // ==========================================

    @Test
    void configureLayoutAndInventory_ValidOwner_ReturnsOkResult() {
        Event mockEvent = mock(Event.class);
        User mockUser = mock(User.class);
        Venue mockVenue = mock(Venue.class);

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.EventExists(eventID)).thenReturn(true);
        when(mockEventRepository.getEventByID(eventID)).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true); 
        
        when(mockVenue.getName()).thenReturn(venueName);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertTrue(result.isSuccess());
        assertEquals("Venue layout configured and saved successfully.", result.getValue());
        
        verify(mockVenue, times(1)).bookEvent(startTime, endTime, eventID);
        verify(mockEvent, times(1)).setEventString(venueName);
        verify(mockVenueRepository, times(1)).saveVenue(venueName, mockVenue);
        verify(mockEventRepository, times(1)).updateEvent(mockEvent);
    }

    @Test
    void configureLayoutAndInventory_ValidManager_ReturnsOkResult() {
        Event mockEvent = mock(Event.class);
        User mockUser = mock(User.class);
        Venue mockVenue = mock(Venue.class);

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.EventExists(eventID)).thenReturn(true);
        when(mockEventRepository.getEventByID(eventID)).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(false); 
        when(mockUser.managerInCompany(companyID)).thenReturn(true); //is a manager instead
        
        when(mockVenue.getName()).thenReturn(venueName);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertTrue(result.isSuccess());
    }


    @Test
    void configureLayoutAndInventory_InvalidToken_ReturnsFailResult() {
        Venue mockVenue = mock(Venue.class);
        when(mockAuthService.validateToken(validToken)).thenReturn(false);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Please log in again.", result.getError());
        verify(mockEventRepository, never()).EventExists(anyInt());
    }

    @Test
    void configureLayoutAndInventory_TokenRoleIsNotUser_ReturnsFailResult() {
        Venue mockVenue = mock(Venue.class);
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        // E.g., An Admin token trying to hit a User endpoint
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("Admin");

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Please log in again.", result.getError());
        verify(mockAuthService, never()).extractSubjectFromToken(anyString());
    }

    @Test
    void configureLayoutAndInventory_UserNotFound_ReturnsFailResult() {
        Event mockEvent = mock(Event.class);
        Venue mockVenue = mock(Venue.class);

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.EventExists(eventID)).thenReturn(true);
        when(mockEventRepository.getEventByID(eventID)).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        
        when(mockUserRepository.getUserByID(userID)).thenReturn(null);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Permission denied. You must be an owner or manager of this company.", result.getError());
        verify(mockVenue, never()).bookEvent(any(), any(), anyInt());
    }

    @Test
    void configureLayoutAndInventory_UserNotPermitted_ReturnsFailResult() {
        Event mockEvent = mock(Event.class);
        User mockUser = mock(User.class);
        Venue mockVenue = mock(Venue.class);

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.EventExists(eventID)).thenReturn(true);
        when(mockEventRepository.getEventByID(eventID)).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        
        // user exists but is neither owner nor manager
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(false);
        when(mockUser.managerInCompany(companyID)).thenReturn(false);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Permission denied. You must be an owner or manager of this company.", result.getError());
    }

    @Test
    void configureLayoutAndInventory_EventDoesNotExist_ReturnsFailResult() {
        Venue mockVenue = mock(Venue.class);

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.EventExists(eventID)).thenReturn(false);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Event not found.", result.getError());
        verify(mockEventRepository, never()).getEventByID(anyInt());
    }

    @Test
    void configureLayoutAndInventory_EventIsAlreadyActive_ReturnsFailResult() {
        Event mockEvent = mock(Event.class);
        Venue mockVenue = mock(Venue.class);

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.EventExists(eventID)).thenReturn(true);
        when(mockEventRepository.getEventByID(eventID)).thenReturn(mockEvent);
        
        when(mockEvent.isActiveEvent()).thenReturn(true);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("Event is already active and is not in creation process.", result.getError());
    }

    @Test
    void configureLayoutAndInventory_DomainThrowsIllegalArgumentException_ReturnsFailResult() {
        Event mockEvent = mock(Event.class);
        User mockUser = mock(User.class);
        Venue mockVenue = mock(Venue.class);

        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        when(mockEventRepository.EventExists(eventID)).thenReturn(true);
        when(mockEventRepository.getEventByID(eventID)).thenReturn(mockEvent);
        when(mockEvent.isActiveEvent()).thenReturn(false);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        
        doThrow(new IllegalArgumentException("Venue already reserved for this time."))
            .when(mockVenue).bookEvent(startTime, endTime, eventID);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Configuration failed: Venue already reserved for this time."));
        verify(mockVenueRepository, never()).saveVenue(anyString(), any());
    }

    @Test
    void configureLayoutAndInventory_GenericSystemException_ReturnsFailResult() {
        Venue mockVenue = mock(Venue.class);
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn("invalid_id_format");

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, mockVenue, startTime, endTime);

        assertFalse(result.isSuccess());
        // Update the expected string to match the caught IllegalArgumentException
        assertEquals("Configuration failed: For input string: \"invalid_id_format\"", result.getError());
    }

    @Test
    void configureLayoutAndInventory_NullVenueLayout_TriggersSystemException_ReturnsFailResult() {
        when(mockAuthService.validateToken(validToken)).thenReturn(true);
        when(mockAuthService.extractRoleFromToken(validToken)).thenReturn("User");
        when(mockAuthService.extractSubjectFromToken(validToken)).thenReturn(userIDString);
        
        when(mockEventRepository.EventExists(eventID)).thenReturn(true);
        when(mockEventRepository.getEventByID(eventID)).thenReturn(mock(Event.class));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mock(User.class));
        when(mockUserRepository.getUserByID(userID).isOwnerOfCompany(companyID)).thenReturn(true);

        Result<String> result = configService.configureLayoutAndInventory(
                validToken, companyID, eventID, null, startTime, endTime);

        assertFalse(result.isSuccess());
        assertEquals("An unexpected system error occurred while saving the layout.", result.getError());
    }
}