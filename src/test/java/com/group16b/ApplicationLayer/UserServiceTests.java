package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.TicketGateway; 

public class UserServiceTests {

    private UserRepositoryMapImpl userRepo;
    private AuthenticationServiceJWTImpl authService;
    private UserService userService;

@BeforeEach
    void setUp() {
        userRepo = new UserRepositoryMapImpl();
        

        String userSecret = "this-is-a-very-long-and-secure-user-secret-key-123456";
        String adminSecret = "this-is-a-very-long-and-secure-admin-secret-key-654321";
        
        authService = new AuthenticationServiceJWTImpl(userSecret, adminSecret);
        
        OrderRepositoryMapImpl orderRepo = new OrderRepositoryMapImpl();
        VenueRepositoryMapImpl venueRepo = new VenueRepositoryMapImpl();
        EventRepositoryMapImpl eventRepo = new EventRepositoryMapImpl();
        TicketGateway ticketGateway = new TicketGateway();

        userService = new UserService(authService, ticketGateway, venueRepo, userRepo, orderRepo, eventRepo);
    }

    @Test
    void registerUser_ValidData_SavesUserToRepository() {
        Result<UserDTO> result = userService.registerUser("test@test.com", "Password123!");

        assertTrue(result.isSuccess());
        assertNotNull(userRepo.findByID("test@test.com"));
    }

    @Test
    void registerUser_UserAlreadyExists_FailsAndDoesNotDuplicate() {
        userRepo.save(new User("existing@test.com", "Password123!"));

        Result<UserDTO> result = userService.registerUser("existing@test.com", "NewPassword456!");

        assertFalse(result.isSuccess());
        assertEquals("User already exists", result.getError());
        
        User domainUser = userRepo.findByID("existing@test.com");
        assertTrue(domainUser.confirmPassword("Password123!"));
    }

    @Test
    void updateUserPassword_ValidData_UpdatesDomainState() {
        userRepo.save(new User("member@test.com", "OldPassword123!"));
        String token = authService.generateVisitor_SignedToken("member@test.com");

        Result<Boolean> result = userService.updateUserPassword(token, "OldPassword123!", "NewPassword456!");

        assertTrue(result.isSuccess());
        User updatedUser = userRepo.findByID("member@test.com");
        assertTrue(updatedUser.confirmPassword("NewPassword456!"));
    }

    @Test
    void updateUserPassword_WrongOldPassword_FailsAndDoesNotUpdateDomain() {
        userRepo.save(new User("member@test.com", "OldPassword123!"));
        String token = authService.generateVisitor_SignedToken("member@test.com");

        Result<Boolean> result = userService.updateUserPassword(token, "WrongPassword!", "NewPassword456!");

        assertFalse(result.isSuccess());
        assertEquals("An unexpected error occurred: Old password is incorrect.", result.getError());

        User untouchedUser = userRepo.findByID("member@test.com");
        assertTrue(untouchedUser.confirmPassword("OldPassword123!"));
        assertFalse(untouchedUser.confirmPassword("NewPassword456!"));
    }

    @Test
    void updateUserPassword_InvalidJwtToken_FailsAndDoesNotUpdateDomain() {
        userRepo.save(new User("member@test.com", "OldPassword123!"));
        String badToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.token";

        Result<Boolean> result = userService.updateUserPassword(badToken, "OldPassword123!", "NewPassword456!");

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Authentication failed"));
    }
}