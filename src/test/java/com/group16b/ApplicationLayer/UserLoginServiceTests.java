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
    private String guestToken;

    private final String USER_EMAL = "myEmail";
    private final String USER_PASSWORD = "myPassword";
    private final String WRONG_PASSWORD = "wrongPassword";
    private final String WRONG_EMAIL = "wrongEmail";

    @BeforeEach
    void setUp() {
        realUserRepository = new UserRepositoryMapImpl();
        
        String userSecret = "this-is-a-very-long-and-secure-user-secret-key-123456";
        String adminSecret = "this-is-a-very-long-and-secure-admin-secret-key-654321";
        realTokenService = new AuthenticationServiceJWTImpl(userSecret, adminSecret);
        
        userLoginService = new UserLoginService(realUserRepository, realTokenService);

        testUser = new User(USER_EMAL, USER_PASSWORD);
        realUserRepository.save(testUser);

        validToken = realTokenService.generateVisitor_SignedToken(USER_EMAL);
        guestToken = realTokenService.generateVisitor_GuestToken(new SessionToken());
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
        
        Result<String> result = userLoginService.loginMember(USER_EMAL, USER_PASSWORD,guestToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertTrue(realTokenService.isUserToken(result.getValue())); 
    }

    @Test
    void loginMember_WrongPassword_ReturnsFailResult() {
        Result<String> result = userLoginService.loginMember(USER_EMAL, WRONG_PASSWORD,guestToken);

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password", result.getError());
    }

    @Test
    void loginMember_WrongEmail_ReturnsFailResult() {
        Result<String> result = userLoginService.loginMember(WRONG_EMAIL, USER_PASSWORD,guestToken);

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password", result.getError());
    }

    @Test
    void loginMember_UserDoesNotExist_ReturnsFailResult() {
        Result<String> result = userLoginService.loginMember(WRONG_EMAIL, USER_PASSWORD,guestToken);

        assertFalse(result.isSuccess());
        assertEquals("Invalid user ID or password", result.getError()); 
    }

    @Test
    void loginMember_InvalidGuestToken_ReturnsFailResult() {
        String invalidGuestToken = "this-is-not-a-valid-guest-token";
        Result<String> result = userLoginService.loginMember(USER_EMAL, USER_PASSWORD, invalidGuestToken);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Please refresh your session and try again.", result.getError());
    }

    @Test
    void loginMember_TokenIsNotGuestToken_ReturnsFailResult() {
        String userToken = realTokenService.generateVisitor_SignedToken(USER_EMAL);
        Result<String> result = userLoginService.loginMember(USER_EMAL, USER_PASSWORD, userToken);

         assertFalse(result.isSuccess());
         assertEquals("Authentication failed. Only guests are allowed to login.", result.getError());
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
        realUserRepository.delete(USER_EMAL);

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