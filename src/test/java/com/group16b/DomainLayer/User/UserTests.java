package com.group16b.DomainLayer.User;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;

public class UserTests {

    private User user;
    private IAuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        user = new User("member@test.com", "Password123!");
    }

    @Test
    void constructor_ValidData_CreatesUserWithZeroVersionAndHashedPassword() {
        assertEquals("member@test.com", user.getEmail());
        assertEquals(0L, user.getVersion());
        assertTrue(user.confirmPassword("Password123!"));
    }

    @Test
    void copyConstructor_ValidUser_CopiesAllFieldsExactly() {
        user.setVersion(5L);
        User copiedUser = new User(user);

        assertEquals(user.getEmail(), copiedUser.getEmail());
        assertEquals(user.getVersion(), copiedUser.getVersion());
        
        assertTrue(copiedUser.confirmPassword("Password123!"));
    }


    @Test
    void confirmPassword_CorrectPassword_ReturnsTrue() {
        assertTrue(user.confirmPassword("Password123!"));
    }

    @Test
    void confirmPassword_IncorrectPassword_ReturnsFalse() {
        assertFalse(user.confirmPassword("WrongPassword!"));
    }

    @Test
    void changePassword_ValidOldAndNew_UpdatesPasswordSuccessfully() {
        user.changePassword("Password123!", "NewPassword456!");
        
        assertFalse(user.confirmPassword("Password123!"));
        assertTrue(user.confirmPassword("NewPassword456!"));
    }

    @Test
    void changePassword_WrongOldPassword_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> user.changePassword("WrongOld!", "NewPassword456!"));
            
        assertEquals("Old password is incorrect.", ex.getMessage());
        assertTrue(user.confirmPassword("Password123!")); // verify state didn't change
    }

    @Test
    void changePassword_NewPasswordSameAsOld_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> user.changePassword("Password123!", "Password123!"));
            
        assertEquals("New password cannot be the same as the old password.", ex.getMessage());
    }

    // @Test
    // void update_ValidIncomingUser_MutatesFieldsAndIncrementsVersion() {
    //     User incomingData = new User("updated@test.com", "UpdatedPassword!");
        
    //     user.update(incomingData);

    //     assertEquals("updated@test.com", user.getEmail());
    //     assertEquals(1L, user.getVersion()); // incremented from 0 to 1
    //     assertTrue(user.confirmPassword("UpdatedPassword!"));
    // }

    @Test
    void setVersion_ValidLong_UpdatesVersionDirectly() {
        user.setVersion(99L);
        
        assertEquals(99L, user.getVersion());
    }
}    