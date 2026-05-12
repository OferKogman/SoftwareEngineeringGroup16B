package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.group16b.ApplicationLayer.Objects.Result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.ProductionCompanyPolicy.OpenCompany;
import com.group16b.DomainLayer.ProductionCompanyPolicy.ProductionCompanyPolicy;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.Roles.Founder;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Order.OrderState;

public class adminManagementServiceTest {
    static private AdminManagementService adminManagementService;
    static private IAuthenticationService mockTokenService;
    static private ISystemAdminRepository mockSystemAdminRepository;
    static private IEventRepository mockEventRepository;
    static private IVenueRepository mockVenueRepository;
    static private OrderService orderService;
    static private IUserRepository userRepository;
    Location location1;
    Segment segment1;
    Venue venue1;
    Event e1;
    SystemAdmin systemAdmin;
    User user;
    User user2;
    String sessionToken;
    String invalidToken;


     
    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        mockSystemAdminRepository = mock(ISystemAdminRepository.class);
        mockTokenService = mock(IAuthenticationService.class);
        mockEventRepository = mock(IEventRepository.class);
        mockVenueRepository = mock(IVenueRepository.class);
        systemAdmin = new SystemAdmin(1, "username", "password", "email");
        userRepository = mock(IUserRepository.class);
        invalidToken = "invalidToken";

        adminManagementService = new AdminManagementService(mockTokenService);
        Field adminRepo = adminManagementService.getClass().getDeclaredField("systemAdminRepository");
        adminRepo.setAccessible(true);
        adminRepo.set(adminManagementService, mockSystemAdminRepository);
        sessionToken = "validToken";
        orderService = new OrderService(mockTokenService);


        user = new User("testuser", "password");
        when(mockTokenService.validateToken("user1")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user1")).thenReturn("Signed");
        when(mockTokenService.isUserToken("user1")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user1")).thenReturn(String.valueOf(user.getUserID()));
        user.addRole(1, new Founder(user.getUserID()));

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

    //good
    @Test
    void testViewAllPurchaseHistory() {
        ReserveService reserveService = new ReserveService(mockTokenService);
        int eventID = e1.getEventID();
        String venueID = e1.getEventVenueID();
        String eventSegment = "segment1";
        reserveService.reserveFieldSeats(eventSegment, 1, eventID, venueID, sessionToken);
        Order completedOrder = new Order(eventSegment, 1, 1, eventID, null);
        completedOrder.CompleteOrder();
        List<OrderDTO> completedOrders = new ArrayList<>();
        OrderDTO orderDTO = new OrderDTO(completedOrder);
        completedOrders.add(orderDTO);
        Result.makeOk(completedOrders);
        assertTrue(adminManagementService.viewPurchesHistoryByUser(sessionToken, user.getUserID()).equals(completedOrders));
        
        
    }
    //bad
    @Test
    public void testViewAllPurchaseHistoryBad() {
        user = new User("test@example.com", "password123");
        userRepository.addUser(user);
        
        assertThrows(IllegalArgumentException.class, () -> {
            adminManagementService.viewPurchesHistoryByUser(sessionToken, 999); // Non-existent user ID
        });
    }

    @Test
    public void testViewAllPurchaseHistoryEmptyHistory() {
        User newUser = new User("newuser@example.com", "password123");
        userRepository.addUser(newUser);
        
        Result<List<OrderDTO>> history = adminManagementService.viewPurchesHistoryByUser(sessionToken, newUser.getUserID());
        
        assertNotNull(history);
        assertTrue(history.getValue().isEmpty());
    }

    @Test
    public void testViewAllPurchaseHistoryMultipleOrders() {
        ReserveService reserveService = new ReserveService(mockTokenService);
        int eventID = e1.getEventID();
        String venueID = e1.getEventVenueID();
        
        // Create multiple completed orders
        Order order1 = new Order("segment1", 1, 1, eventID, null);
        order1.CompleteOrder();
        Order order2 = new Order("segment2", 2, 2, eventID, null);
        order2.CompleteOrder();
        
        Result<List<OrderDTO>> history = adminManagementService.viewPurchesHistoryByUser(sessionToken, user.getUserID());
        
        assertEquals(2, history.getValue().size());
    }

    @Test
    public void testViewAllPurchaseHistoryInvalidToken() {
        assertThrows(IllegalArgumentException.class, () -> {
            adminManagementService.viewPurchesHistoryByUser("invalidToken", user.getUserID());
        });
    }

    @Test
    public void testViewAllPurchaseHistoryNullToken() {
        assertThrows(IllegalArgumentException.class, () -> {
            adminManagementService.viewPurchesHistoryByUser(null, user.getUserID());
        });
    }

    @Test
    public void testViewPurchaseHistoryUnauthorizedUser() {
        User otherUser = new User("other@example.com", "password123");
        userRepository.addUser(otherUser);
        
        assertThrows(IllegalArgumentException.class, () -> {
            adminManagementService.viewPurchesHistoryByUser(sessionToken, otherUser.getUserID());
        });
    }

     @Test
    public void testCloseProductionCompanySuccess() {
        int companyID = 1;
        ProductionCompanyPolicy company = mock(ProductionCompanyPolicy.class);
        List<User> companyUsers = new ArrayList<>();
        List<Event> companyEvents = new ArrayList<>();

        when(mockTokenService.validateToken(sessionToken)).thenReturn(true);
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(true);
        when(company.getAssociatedUsers()).thenReturn(companyUsers);

        Result<String> result = adminManagementService.closeProductionCompany(companyID, sessionToken);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testCloseProductionCompanyInvalidToken() {
        when(mockTokenService.validateToken(invalidToken)).thenReturn(false);

        Result<String> result = adminManagementService.closeProductionCompany(1, invalidToken);

        assertFalse(result.isSuccess());
    }

    @Test
    public void testCloseProductionCompanyUnauthorized() {
        when(mockTokenService.validateToken(sessionToken)).thenReturn(true);
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(false);

        Result<String> result = adminManagementService.closeProductionCompany(1, sessionToken);

        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminSuccess() {
        int newAdminID = 1;
        String username = "newAdmin";
        String password = "password123";
        String email = "admin@example.com";

        when(mockTokenService.validateToken(sessionToken)).thenReturn(true);
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(true);

        Result<String> result = adminManagementService.registerNewAdmin(sessionToken, newAdminID, username, password, email);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminInvalidToken() {
        when(mockTokenService.validateToken(invalidToken)).thenReturn(false);

        Result<String> result = adminManagementService.registerNewAdmin(invalidToken, 1, "admin", "pass", "email@example.com");

        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminUnauthorized() {
        when(mockTokenService.validateToken(sessionToken)).thenReturn(true);
        when(mockTokenService.isAdminToken(sessionToken)).thenReturn(false);

        Result<String> result = adminManagementService.registerNewAdmin(sessionToken, 1, "admin", "pass", "email@example.com");

        assertFalse(result.isSuccess());
    }
}
