package com.group16b.ApplicationLayer;

import java.lang.reflect.Field;

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
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;

public class UserLoginServiceTests {

    private IUserRepository mockUserRepository;
    private IAuthenticationService mockTokenService;
    private UserLoginService userLoginService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        mockUserRepository = mock(IUserRepository.class);
        mockTokenService = mock(IAuthenticationService.class);
        userLoginService = new UserLoginService(mockTokenService);
        Field userRepo = userLoginService.getClass().getDeclaredField("userRepository");
        userRepo.setAccessible(true);
        userRepo.set(userLoginService, mockUserRepository);
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
        String correctMail = "myEmail";
        
        User mockUser = mock(User.class);
        when(mockUser.confirmPassword(correctPassword)).thenReturn(true); 
        when(mockUser.getEmail()).thenReturn(correctMail);

        when(mockUserRepository.userExists(userID)).thenReturn(true);
        when(mockUserRepository.getUserByEmail(userID)).thenReturn(mockUser);
        when(mockTokenService.generateVisitor_SignedToken(userID)).thenReturn("mock.member.token");

        Result<String> result = userLoginService.loginMember(userID, correctPassword, correctMail);

        assertTrue(result.isSuccess());
        assertEquals("mock.member.token", result.getValue());
    }

    @Test
    void loginMember_WrongPassword_ReturnsFailResult() {
        int userID = 1;
        String wrongPassword = "wrongPassword";
        String mail = "someEmail";
        
        User mockUser = mock(User.class);
        when(mockUser.confirmPassword(wrongPassword)).thenReturn(false); 
        when(mockUser.getEmail()).thenReturn(mail);

        when(mockUserRepository.userExists(userID)).thenReturn(true);
        when(mockUserRepository.getUserByEmail(userID)).thenReturn(mockUser);

        Result<String> result = userLoginService.loginMember(userID, wrongPassword, mail);

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password + email", result.getError());
    }

    
    @Test
    void loginMember_WrongEmail_ReturnsFailResult() {
        int userID = 1;
        String correctPassword = "myPassword";
        String correctMail = "myEmail";
        String wrongMail = "my$Email";
        
        User mockUser = mock(User.class);
        when(mockUser.confirmPassword(correctPassword)).thenReturn(true); 
        when(mockUser.getEmail()).thenReturn(correctMail);

        when(mockUserRepository.userExists(userID)).thenReturn(true);
        when(mockUserRepository.getUserByEmail(userID)).thenReturn(mockUser);
        when(mockTokenService.generateVisitor_SignedToken(userID)).thenReturn("mock.member.token");

        Result<String> result = userLoginService.loginMember(userID, correctPassword, wrongMail);

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password + email", result.getError());
    }

    @Test
    void loginMember_UserDoesNotExist_ReturnsFailResult() {
        int nonExistentUserID = 999;
        when(mockUserRepository.userExists(nonExistentUserID)).thenReturn(false);

        Result<String> result = userLoginService.loginMember(nonExistentUserID, "anyPassword", "someMail");

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID", result.getError());
        
        verify(mockUserRepository, never()).getUserByEmail(anyInt());
        verify(mockTokenService, never()).generateVisitor_SignedToken(anyInt());
    }

    @Test
    void logOutMember_SuccessfulLogout_ReturnsNewGuestToken() {
        String validToken = "valid.user.token";
        String newGuestToken = "new.guest.token";
        int userID = 123;
        
        when(mockTokenService.extractSubjectFromToken(validToken)).thenReturn(String.valueOf(userID));
        when(mockUserRepository.userExists(userID)).thenReturn(true);
        when(mockTokenService.isUserToken(validToken)).thenReturn(true);
        
        when(mockTokenService.generateVisitor_GuestToken(any(SessionToken.class))).thenReturn(newGuestToken);

        Result<String> result = userLoginService.logOutMember(validToken);

        assertTrue(result.isSuccess());
        assertEquals(newGuestToken, result.getValue());
    }

    @Test
    void logOutMember_TokenIsNotUserToken_ReturnsFailResult() {
        String guestToken = "guest.token";
        int guestID = 0; 
        
        when(mockTokenService.extractRoleFromToken(guestToken)).thenReturn("Guest");
        when(mockTokenService.extractSubjectFromToken(guestToken)).thenReturn(String.valueOf(guestID));//shouldnt be relevant won;t parse subject since incorrect role
        when(mockUserRepository.userExists(guestID)).thenReturn(true);//should crash regardless since irrelevant token

        Result<String> result = userLoginService.logOutMember(guestToken);

        assertFalse(result.isSuccess());
        assertEquals("Invalid ID for logout", result.getError());
    }

    @Test
    void logOutMember_UserDoesNotExistInDB_ReturnsFailResult() {
        String validToken = "valid.user.token";
        int nonExistentID = 999;
        
        when(mockTokenService.isUserToken(validToken)).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken(validToken)).thenReturn(String.valueOf(nonExistentID));
        when(mockUserRepository.userExists(nonExistentID)).thenReturn(false);

        Result<String> result = userLoginService.logOutMember(validToken);

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID", result.getError());
    }

    @Test
    void logOutMember_MalformedTokenException_ReturnsFailResult() {
        String badToken = "malformed.token";
        
        when(mockTokenService.extractSubjectFromToken(badToken))
                .thenThrow(new RuntimeException("Invalid token signature"));

        Result<String> result = userLoginService.logOutMember(badToken);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Failed to log out: Invalid token signature"));
    }

}