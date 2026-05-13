package com.group16b.ApplicationLayer;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompanyPolicy.ProductionCompanyPolicy;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Founder;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyPolicyRepositoryMapImpl;

public class AdminManagementServiceTest1 {
    
    private AdminManagementService adminManagementService;
    private IAuthenticationService mockTokenService;
    private ISystemAdminRepository mockSystemAdminRepository;
    private IEventRepository mockEventRepository;
    private IVenueRepository mockVenueRepository;
    private IUserRepository mockUserRepository;
    private IOrderRepository mockOrderRepository;

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
        // 1. Initialize Mocks
        mockSystemAdminRepository = mock(ISystemAdminRepository.class);
        mockTokenService = mock(IAuthenticationService.class);
        mockEventRepository = mock(IEventRepository.class);
        mockVenueRepository = mock(IVenueRepository.class);
        mockUserRepository = mock(IUserRepository.class);
        mockOrderRepository = mock(IOrderRepository.class); // ADDED: Order Repo Mock

        // 2. Initialize Service
        adminManagementService = new AdminManagementService(mockTokenService);

        // 3. Inject ALL Repositories using Reflection
        setPrivateField(adminManagementService, "systemAdminRepo", mockSystemAdminRepository);
        setPrivateField(adminManagementService, "userRepository", mockUserRepository);
        setPrivateField(adminManagementService, "orderRepo", mockOrderRepository);
        setPrivateField(adminManagementService, "eventRepo", mockEventRepository);

        // 4. Setup Tokens
        sessionToken = "validToken";
        invalidToken = "invalidToken";
        when(mockTokenService.validateToken(sessionToken)).thenReturn(true);
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(true);
        when(mockTokenService.validateToken(invalidToken)).thenReturn(false);

        // 5. Setup Original Data
        systemAdmin = new SystemAdmin(1, "username", "password", "email");
        
        user = new User("testuser", "password");
        user.addRole(1, new Founder(user.getUserID()));
        when(mockTokenService.validateToken("user1")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user1")).thenReturn("Signed");
        when(mockTokenService.isUserToken("user1")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user1")).thenReturn(String.valueOf(user.getUserID()));

        user2 = new User("testuser2", "password");
        when(mockTokenService.validateToken("user2")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user2")).thenReturn("Signed");
        when(mockTokenService.isUserToken("user2")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user2")).thenReturn(String.valueOf(user2.getUserID()));

        location1 = new Location("location1", "1", "street", "city", "state", "country", 0.00, 0.00);
        segment1 = new FieldSeg("segment1", 50);

        Map<String, Segment> segmentMap = new TreeMap<>();
        segmentMap.put("segment1", segment1);
        venue1 = new Venue("Test Venue", location1, segmentMap);
        when(mockVenueRepository.getVenueByID("venue1")).thenReturn(venue1);
        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);
        
        e1 = new Event(new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 1, 5.0, 3.5), user.getUserID());
        when(mockEventRepository.getEventByID(e1.getEventID())).thenReturn(e1);
        when(mockEventRepository.searchEvents(List.of("empty"), null, null, null, null, null, null, null, null, null)).thenReturn(new ArrayList<>(List.of()));
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
        
        Order completedOrder = new Order("segment1", 1, 1.0, eventID, String.valueOf(user.getUserID()));
        completedOrder.CompleteOrder();
        
        List<Order> databaseOrders = new ArrayList<>();
        databaseOrders.add(completedOrder);
        
        // THE FIX: Tell Mockito to return the dummy data when getAllCompletedOrders() is called!
        when(mockOrderRepository.getAllCompletedOrders()).thenReturn(databaseOrders);
        
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(sessionToken, user.getUserID());
        
        assertTrue(result.isSuccess(), "Service failed with error: " + result.getError());
        assertEquals(1, result.getValue().size());
        assertEquals(completedOrder.getOrderId(), result.getValue().get(0).getOrderId());
    }

    @Test
    public void testViewAllPurchaseHistoryMultipleOrders() {
        int eventID = e1.getEventID();
        
        Order order1 = new Order("segment1", 1, 1.0, eventID, String.valueOf(user.getUserID()));
        order1.CompleteOrder();
        Order order2 = new Order("segment2", 2, 2.0, eventID, String.valueOf(user.getUserID()));
        order2.CompleteOrder();
        
        when(mockOrderRepository.getAllCompletedOrders()).thenReturn(List.of(order1, order2));
        
        Result<List<OrderDTO>> history = adminManagementService.viewPurchesHistoryByUser(sessionToken, user.getUserID());
        
        assertTrue(history.isSuccess(), "Service failed with error: " + history.getError());
        assertEquals(2, history.getValue().size());
    }

    @Test
    public void testViewAllPurchaseHistoryBad() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(sessionToken, 999);
        
        assertTrue(result.isSuccess(), "Service should succeed even if user has no orders");
        assertTrue(result.getValue().isEmpty(), "Expected an empty history for a user that doesn't exist");
    }

    @Test
    public void testCloseProductionCompanySuccess() throws Exception {
        int companyID = 1;
        
        // 1. Create a mock for the Policy Repository
        ProductionCompanyPolicyRepositoryMapImpl mockPolicyRepo = mock(ProductionCompanyPolicyRepositoryMapImpl.class);
        
        // 2. Inject it using Reflection (Requires the change to AdminManagementService.java mentioned in Step 1!)
        Field policyField = adminManagementService.getClass().getDeclaredField("productionCompanyRepo");
        policyField.setAccessible(true);
        policyField.set(adminManagementService, mockPolicyRepo);

        // 3. Tell the mock to return a dummy company
        ProductionCompanyPolicy mockCompany = mock(ProductionCompanyPolicy.class);
        // Ensure this method name matches exactly what your getProductionCompanyByID method is called
        when(mockPolicyRepo.getProductionCompanyByID(companyID)).thenReturn(mockCompany); 
        
        // 4. Because your service calls searchEvents, we must tell Mockito not to panic when that happens
        when(mockEventRepository.searchEvents(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyList()))
            .thenReturn(new ArrayList<>());
        
        Result<String> result = adminManagementService.closeProductionCompany(companyID, sessionToken);
        
        assertTrue(result.isSuccess(), "Failed to close company: " + result.getError());
    }

    @Test
    public void testViewAllPurchaseHistoryEmptyHistory() {
        User newUser = new User("newuser@example.com", "password123");
        when(mockUserRepository.getUserByID(newUser.getUserID())).thenReturn(newUser);
        when(mockOrderRepository.getOrdersBySubjectID(String.valueOf(newUser.getUserID()))).thenReturn(new ArrayList<>());
        
        Result<List<OrderDTO>> history = adminManagementService.viewPurchesHistoryByUser(sessionToken, newUser.getUserID());
        
        assertNotNull(history);
        assertTrue(history.isSuccess());
        assertTrue(history.getValue().isEmpty());
    }

    @Test
    public void testViewAllPurchaseHistoryInvalidToken() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(invalidToken, user.getUserID());
        assertFalse(result.isSuccess(), "Service should fail with invalid token");
    }

    @Test
    public void testViewAllPurchaseHistoryNullToken() {
        when(mockTokenService.validateToken(null)).thenReturn(false);
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(null, user.getUserID());
        assertFalse(result.isSuccess(), "Service should fail with null token");
    }

    @Test
    public void testViewPurchaseHistoryUnauthorizedUser() {
        User otherUser = new User("other@example.com", "password123");
        when(mockUserRepository.getUserByID(otherUser.getUserID())).thenReturn(otherUser);
        
        // Simulating the user trying to fetch their own history without admin token
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(false);

        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(sessionToken, otherUser.getUserID());
        assertFalse(result.isSuccess(), "Service should fail for unauthorized access");
    }

    @Test
    public void testCloseProductionCompanyUnauthorized() {
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(false);

        Result<String> result = adminManagementService.closeProductionCompany(1, sessionToken);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminSuccess() {
        Result<String> result = adminManagementService.registerNewAdmin(sessionToken, 1, "newAdmin", "password123", "admin@example.com");
        assertTrue(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminInvalidToken() {
        Result<String> result = adminManagementService.registerNewAdmin(invalidToken, 1, "admin", "pass", "email@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminUnauthorized() {
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(false);

        Result<String> result = adminManagementService.registerNewAdmin(sessionToken, 1, "admin", "pass", "email@example.com");
        assertFalse(result.isSuccess());
    }
}