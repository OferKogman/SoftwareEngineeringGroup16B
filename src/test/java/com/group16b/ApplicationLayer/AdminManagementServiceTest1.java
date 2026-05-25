package com.group16b.ApplicationLayer;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.SystemAdminRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class AdminManagementServiceTest1 {
    
    private AdminManagementService adminManagementService;
    private IAuthenticationService tokenService;
    private ISystemAdminRepository systemAdminRepository;
    private IEventRepository eventRepository;
    private IVenueRepository venueRepository;
    private IRepository<User> userRepository;
    private OrderRepositoryMapImpl orderRepository;
    private IProductionCompanyRepository productionCompanyRepository;

    private Location location1;
    private Segment segment1;
    private Venue venue1;
    private Event e1;
    private SystemAdmin systemAdmin;
    private User user;
    private User user2;
    private String sessionToken;
    private String invalidToken;

    @BeforeEach
    void setUp() throws Exception {
        systemAdminRepository = new SystemAdminRepositoryMapImpl();
        tokenService = new AuthenticationServiceJWTImpl(sessionToken, sessionToken);
        eventRepository = new EventRepositoryMapImpl();
        venueRepository = mock(IVenueRepository.class);
        userRepository = new UserRepositoryMapImpl();
        orderRepository = new OrderRepositoryMapImpl(); 
        productionCompanyRepository = new ProductionCompanyRepositoryMapImpl();

        adminManagementService = new AdminManagementService(tokenService,productionCompanyRepository, orderRepository, eventRepository, userRepository, systemAdminRepository);

        setPrivateField(adminManagementService, "systemAdminRepo", systemAdminRepository);
        setPrivateField(adminManagementService, "userRepository", userRepository);
        setPrivateField(adminManagementService, "orderRepo", orderRepository);
        setPrivateField(adminManagementService, "eventRepo", eventRepository);

        sessionToken = "validToken";
        invalidToken = "invalidToken";
        
        systemAdmin = new SystemAdmin("1", "username", "password", "email");
        
        user = new User("testuser", "password");
        
        user2 = new User("testuser2", "password");
        
        location1 = new Location("location1", "1", "street", "city", "state", "country", 0.00, 0.00);
        segment1 = new FieldSeg("segment1", 50);

        Map<String, Segment> segmentMap = new TreeMap<>();
        segmentMap.put("segment1", segment1);
        venue1 = new Venue("Test Venue", location1, segmentMap, "testVenueID");
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);
        
        e1 = new Event(new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 1, 5.0, 3.5), user.getEmail());
    }

    // Helper method to keep reflection injection clean
    private void setPrivateField(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testViewAllPurchaseHistory() {
        int eventID = e1.getEventID();
        
        Order completedOrder = new Order("segment1", 1, 1.0, eventID, String.valueOf(user.getEmail()));
        completedOrder.CompleteOrder();
        
        List<Order> databaseOrders = new ArrayList<>();
        databaseOrders.add(completedOrder);
        
        
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(sessionToken, user.getEmail());
        
        assertTrue(result.isSuccess(), "Service failed with error: " + result.getError());
        assertEquals(1, result.getValue().size());
        assertEquals(completedOrder.getOrderId(), result.getValue().get(0).getOrderId());
    }

    @Test
    public void testViewAllPurchaseHistoryMultipleOrders() {
        int eventID = e1.getEventID();
        
        Order order1 = new Order("segment1", 1, 1.0, eventID, String.valueOf(user.getEmail()));
        order1.CompleteOrder();
        Order order2 = new Order("segment2", 2, 2.0, eventID, String.valueOf(user.getEmail()));
        order2.CompleteOrder();
        
        
        Result<List<OrderDTO>> history = adminManagementService.viewPurchesHistoryByUser(sessionToken, user.getEmail());
        
        assertTrue(history.isSuccess(), "Service failed with error: " + history.getError());
        assertEquals(2, history.getValue().size());
    }

    @Test
    public void testViewAllPurchaseHistoryBad() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(sessionToken, "999");
        
        assertTrue(result.isSuccess(), "Service should succeed even if user has no orders");
        assertTrue(result.getValue().isEmpty(), "Expected an empty history for a user that doesn't exist");
    }

    @Test
    public void testCloseProductionCompanySuccess() throws Exception {
        int companyID = 1;
        
        
        Field policyField = adminManagementService.getClass().getDeclaredField("productionCompanyRepo");
        policyField.setAccessible(true);
        policyField.set(adminManagementService, productionCompanyRepository);

        ProductionCompany mockCompany = mock(ProductionCompany.class);
        
        
        Result<String> result = adminManagementService.closeProductionCompany(companyID, sessionToken);
        
        assertTrue(result.isSuccess(), "Failed to close company: " + result.getError());
    }

    @Test
    public void testViewAllPurchaseHistoryEmptyHistory() {
        User newUser = new User("newuser@example.com", "password123");
        Result<List<OrderDTO>> history = adminManagementService.viewPurchesHistoryByUser(sessionToken, newUser.getEmail());
        
        assertNotNull(history);
        assertTrue(history.isSuccess());
        assertTrue(history.getValue().isEmpty());
    }

    @Test
    public void testViewAllPurchaseHistoryInvalidToken() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(invalidToken, user.getEmail());
        assertFalse(result.isSuccess(), "Service should fail with invalid token");
    }

    @Test
    public void testViewAllPurchaseHistoryNullToken() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(null, user.getEmail());
        assertFalse(result.isSuccess(), "Service should fail with null token");
    }

    @Test
    public void testViewPurchaseHistoryUnauthorizedUser() {
        User otherUser = new User("other@example.com", "password123");
        
        // Simulating the user trying to fetch their own history without admin token

        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(sessionToken, otherUser.getEmail());
        assertFalse(result.isSuccess(), "Service should fail for unauthorized access");
    }

    @Test
    public void testCloseProductionCompanyUnauthorized() {

        Result<String> result = adminManagementService.closeProductionCompany(1, sessionToken);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminSuccess() {
        Result<String> result = adminManagementService.registerNewAdmin(sessionToken, "1", "newAdmin", "password123", "admin@example.com");
        assertTrue(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminInvalidToken() {
        Result<String> result = adminManagementService.registerNewAdmin(invalidToken, "1", "admin", "pass", "email@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminUnauthorized() {

        Result<String> result = adminManagementService.registerNewAdmin(sessionToken, "1", "admin", "pass", "email@example.com");
        assertFalse(result.isSuccess());
    }
    @Test
    public void concurrentRemove_removeUser_OnlyOneSucceeds() throws InterruptedException {
        int companyID = 1;
        
        CountDownLatch startLatch = new CountDownLatch(1);
        ProductionCompany company = new ProductionCompany(companyID, "Test Company", 1, user.getEmail());
        productionCompanyRepository.save(company);

        Runnable removeTask = () -> {
            adminManagementService.closeProductionCompany(companyID, sessionToken);
        };
        Thread thread1 = new Thread(removeTask);
        Thread thread2 = new Thread(removeTask);


        thread1.start();
        thread2.start();
        startLatch.countDown();
        thread1.join();
        thread2.join();

        // Verify that the company is removed
        productionCompanyRepository.findByID(String.valueOf(companyID));
            assertThrows(IllegalArgumentException.class, () -> {
                productionCompanyRepository.findByID(String.valueOf(companyID));
            });
    }
    @Test
    public void concurrentRegister_registerAdmin_OnlyOneSucceeds() throws InterruptedException {
        String newAdminID = "1";
        String newAdminUsername = "newAdmin";
        String newAdminPassword = "password123";
        String newAdminEmail = "newadmin@example.com";
        CountDownLatch startLatch = new CountDownLatch(1);
        Runnable registerTask = () -> {
            adminManagementService.registerNewAdmin(sessionToken, newAdminID, newAdminUsername, newAdminPassword, newAdminEmail);
        };
        Thread thread1 = new Thread(registerTask);
        Thread thread2 = new Thread(registerTask);
        thread1.start();
        thread2.start();
        startLatch.countDown();
        thread1.join();
        thread2.join();
        // Verify that only one admin is registered
        SystemAdmin registeredAdmin = systemAdminRepository.findByID(newAdminID);
        assertNotNull(registeredAdmin, "Expected one admin to be registered");
    }
}