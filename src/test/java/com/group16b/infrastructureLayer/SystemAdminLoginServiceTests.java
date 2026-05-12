package com.group16b.infrastructureLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.SystemAdminLoginService;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;

public class SystemAdminLoginServiceTests {

    // Replace AdminManagementService with the actual name of your service class
    private SystemAdminLoginService adminService;
    private IAuthenticationService mockTokenService;
    private ISystemAdminRepository mockSystemAdminRepository;

    @BeforeEach
    void setUp() throws Exception {
        mockTokenService = mock(IAuthenticationService.class);
        mockSystemAdminRepository = mock(ISystemAdminRepository.class);
        
        // Assuming your service takes these in the constructor. 
        // If it uses reflection/singletons like the previous classes, apply the same reflection setup here!
        adminService = new SystemAdminLoginService(mockSystemAdminRepository, mockTokenService);
    }

    // -----------------------------------------------------------------
    //  SUCCESS SCENARIO
    // -----------------------------------------------------------------
    @Test
    void testLogOutAdmin_Success() {
        String sessionToken = "valid-admin-token";
        int adminID = 1;
        String expectedGuestToken = "new-guest-token-123";

        // 1. Extract subject returns a valid ID string
        when(mockTokenService.extractSubjectFromToken(sessionToken)).thenReturn(String.valueOf(adminID));
        // 2. Token is confirmed as an admin token
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(true);
        // 3. Admin exists in the repository
        when(mockSystemAdminRepository.doesSystemAdminExist(adminID)).thenReturn(true);
        // 4. Generating the guest token works
        when(mockTokenService.generateVisitor_GuestToken(any())).thenReturn(expectedGuestToken);

        Result<String> result = adminService.logOutAdmin(sessionToken);

        assertTrue(result.isSuccess(), "Logout should be successful");
        assertEquals(expectedGuestToken, result.getValue(), "Should return the newly generated guest token");
    }

    // -----------------------------------------------------------------
    //  FAILURE: TOKEN IS NOT AN ADMIN TOKEN
    // -----------------------------------------------------------------
    @Test
    void testLogOutAdmin_NotAdminToken_Fail() {
        String sessionToken = "standard-user-token";
        int userID = 5;

        // Extracts successfully, but isAdminToken returns false
        when(mockTokenService.extractSubjectFromToken(sessionToken)).thenReturn(String.valueOf(userID));
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(false);

        Result<String> result = adminService.logOutAdmin(sessionToken);

        assertFalse(result.isSuccess(), "Logout should fail if token is not an admin token");
        assertEquals("Invalid ID for logout", result.getError());
        
        // Verify we never checked the database since it failed early
        verify(mockSystemAdminRepository, never()).doesSystemAdminExist(anyInt());
    }

    // -----------------------------------------------------------------
    //  FAILURE: ADMIN DOES NOT EXIST IN REPOSITORY
    // -----------------------------------------------------------------
    @Test
    void testLogOutAdmin_AdminDoesNotExist_Fail() {
        String sessionToken = "deleted-admin-token";
        int ghostAdminID = 99;

        when(mockTokenService.extractSubjectFromToken(sessionToken)).thenReturn(String.valueOf(ghostAdminID));
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(true);
        
        // The admin token is valid, but the user doesn't exist in the DB anymore
        when(mockSystemAdminRepository.doesSystemAdminExist(ghostAdminID)).thenReturn(false);

        Result<String> result = adminService.logOutAdmin(sessionToken);

        assertFalse(result.isSuccess(), "Logout should fail if admin ID does not exist in DB");
        assertEquals("Invalid adminID ID", result.getError());
    }

    // -----------------------------------------------------------------
    //  FAILURE: EXCEPTION THROWN (e.g. malformed token)
    // -----------------------------------------------------------------
    @Test
    void testLogOutAdmin_ExceptionThrown_Fail() {
        String badToken = "malformed-token";

        // Force the parsing to throw an exception (e.g. if the subject isn't an integer)
        when(mockTokenService.extractSubjectFromToken(badToken)).thenThrow(new NumberFormatException("Not a number"));

        Result<String> result = adminService.logOutAdmin(badToken);

        assertFalse(result.isSuccess(), "Logout should catch exceptions and return a failure Result");
        assertTrue(result.getError().contains("Failed to log out"), "Error message should contain exception prefix");
    }
}