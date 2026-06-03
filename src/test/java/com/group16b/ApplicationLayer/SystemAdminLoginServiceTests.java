package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.MapDBs.SystemAdminRepositoryMapImpl;

public class SystemAdminLoginServiceTests {
    private SystemAdminRepositoryMapImpl realAdminRepository;
    private AuthenticationServiceJWTImpl realTokenService;
    private SystemAdminLoginService     adminService;

    private final String ADMIN_USERNAME = "admin1";
    private final String ADMIN_PASSWORD = "password123";
    private final String ADMIN_EMAIL    = "admin@mail.ru";

    private final String NONEXISTENT_USERNAME = "ghostAdmin";
    private final String WRONG_PASSWORD       = "wrongPassword";
    private final String WRONG_EMAIL          = "wrong@mail.ru";

    private String validAdminToken;   // token that belongs to admin1
    private String guestToken;        // fresh guest/visitor token


    @BeforeEach
    void setUp() {
        realAdminRepository = new SystemAdminRepositoryMapImpl();

        String userSecret  = "this-is-a-very-long-and-secure-user-secret-key-123456";
        String adminSecret = "this-is-a-very-long-and-secure-admin-secret-key-654321";
        realTokenService = new AuthenticationServiceJWTImpl(userSecret, adminSecret);

        adminService = new SystemAdminLoginService(realAdminRepository, realTokenService);

        SystemAdmin admin = new SystemAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL);
        realAdminRepository.save(admin);

        validAdminToken = realTokenService.generateAdminToken(ADMIN_USERNAME);
        guestToken      = realTokenService.generateVisitor_GuestToken(new SessionToken());
    }


    @Test
    void loginAdmin_ValidCredentials_ReturnsAdminToken() {
        Result<String> result = adminService.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, guestToken);

        assertTrue(result.isSuccess());
        assertTrue(realTokenService.isAdminToken(result.getValue()),
                "Returned token must be recognised as an admin token");
    }

    @Test
    void loginAdmin_WrongPassword_ReturnsFailResult() {
        Result<String> result = adminService.loginAdmin(ADMIN_USERNAME, WRONG_PASSWORD, ADMIN_EMAIL, guestToken);

        assertFalse(result.isSuccess());
        assertEquals("invalid password or email", result.getError());
    }

    @Test
    void loginAdmin_WrongEmail_ReturnsFailResult() {
        Result<String> result = adminService.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, WRONG_EMAIL, guestToken);

        assertFalse(result.isSuccess());
        assertEquals("invalid password or email", result.getError());
    }

    @Test
    void loginAdmin_NonexistentUsername_ReturnsFailResult() {
        Result<String> result = adminService.loginAdmin(NONEXISTENT_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, guestToken);

        assertFalse(result.isSuccess());
        assertEquals("System admin with username ghostAdmin does not exist.", result.getError());
    }

    @Test
    void loginAdmin_InvalidToken_ReturnsFailResult() {
        String invalidToken = "this-is-not-a-valid-token";
        Result<String> result = adminService.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, invalidToken);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Please refresh your session and try again.", result.getError());
    }

    @Test
    void loginAdmin_NonGuestToken_ReturnsFailResult() {
        Result<String> result = adminService.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, validAdminToken);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Only guests are allowed to login.", result.getError());
    }


    @Test
    void loginAdmin_SuccessDoesNotAlterStoredAdminCredentials() {
        adminService.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, guestToken);

        SystemAdmin persisted = realAdminRepository.findByID(ADMIN_USERNAME);
        assertEquals(ADMIN_USERNAME, persisted.getUsername());
        assertEquals(ADMIN_EMAIL,    persisted.getEmail());
    }

    @Test
    void loginAdmin_FailureDoesNotAlterStoredAdminCredentials() {
        adminService.loginAdmin(ADMIN_USERNAME, WRONG_PASSWORD, ADMIN_EMAIL, guestToken);

        SystemAdmin persisted = realAdminRepository.findByID(ADMIN_USERNAME);
        assertEquals(ADMIN_USERNAME, persisted.getUsername());
        assertEquals(ADMIN_EMAIL,    persisted.getEmail());
    }

    @Test
    void logOutAdmin_ValidAdminToken_ReturnsGuestToken() {
        Result<String> result = adminService.logOutAdmin(validAdminToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertFalse(realTokenService.isAdminToken(result.getValue()),
                "Token returned after logout must NOT be an admin token");
        assertTrue(realTokenService.isGuestToken(result.getValue()),
                "Token returned after logout must be a guest token");
    }

    @Test
    void logOutAdmin_GuestToken_ReturnsFailResult() {
        Result<String> result = adminService.logOutAdmin(guestToken);

        assertFalse(result.isSuccess());
        assertEquals("invalid Session for logout", result.getError());
    }

    @Test
    void logOutAdmin_UserToken_ReturnsFailResult() {
        String userToken = realTokenService.generateVisitor_SignedToken(ADMIN_USERNAME);
        Result<String> result = adminService.logOutAdmin(userToken);

        assertFalse(result.isSuccess());
        assertEquals("invalid Session for logout", result.getError());
    }

    @Test
    void logOutAdmin_MalformedToken_ReturnsFailResult() {
        Result<String> result = adminService.logOutAdmin("malformed.token.xyz");

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed. Please refresh your session and try again.", result.getError());
    }

    @Test
    void logOutAdmin_AdminNotInRepository_ReturnsFailResult() {
        realAdminRepository.delete(ADMIN_USERNAME);
        Result<String> result = adminService.logOutAdmin(validAdminToken);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().startsWith("Failed to log out:"),
                "Error message should indicate logout failure, got: " + result.getError());
    }

    @Test
    void logOutAdmin_SuccessDoesNotAlterStoredAdmin() {
        adminService.logOutAdmin(validAdminToken);

        SystemAdmin persisted = realAdminRepository.findByID(ADMIN_USERNAME);
        assertNotNull(persisted);
        assertEquals(ADMIN_USERNAME, persisted.getUsername());
    }


    @Test
    void loginAdmin_OptimisticLockConflictOnFirstAttempt_EventuallySucceeds() {
        SystemAdmin currentAdmin = realAdminRepository.findByID(ADMIN_USERNAME); 
        realAdminRepository.save(currentAdmin);

        Result<String> result = adminService.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, guestToken);

        assertTrue(result.isSuccess(),
                "Service should retry on optimistic lock conflict and not propagate the failure");
        assertTrue(realTokenService.isAdminToken(result.getValue()));
    }


    @Test
    void logOutAdmin_OptimisticLockConflictOnFirstAttempt_EventuallySucceeds() {
        SystemAdmin currentAdmin = realAdminRepository.findByID(ADMIN_USERNAME); 
        realAdminRepository.save(currentAdmin);
        Result<String> result = adminService.logOutAdmin(validAdminToken);

        assertTrue(result.isSuccess(),
                "Service should retry on optimistic lock conflict and not propagate the failure");
        assertTrue(realTokenService.isGuestToken(result.getValue()));
    }


    @Test
    void loginAdmin_ConcurrentLogins_BothEventuallySucceed() throws InterruptedException {
        String guestToken2 = realTokenService.generateVisitor_GuestToken(new SessionToken());

        Result<String>[] results = new Result[2];

        Thread t1 = new Thread(() ->
                results[0] = adminService.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, guestToken));
        Thread t2 = new Thread(() ->
                results[1] = adminService.loginAdmin(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, guestToken2));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(results[0].isSuccess(), "First concurrent login must succeed");
        assertTrue(results[1].isSuccess(), "Second concurrent login must succeed");
    }


    @Test
    void logOutAdmin_ConcurrentLogouts_RepositoryRemainsConsistent() throws InterruptedException {
        Result<String>[] results = new Result[2];

        Thread t1 = new Thread(() -> results[0] = adminService.logOutAdmin(validAdminToken));
        Thread t2 = new Thread(() -> results[1] = adminService.logOutAdmin(validAdminToken));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        boolean atLeastOneSuccess = (results[0] != null && results[0].isSuccess())
                || (results[1] != null && results[1].isSuccess());
        assertTrue(atLeastOneSuccess, "At least one concurrent logout must succeed");

        SystemAdmin persisted = realAdminRepository.findByID(ADMIN_USERNAME);
        assertNotNull(persisted, "Admin record must remain in the repository after concurrent logouts");
    }
}