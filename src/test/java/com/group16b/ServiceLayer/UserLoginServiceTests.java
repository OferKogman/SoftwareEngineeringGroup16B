package com.group16b.ServiceLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Result;
import com.group16b.ApplicationLayer.UserLoginService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;

public class UserLoginServiceTests {

    private IUserRepository mockUserRepository;
    private IAuthenticationService mockTokenService;
    private UserLoginService userLoginService;

    @BeforeEach
    void setUp() {
        mockUserRepository = mock(IUserRepository.class);
        mockTokenService = mock(IAuthenticationService.class);
        userLoginService = new UserLoginService(mockUserRepository, mockTokenService);
    }


    @Test
    void createGuestSession_Success_ReturnsOkResult() {
        when(mockTokenService.generateVisitor_GuestToken(any(SessionToken.class)))
                .thenReturn("mock.guest.token");

        Result<String> result = userLoginService.createGuestSession();

        assertTrue(result.isSuccess());
        assertEquals("mock.guest.token", result.getValue());
        verify(mockTokenService, times(1)).generateVisitor_GuestToken(any(SessionToken.class));
    }

    @Test
    void createGuestSession_ServiceThrowsException_ReturnsFailResult() {
        when(mockTokenService.generateVisitor_GuestToken(any(SessionToken.class)))
                .thenThrow(new RuntimeException("System error"));

        Result<String> result = userLoginService.createGuestSession();

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Failed to create guest session"));//there is exception, not sure whitch
    }


@Test
    void loginMember_ValidCredentials_ReturnsOkResult() {
        int userID = 1;
        String correctPassword = "myPassword";
        
        User mockUser = mock(User.class);
        when(mockUser.confirmPassword(correctPassword)).thenReturn(true); 

        when(mockUserRepository.userExists(userID)).thenReturn(true);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockTokenService.generateVisitor_SignedToken(userID)).thenReturn("mock.member.token");

        Result<String> result = userLoginService.loginMember(userID, correctPassword);

        assertTrue(result.isSuccess());
        assertEquals("mock.member.token", result.getValue());
    }

    @Test
    void loginMember_WrongPassword_ReturnsFailResult() {
        int userID = 1;
        String wrongPassword = "wrongPassword";
        
        User mockUser = mock(User.class);
        when(mockUser.confirmPassword(wrongPassword)).thenReturn(false); 

        when(mockUserRepository.userExists(userID)).thenReturn(true);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);

        Result<String> result = userLoginService.loginMember(userID, wrongPassword);

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password", result.getError());
    }

    @Test
    void loginMember_UserDoesNotExist_ReturnsFailResult() {
        int nonExistentUserID = 999;
        when(mockUserRepository.userExists(nonExistentUserID)).thenReturn(false);

        Result<String> result = userLoginService.loginMember(nonExistentUserID, "anyPassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password", result.getError());
        
        verify(mockUserRepository, never()).getUserByID(anyInt());
        verify(mockTokenService, never()).generateVisitor_SignedToken(anyInt());
    }

}