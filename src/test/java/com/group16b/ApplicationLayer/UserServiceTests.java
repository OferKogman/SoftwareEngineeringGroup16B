package com.group16b.ApplicationLayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.TicketGateway; 

public class UserServiceTests {

    private UserRepositoryMapImpl userRepo;
    private AuthenticationServiceJWTImpl authService;
    private UserService userService;
    private String sessionToken;
    private String noOrdersToken;
    private String guestToken;
    private String adminToken;


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

        // for get user order history tests, we need to have a user with orders in the repo and a valid token for that user
        sessionToken = authService.generateVisitor_SignedToken("test2@test.com");
        noOrdersToken = authService.generateVisitor_SignedToken("noorders@test.com");
        guestToken = authService.generateVisitor_GuestToken(new SessionToken("guest-session"));
        adminToken = authService.generateAdminToken("admin@test.com");
        User tUser = new User("test2@test.com", "Password123!");
        userRepo.save(tUser);
        Order orderSeat1 = new Order( "seg1", List.of("A1", "A2"), 100.0, 1, "test2@test.com");
        orderSeat1.CompleteOrder();
        Order orderSeat2 = new Order( "seg2", List.of("A3", "A4"), 150.0, 1, "test2@test.com");
        orderSeat2.CompleteOrder();
        Order orderSeat3 = new Order( "seg3", List.of("B1"), 50.0, 2, "test2@test.com");
        orderSeat3.CompleteOrder();
        Order orderAmount1 = new Order( "seg4", 3, 75.0, 3, "test2@test.com");
        orderAmount1.CompleteOrder();
        Order orderAmount2 = new Order( "seg5", 2, 50.0, 3, "test2@test.com");
        orderAmount2.CompleteOrder();
        Order orderAmount3 = new Order( "seg6", 1, 25.0, 4, "test2@test.com");
        orderRepo.save(orderSeat1);
        orderRepo.save(orderSeat2);
        orderRepo.save(orderSeat3);
        orderRepo.save(orderAmount1);
        orderRepo.save(orderAmount2);
        orderRepo.save(orderAmount3);

        Order otherOrder1 = new Order("seg7", List.of("C1"), 30.0, 5, "other@test.com");
        orderRepo.save(otherOrder1);
        otherOrder1.CompleteOrder();
        
        Order otherOrder2 = new Order("seg8", 4, 100.0, 6, "other@test.com");
        otherOrder2.CompleteOrder();
        orderRepo.save(otherOrder2);

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
        assertTrue(result.getError().contains("Invalid token for user update password"));
    }

    @Test
    void updateUserPassword_IncorrectTypewtToken_FailsAndDoesNotUpdateDomain() {
        userRepo.save(new User("member@test.com", "OldPassword123!"));

        //even admin with the same email as user can't be user with token admin
        String IncorrectTypeToken = authService.generateAdminToken("member@test.com");

        //so we know that error occurs only because of illegal token type for user functions
        assertEquals("member@test.com", authService.extractSubjectFromToken(IncorrectTypeToken));

        Result<Boolean> result = userService.updateUserPassword(IncorrectTypeToken, "OldPassword123!", "NewPassword456!");

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Invalid token for user"));
    }   
    
    @Test
    void getUserOrderHistory_validToken_returnsUserOrders() {
        Result<List<OrderDTO>> result = userService.getUserOrderHistory(sessionToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertEquals(5, result.getValue().size());
    }
    @Test
    void getUserOrderHistory_validTokenWithNoOrders_returnsEmptyList() {
        Result<List<OrderDTO>> result = userService.getUserOrderHistory(noOrdersToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertEquals(0, result.getValue().size());
    }

    @Test
    void getUserOrderHistory_guestToken_returnsFail() {
        Result<List<OrderDTO>> result = userService.getUserOrderHistory(guestToken);

        assertFalse(result.isSuccess());
    }

    @Test
    void getUserOrderHistory_adminToken_returnsFail() {

        Result<List<OrderDTO>> result = userService.getUserOrderHistory(adminToken);
        assertFalse(result.isSuccess());
    }

    @Test
    void getUserOrderHistory_invalidToken_returnsFail() {
        String invalidToken = "this-is-not-a-real-token-because-chaos";

        Result<List<OrderDTO>> result = userService.getUserOrderHistory(invalidToken);
        assertFalse(result.isSuccess());
    }

    @Test
    void getUserOrderHistory_nullToken_returnsFail() {
        Result<List<OrderDTO>> result = userService.getUserOrderHistory(null);
        assertFalse(result.isSuccess());
    }

    @Test
    void getUserOrderHistory_blankToken_returnsFail() {
        Result<List<OrderDTO>> result = userService.getUserOrderHistory("");
        assertFalse(result.isSuccess());
    }

    @Test
    void getUserOrderHistory_returnsOnlyOrdersOfTokenUser() {
        
        Result<List<OrderDTO>> result = userService.getUserOrderHistory(sessionToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertEquals(5, result.getValue().size());
    }
}