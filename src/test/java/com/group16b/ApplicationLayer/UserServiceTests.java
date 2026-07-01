package com.group16b.ApplicationLayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.web.client.RestTemplate;

import com.group16b.ApplicationLayer.DTOs.ActiveOrderDTO;
import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.ProductionCompanyDTO;
import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.Security.Role;
import com.group16b.InfrastructureLayer.TicketGateway; 

public class UserServiceTests {

    private IRepository<User> userRepo;
    private IOrderRepository orderRepo;
    private AuthenticationServiceJWTImpl authService;
    private UserService userService;
    private String sessionToken;
    private String noOrdersToken;
    private String guestToken;
    private String adminToken;
    private String noCompaniesToken;
    private String nonExistentUserToken;

    private final int USER_COMPANY_ID=1;
    private final int USER_COMPANY_ID_2=3;
    private final int NON_USER_COMPANY_ID=999;

    private final String USER_EMAIL = "test2@test.com";
    private final String OTHER_USER_EMAIL = "noorders@test.com";
    private final String NO_COMPANY_USER_EMAIL = "no-comps@wa.com";
    private final String NON_EXISTENT_USER_EMAIL = "birds are fake";

@BeforeEach
    void setUp() {
        userRepo = new UserRepositoryMapImpl();
        orderRepo = new OrderRepositoryMapImpl();

        String userSecret = "this-is-a-very-long-and-secure-user-secret-key-123456";
        String adminSecret = "this-is-a-very-long-and-secure-admin-secret-key-654321";
        
        authService = new AuthenticationServiceJWTImpl(userSecret, adminSecret);
        
        VenueRepositoryMapImpl venueRepo = new VenueRepositoryMapImpl();
        EventRepositoryMapImpl eventRepo = new EventRepositoryMapImpl();
        TicketGateway ticketGateway = new TicketGateway(new WsepClient(mock(RestTemplate.class), "https://damp-lynna-wsep-1984852e.koyeb.app/"));
        IProductionCompanyRepository productionCompanyRepository = new ProductionCompanyRepositoryMapImpl();

        userService = new UserService(authService, ticketGateway, venueRepo, userRepo, orderRepo, eventRepo, productionCompanyRepository);

        // for get user order history tests, we need to have a user with orders in the repo and a valid token for that user
        sessionToken = authService.generateVisitor_SignedToken(USER_EMAIL);
        noOrdersToken = authService.generateVisitor_SignedToken("noorders@test.com");
        noCompaniesToken = authService.generateVisitor_SignedToken(NO_COMPANY_USER_EMAIL);
        guestToken = authService.generateVisitor_GuestToken(new SessionToken("guest-session"));
        adminToken = authService.generateAdminToken("admin@test.com");
        nonExistentUserToken = authService.generateVisitor_SignedToken(NON_EXISTENT_USER_EMAIL);
        User tUser = new User(USER_EMAIL, "Password123!");
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

        ProductionCompany company1 = new ProductionCompany(USER_COMPANY_ID, "User's Company",0.0,USER_EMAIL);
        ProductionCompany company2 = new ProductionCompany(NON_USER_COMPANY_ID, "Non User's Company",0.0,OTHER_USER_EMAIL);
        ProductionCompany company3 = new ProductionCompany(USER_COMPANY_ID_2, "Other Company",0.0,USER_EMAIL);
        productionCompanyRepository.save(company1);
        productionCompanyRepository.save(company2);
        productionCompanyRepository.save(company3);

        userRepo.save(new User(NO_COMPANY_USER_EMAIL, "Password123!"));
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
        assertEquals("Old password is incorrect.", result.getError());

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

    @Test
    void getAllUserCompanies_validUserToken_returnsNonEmptyCompanyList() {
        Result<List<ProductionCompanyDTO>> result = userService.getAllUserCompanies(sessionToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertEquals(2, result.getValue().size());
        assertTrue(result.getValue().stream().anyMatch(c -> c.getId() == USER_COMPANY_ID));
        assertTrue(result.getValue().stream().anyMatch(c -> c.getId() == USER_COMPANY_ID_2));
    }

    @Test
    void getAllUserCompanies_validUserTokenWithNoCompanies_returnsEmptyCompanyList() {
        Result<List<ProductionCompanyDTO>> result = userService.getAllUserCompanies(noCompaniesToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertEquals(0, result.getValue().size());
    }

    @Test
    void getAllUserCompanies_guestToken_returnsFail() {
        Result<List<ProductionCompanyDTO>> result = userService.getAllUserCompanies(guestToken);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed: Only users are allowed to perform operation", result.getError());
    }

    @Test
    void getAllUserCompanies_adminToken_returnsFail() {
        Result<List<ProductionCompanyDTO>> result = userService.getAllUserCompanies(adminToken);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed: Only users are allowed to perform operation", result.getError());
    }

    @Test
    void getAllUserCompanies_invalidToken_returnsFail() {
        String invalidToken = "this-is-not-a-real-token-because-chaos";

        Result<List<ProductionCompanyDTO>> result = userService.getAllUserCompanies(invalidToken);

        assertFalse(result.isSuccess());
        assertEquals("Authentication failed: Invalid Token", result.getError());
    }

    @Test
    void getAllUserCompanies_nullToken_returnsFail() {
        Result<List<ProductionCompanyDTO>> result = userService.getAllUserCompanies(null);

        assertFalse(result.isSuccess());
        assertEquals("JWT String argument cannot be null or empty.", result.getError());
    }

    @Test
    void getAllUserCompanies_userNotFound_returnsFail() {
        Result<List<ProductionCompanyDTO>> result = userService.getAllUserCompanies(nonExistentUserToken);

        assertFalse(result.isSuccess());
        assertEquals("User with ID "+NON_EXISTENT_USER_EMAIL+" not found.", result.getError());
    }

    @Test
    void getAllUserCompanies_unexpcetedError_returnsFail() {
        IAuthenticationService faultyAuthService = mock(IAuthenticationService.class);
        when(faultyAuthService.validateToken(anyString())).thenThrow(new RuntimeException("Database Exploded!!!!!"));
        UserService faultyUserService = new UserService(faultyAuthService, null, null, null, null, null, null);

        Result<List<ProductionCompanyDTO>> result = faultyUserService.getAllUserCompanies(sessionToken);

        assertFalse(result.isSuccess());
        assertEquals("An unexpected error occurred: Database Exploded!!!!!", result.getError());
    }

    @Test
    void isRoleAdmin_adminToken_returnTrue()
    {
        Result<Boolean> result = userService.isRole(adminToken, Role.ADMIN);
        assertTrue(result.isSuccess());
        assertEquals(true, result.getValue());
    }
    @Test
    void isRoleUser_UserToken_returnTrue()
    {
        Result<Boolean> result = userService.isRole(sessionToken, Role.SIGNED);
        assertTrue(result.isSuccess());
        assertEquals(true, result.getValue());
    }
    @Test
    void isRoleGuest_GuestToken_returnTrue()
    {
        Result<Boolean> result = userService.isRole(guestToken, Role.GUEST);
        assertTrue(result.isSuccess());
        assertEquals(true, result.getValue());
    }
    @Test
    void isRole_InvalidRole_returnFalse()
    {
        Result<Boolean> result = userService.isRole(guestToken, Role.ADMIN);
        assertTrue(result.isSuccess());
        assertEquals(false, result.getValue());
    }
    @Test
    void isRole_BadToekn_returnError()
    {
        Result<Boolean> result = userService.isRole("chi-vap-chi-chi", Role.GUEST);
        assertFalse(result.isSuccess());
        assertEquals("Invalid Token", result.getError());
    }
    @Test
    void isRole_unexpectedError_returnFail()
    {
        IAuthenticationService mockAuthenticationService=mock(IAuthenticationService.class);
        doThrow(new RuntimeException("I recognize the bodies in the water...")).when(mockAuthenticationService).validateToken(anyString());
        userService=new UserService(mockAuthenticationService, null, null, userRepo, null, null, null);
        Result<Boolean> result = userService.isRole(guestToken, Role.GUEST);
        assertFalse(result.isSuccess());
        assertEquals("An unexpected error occured, pls try again later.", result.getError());
    }
    @Test
    void getUserActiveOrder_validToken_returnsActiveOrder() {
        Result<ActiveOrderDTO> result = userService.getUserActiveOrder(sessionToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
    }

    @Test
    void getUserActiveOrder_userWithNoActiveOrder_returnsFail() {
        Result<ActiveOrderDTO> result = userService.getUserActiveOrder(noCompaniesToken);

        assertFalse(result.isSuccess());
        assertEquals("No active order found for user.", result.getError());
    }

    @Test
    void getUserActiveOrder_guestToken_returnsFail() {
        Result<ActiveOrderDTO> result = userService.getUserActiveOrder(guestToken);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Authentication failed"));
    }

    @Test
    void getUserActiveOrder_adminToken_returnsFail() {
        Result<ActiveOrderDTO> result = userService.getUserActiveOrder(adminToken);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Authentication failed"));
    }

    @Test
    void getUserActiveOrder_invalidToken_returnsFail() {
        String invalidToken = "this-is-not-a-real-token-because-chaos";

        Result<ActiveOrderDTO> result = userService.getUserActiveOrder(invalidToken);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Authentication failed"));
    }

    @Test
    void getUserActiveOrder_nullToken_returnsFail() {
        Result<ActiveOrderDTO> result = userService.getUserActiveOrder(null);

        assertFalse(result.isSuccess());
    }

    @Test
    void getUserActiveOrder_blankToken_returnsFail() {
        Result<ActiveOrderDTO> result = userService.getUserActiveOrder("");

        assertFalse(result.isSuccess());
    }

    @Test
    void getUserActiveOrder_userNotFound_returnsFail() {
        Result<ActiveOrderDTO> result = userService.getUserActiveOrder(nonExistentUserToken);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("not found"));
    }

    @Test
    void getUserActiveOrder_ignoresCompletedOrdersAndFindsOnlyActiveOrder() {
        Result<ActiveOrderDTO> result = userService.getUserActiveOrder(sessionToken);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
    }

    @Test
    void getUserActiveOrder_unexpectedError_returnsFail() {
        IAuthenticationService faultyAuthService = mock(IAuthenticationService.class);
        when(faultyAuthService.validateToken(anyString())).thenThrow(new RuntimeException("Database Exploded!!!!!"));

        UserService faultyUserService = new UserService(faultyAuthService, null, null, userRepo, orderRepo, null, null);

        Result<ActiveOrderDTO> result = faultyUserService.getUserActiveOrder(sessionToken);

        assertFalse(result.isSuccess());
        assertEquals("An unexpected error occurred: Database Exploded!!!!!", result.getError());
    }

}