package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class UserLoginServiceTests {

    private UserRepositoryMapImpl realUserRepository;
    private AuthenticationServiceJWTImpl realTokenService;
    private UserLoginService userLoginService;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        realUserRepository = new UserRepositoryMapImpl();
        
        String userSecret = "this-is-a-very-long-and-secure-user-secret-key-123456";
        String adminSecret = "this-is-a-very-long-and-secure-admin-secret-key-654321";
        realTokenService = new AuthenticationServiceJWTImpl(userSecret, adminSecret);
        
        userLoginService = new UserLoginService(realUserRepository, realTokenService);

        testUser = new User("myEmail", "myPassword");
        realUserRepository.save(testUser);

        validToken = realTokenService.generateVisitor_SignedToken("myEmail");
    }

    @Test
    void createGuestSession_Success_ReturnsOkResult() {
        Result<String> result = userLoginService.createGuestSession();

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertFalse(realTokenService.isUserToken(result.getValue())); // Verify it acts as a guest token
    }

    @Test
    void createGuestSession_ServiceThrowsException_ReturnsFailResult() {
        UserLoginService brokenService = new UserLoginService(realUserRepository, null);
        
        Result<String> result = brokenService.createGuestSession();

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("An unexpected error occurred")); 
    }

    @Test
    void loginMember_ValidCredentials_ReturnsOkResult() {
        Result<String> result = userLoginService.loginMember("myEmail", "myPassword");

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertTrue(realTokenService.isUserToken(result.getValue())); 
    }

    @Test
    void loginMember_WrongPassword_ReturnsFailResult() {
        Result<String> result = userLoginService.loginMember("myEmail", "wrongPassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password", result.getError());
    }

    @Test
    void loginMember_WrongEmail_ReturnsFailResult() {
        Result<String> result = userLoginService.loginMember("my$Email", "myPassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password", result.getError());
    }

    @Test
    void loginMember_UserDoesNotExist_ReturnsFailResult() {
        Result<String> result = userLoginService.loginMember("999", "anyPassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password", result.getError()); 
    }

    @Test
    void logOutMember_SuccessfulLogout_ReturnsNewGuestToken() {
        Result<String> result = userLoginService.logOutMember(validToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertFalse(realTokenService.isUserToken(result.getValue()));
    }

    @Test
    void logOutMember_TokenIsNotUserToken_ReturnsFailResult() {
        SessionToken guestSession = new SessionToken();
        String guestToken = realTokenService.generateVisitor_GuestToken(guestSession);

        Result<String> result = userLoginService.logOutMember(guestToken);

        assertFalse(result.isSuccess());
        assertEquals("Invalid token for logout", result.getError());
    }

    @Test
    void logOutMember_UserDoesNotExistInDB_ReturnsFailResult() {
        realUserRepository.delete("myEmail");

        Result<String> result = userLoginService.logOutMember(validToken);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Failed to log out"));
    }

    @Test
    void logOutMember_MalformedTokenException_ReturnsFailResult() {
        String badToken = "malformed.token";

        Result<String> result = userLoginService.logOutMember(badToken);

        assertFalse(result.isSuccess());
        assertEquals("Invalid token for logout", result.getError());   }
}