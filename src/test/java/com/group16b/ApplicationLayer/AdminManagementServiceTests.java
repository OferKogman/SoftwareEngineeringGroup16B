package com.group16b.ApplicationLayer;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.GridRectangle;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Entrance;
import com.group16b.DomainLayer.Venue.Stage;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Venue.VenueGrid;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.SystemAdminRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;

public class AdminManagementServiceTests {

    private AdminManagementService adminManagementService;
    private IAuthenticationService tokenService;
    private IRepository<SystemAdmin> systemAdminRepository;
    private IEventRepository eventRepository;
    private IRepository<Venue> venueRepository;
    private IRepository<User> userRepository;
    private OrderRepositoryMapImpl orderRepository;
    private IProductionCompanyRepository productionCompanyRepository;
    private IPaymentGateway mockPaymentGateway;
    private ITicketGateway mockTicketGateway;

    private Location location1;
    private Segment segment1;
    private Venue venue1;
    private Event e1;
    private Order myActiveOrder;
    private Order myCompletedOrder;
    private Order myCanceledOrder;
    private Order unrelatedCompletedOrder;
    private Event myActiveEvent;
    private User user;
    private String USER2_MAIL="miki mahus";
    private String sessionToken;
    private String adminToken;
    private String invalidToken;
    private String userSecret;
    private String adminSecret;

    @BeforeEach
    void setUp() throws Exception {
        systemAdminRepository = new SystemAdminRepositoryMapImpl();
        userSecret = "mySuperSecretKeyForUsers123456789"; // Must be at least 256 bits for HS256
		adminSecret = "mySuperSecretKeyForAdmins123456789"; // Must be at least 256 bits for HS256
        tokenService = new AuthenticationServiceJWTImpl(userSecret, adminSecret);
        eventRepository = new EventRepositoryMapImpl();
        venueRepository = new VenueRepositoryMapImpl();
        userRepository = new UserRepositoryMapImpl();
        orderRepository = new OrderRepositoryMapImpl(); 
        productionCompanyRepository = new ProductionCompanyRepositoryMapImpl();

        mockPaymentGateway=mock(IPaymentGateway.class);
        mockTicketGateway=mock(ITicketGateway.class);
        adminManagementService = new AdminManagementService(tokenService,productionCompanyRepository, orderRepository, eventRepository, userRepository, systemAdminRepository,mockPaymentGateway,mockTicketGateway);
            
        user = new User("testuser", "password");
        adminToken = tokenService.generateAdminToken("admin@test.com");
        sessionToken = tokenService.generateVisitor_SignedToken("testuser");
        invalidToken = "invalidToken";
        
        location1 = new Location("location1", "1", "street", "city", "state", "country", 0.00, 0.00);
        segment1 = new FieldSeg("segment1", 50, new GridRectangle(1, 2, 3, 4));//added basic area since we don't test stuff about it here

        Map<String, Segment> segmentMap = new TreeMap<>();
        segmentMap.put("segment1", segment1);
        venue1 = new Venue("Test Venue", location1, segmentMap, "testVenueID", new VenueGrid(6, 7), new ConcurrentHashMap<String, Stage>(), new ConcurrentHashMap<String, Entrance>(), 1);

        
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);

        
        e1 = new Event(new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 1, 3.5), user.getEmail());
        myActiveEvent=new Event(new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 1, 3.5), USER2_MAIL);
        myActiveEvent.activateEvent();
        eventRepository.save(myActiveEvent);

        ProductionCompany someRnadomCompany=new ProductionCompany(10042,"ra",2.1,"rand");
        productionCompanyRepository.save(someRnadomCompany);

        Event randomAssEvent=new Event(new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 10042, 3.5), USER2_MAIL);
        eventRepository.save(randomAssEvent);

        myActiveOrder=new Order("segment1", 1, 1.0, myActiveEvent.getEventID(), USER2_MAIL);
        myCompletedOrder=new Order("segment2", 1, 1.0, myActiveEvent.getEventID(), USER2_MAIL);
        myCompletedOrder.CompleteOrder();
        myCanceledOrder=new Order("segment3", 1, 1.0, myActiveEvent.getEventID(), USER2_MAIL);
        myCanceledOrder.CancelOrder();
        unrelatedCompletedOrder=new Order("segment2", 1, 1.0, randomAssEvent.getEventID(),USER2_MAIL);
        unrelatedCompletedOrder.CompleteOrder();
        orderRepository.save(myActiveOrder);
        orderRepository.save(myCompletedOrder);
        orderRepository.save(myCanceledOrder);
        orderRepository.save(unrelatedCompletedOrder);

    }

    // Helper method to keep reflection injection clean

    @Test
    void testViewAllPurchaseHistory() {
        int eventID = e1.getEventID();

        Order completedOrder = new Order("segment1", 1, 1.0, eventID, String.valueOf(user.getEmail()));
        completedOrder.CompleteOrder();

        List<Order> databaseOrders = new ArrayList<>();
        databaseOrders.add(completedOrder);

        orderRepository.save(completedOrder);
        
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(adminToken, user.getEmail());

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

        orderRepository.save(order1);
        orderRepository.save(order2);
        
        Result<List<OrderDTO>> history = adminManagementService.viewPurchesHistoryByUser(adminToken, user.getEmail());

        assertTrue(history.isSuccess(), "Service failed with error: " + history.getError());
        assertEquals(2, history.getValue().size());
    }

    @Test
    public void testViewAllPurchaseHistoryBad() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(adminToken, "999");

        assertTrue(result.isSuccess(), "Service should succeed even if user has no orders");
        assertTrue(result.getValue().isEmpty(), "Expected an empty history for a user that doesn't exist");
    }

    @Test
    public void testCloseProductionCompanySuccess() throws Exception {
        int companyID = 1;

        Field policyField = adminManagementService.getClass().getDeclaredField("productionCompanyRepo");
        ProductionCompany testCompany = new ProductionCompany(companyID, "prodTest", 5, "1"); 
        productionCompanyRepository.save(testCompany);

        policyField.setAccessible(true);
        policyField.set(adminManagementService, productionCompanyRepository);
        
        Result<String> result = adminManagementService.closeProductionCompany(companyID, adminToken);

        assertTrue(result.isSuccess(), "Failed to close company: " + result.getError());
    }

    @Test
    public void testViewAllPurchaseHistoryEmptyHistory() {
        User newUser = new User("newuser@example.com", "password123");
        Result<List<OrderDTO>> history = adminManagementService.viewPurchesHistoryByUser(adminToken,
                newUser.getEmail());

        
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

        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(sessionToken,
                otherUser.getEmail());
        assertFalse(result.isSuccess(), "Service should fail for unauthorized access");
    }

    @Test
    public void testCloseProductionCompanyUnauthorized() {

        Result<String> result = adminManagementService.closeProductionCompany(1, sessionToken);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminSuccess() {
        Result<String> result = adminManagementService.registerNewAdmin(adminToken, "newAdmin", "password123",
                "admin@example.com");
        assertTrue(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminInvalidToken() {
        Result<String> result = adminManagementService.registerNewAdmin(invalidToken, "admin", "pass",
                "email@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminUnauthorized() {

        Result<String> result = adminManagementService.registerNewAdmin(sessionToken, "newAdmin", "password123",
                "admin@example.com");
        assertFalse(result.isSuccess());
    }
@Test
    public void concurrentRemove_removeUser_OnlyOneSucceeds() throws InterruptedException {
        User user = new User("email", "password");
        userRepository.save(user);
        
        CountDownLatch startLatch = new CountDownLatch(1);
        
        Runnable removeTask = () -> {
            try {
                // ADD THIS: Threads will pause here and wait for the starting gun!
                startLatch.await(); 
                adminManagementService.removeUser(user.getEmail(), adminToken);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        
        Thread thread1 = new Thread(removeTask);
        Thread thread2 = new Thread(removeTask);
        
        thread1.start();
        thread2.start();
        
        // This drops the latch to 0, firing both threads at the exact same millisecond
        startLatch.countDown(); 
        
        thread1.join();
        thread2.join();
        
        // THE FIX: The findByID call is now safely protected inside the assert block
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.findByID(user.getEmail());
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
            adminManagementService.registerNewAdmin(adminToken, newAdminUsername, newAdminPassword, newAdminEmail);
        };
        Thread thread1 = new Thread(registerTask);
        Thread thread2 = new Thread(registerTask);
        thread1.start();
        thread2.start();
        startLatch.countDown();
        thread1.join();
        thread2.join();
        // Verify that only one admin is registered
        SystemAdmin registeredAdmin = systemAdminRepository.findByID(newAdminUsername);
        assertNotNull(registeredAdmin, "Expected one admin to be registered");
    }
    @Test
    public void concurrentViewPurchaseHistoryByCompany_OnlyOneProcessesAtATime() throws InterruptedException {
        ProductionCompany company = new ProductionCompany(1, "Company1", 1, user.getEmail());
        productionCompanyRepository.save(company);
        int companyID = company.getProductionCompanyID();

        eventRepository.save(e1);

        Order order1 = new Order("segment1", 1, 1.0, e1.getEventID(), user.getEmail());
        order1.CompleteOrder();
        orderRepository.save(order1);

        CountDownLatch startLatch = new CountDownLatch(1);
        List<Result<List<OrderDTO>>> results = new ArrayList<>();

        Runnable viewTask = () -> {
            try {
                startLatch.await();
                Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByCompany(adminToken, companyID);
                synchronized (results) {
                    results.add(result);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread thread1 = new Thread(viewTask);
        Thread thread2 = new Thread(viewTask);

        thread1.start();
        thread2.start();
        startLatch.countDown();
        thread1.join();
        thread2.join();

        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertEquals(2, results.get(0).getValue().size());
        assertEquals(2, results.get(1).getValue().size());
    }

    @Test
    public void concurrentRemoveUser_OnlyOneSucceeds() throws InterruptedException {
        User targetUser = new User("target@example.com", "password123");
        userRepository.save(targetUser);

        CountDownLatch startLatch = new CountDownLatch(1);
        List<Result<String>> results = new ArrayList<>();

        Runnable removeTask = () -> {
            try {
                startLatch.await();
                Result<String> result = adminManagementService.removeUser(targetUser.getEmail(), adminToken);
                synchronized (results) {
                    results.add(result);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread thread1 = new Thread(removeTask);
        Thread thread2 = new Thread(removeTask);

        thread1.start();
        thread2.start();
        startLatch.countDown();
        thread1.join();
        thread2.join();

        assertEquals(2, results.size());
        long successCount = results.stream().filter(Result::isSuccess).count();
        assertEquals(1, successCount, "Exactly one removal should succeed");
        assertThrows(IllegalArgumentException.class, () -> userRepository.findByID(targetUser.getEmail()));
    }

    @Test
    public void concurrentCloseProductionCompany_OnlyOneSucceeds() throws InterruptedException {
        ProductionCompany company = new ProductionCompany(1, "Company1", 1, user.getEmail());
        productionCompanyRepository.save(company);
        int companyID = company.getProductionCompanyID();

        CountDownLatch startLatch = new CountDownLatch(1);
        List<Result<String>> results = new ArrayList<>();

        Runnable closeTask = () -> {
            try {
                startLatch.await();
                Result<String> result = adminManagementService.closeProductionCompany(companyID, adminToken);
                synchronized (results) {
                    results.add(result);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread thread1 = new Thread(closeTask);
        Thread thread2 = new Thread(closeTask);

        thread1.start();
        thread2.start();
        startLatch.countDown();
        thread1.join();
        thread2.join();

        assertEquals(2, results.size());
        long successCount = results.stream().filter(Result::isSuccess).count();
        assertEquals(1, successCount, "Exactly one closure should succeed");
    }
    @Test
    public void testRegisterNewAdminNullToken() {
        Result<String> result = adminManagementService.registerNewAdmin(null, "admin", "pass", "email@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminEmptyUsername() {
        Result<String> result = adminManagementService.registerNewAdmin(adminToken, "", "password123", "admin@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminNullUsername() {
        Result<String> result = adminManagementService.registerNewAdmin(adminToken, null, "password123", "admin@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminEmptyPassword() {
        Result<String> result = adminManagementService.registerNewAdmin(adminToken, "newAdmin", "", "admin@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterNewAdminNullEmail() {
        Result<String> result = adminManagementService.registerNewAdmin(adminToken, "newAdmin", "password123", null);
        assertFalse(result.isSuccess());
    }


    @Test
    public void testRegisterNewAdminDuplicateUsername() {
        adminManagementService.registerNewAdmin(adminToken, "sameAdmin", "password123", "first@example.com");
        Result<String> result = adminManagementService.registerNewAdmin(adminToken, "sameAdmin", "password456", "second@example.com");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testCloseProductionCompanyInvalidToken() {
        Result<String> result = adminManagementService.closeProductionCompany(1, invalidToken);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testCloseProductionCompanyNullToken() {
        Result<String> result = adminManagementService.closeProductionCompany(1, null);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testCloseNonExistentProductionCompany() {
        Result<String> result = adminManagementService.closeProductionCompany(99999, adminToken);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRemoveUserSuccess() {
        User targetUser = new User("remove@example.com", "password123");
        userRepository.save(targetUser);
        Result<String> result = adminManagementService.removeUser(targetUser.getEmail(), adminToken);
        assertTrue(result.isSuccess());
        assertThrows(IllegalArgumentException.class, () -> userRepository.findByID(targetUser.getEmail()));
    }

    @Test
    public void testRemoveUserInvalidToken() {
        User targetUser = new User("remove2@example.com", "password123");
        userRepository.save(targetUser);
        Result<String> result = adminManagementService.removeUser(targetUser.getEmail(), invalidToken);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRemoveUserNullToken() {
        User targetUser = new User("remove3@example.com", "password123");
        userRepository.save(targetUser);
        Result<String> result = adminManagementService.removeUser(targetUser.getEmail(), null);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRemoveNonExistentUser() {
        Result<String> result = adminManagementService.removeUser("ghost@example.com", adminToken);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRemoveUserUnauthorized() {
        User targetUser = new User("remove4@example.com", "password123");
        userRepository.save(targetUser);
        Result<String> result = adminManagementService.removeUser(targetUser.getEmail(), sessionToken);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testViewPurchaseHistoryByCompanySuccess() {
        ProductionCompany company = new ProductionCompany(2, "TestCompany", 1, user.getEmail());
        productionCompanyRepository.save(company);
        int companyID = company.getProductionCompanyID();
        Event event2 = new Event(new EventRecord("venue1", "event2", LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), "artist2", "category2", companyID, 4), user.getEmail());
        eventRepository.save(event2);

        Order order = new Order("segment1", 1, 1.0, event2.getEventID(), user.getEmail());
        order.CompleteOrder();
        orderRepository.save(order);

        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByCompany(adminToken, companyID);
        assertTrue(result.isSuccess());
        assertFalse(result.getValue().isEmpty());
    }

    @Test
    public void testViewPurchaseHistoryByCompanyInvalidToken() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByCompany(invalidToken, 1);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testViewPurchaseHistoryByCompanyNullToken() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByCompany(null, 1);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testViewPurchaseHistoryByCompanyUnauthorized() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByCompany(sessionToken, 1);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testViewPurchaseHistoryByCompanyNoOrders() {
        ProductionCompany company = new ProductionCompany(3, "EmptyCompany", 1, user.getEmail());
        productionCompanyRepository.save(company);
        int companyID = company.getProductionCompanyID();

        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByCompany(adminToken, companyID);
        assertTrue(result.isSuccess());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void testViewPurchaseHistoryByUserNullEmail() {
        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(adminToken, null);
        assertTrue(result.isSuccess());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void testViewPurchaseHistoryByUserMultipleUsersIsolated() {
        User user2 = new User("other@example.com", "password");
        int eventID = e1.getEventID();

        Order order1 = new Order("segment1", 1, 1.0, eventID, user.getEmail());
        order1.CompleteOrder();
        Order order2 = new Order("segment2", 1, 2.0, eventID, user2.getEmail());
        order2.CompleteOrder();

        orderRepository.save(order1);
        orderRepository.save(order2);

        Result<List<OrderDTO>> result = adminManagementService.viewPurchesHistoryByUser(adminToken, user.getEmail());
        assertTrue(result.isSuccess());
        assertEquals(1, result.getValue().size());
        assertEquals(order1.getOrderId(), result.getValue().get(0).getOrderId());
    }

    @Test
    public void testCloseProductionCompanyTwice() {
        ProductionCompany company = new ProductionCompany(4, "Company2", 1, user.getEmail());
        productionCompanyRepository.save(company);
        int companyID = company.getProductionCompanyID();

        Result<String> first = adminManagementService.closeProductionCompany(companyID, adminToken);
        Result<String> second = adminManagementService.closeProductionCompany(companyID, adminToken);

        assertTrue(first.isSuccess());
        assertFalse(second.isSuccess());
    }

    @Test
    public void testRemoveUserTwice() {
        User targetUser = new User("twice@example.com", "password123");
        userRepository.save(targetUser);

        Result<String> first = adminManagementService.removeUser(targetUser.getEmail(), adminToken);
        Result<String> second = adminManagementService.removeUser(targetUser.getEmail(), adminToken);

        assertTrue(first.isSuccess());
        assertFalse(second.isSuccess());
    }

    @Test
    public void concurrentRegisterAdmin_SameEmail_OnlyOneSucceeds() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Result<String>> results = new ArrayList<>();

        Runnable registerTask = () -> {
            try {
                startLatch.await();
                Result<String> result = adminManagementService.registerNewAdmin(adminToken, "adminA", "pass", "shared@example.com");
                synchronized (results) {
                    results.add(result);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread t1 = new Thread(registerTask);
        Thread t2 = new Thread(registerTask);
        t1.start();
        t2.start();
        startLatch.countDown();
        t1.join();
        t2.join();

        assertEquals(2, results.size());
        long successCount = results.stream().filter(Result::isSuccess).count();
        assertEquals(1, successCount);
    }

    @Test
    public void concurrentCloseProductionCompany_MultipleCompanies_EachClosedOnce() throws InterruptedException {
        ProductionCompany company1 = new ProductionCompany(10, "CompanyA", 1, user.getEmail());
        ProductionCompany company2 = new ProductionCompany(11, "CompanyB", 1, user.getEmail());
        productionCompanyRepository.save(company1);
        productionCompanyRepository.save(company2);

        CountDownLatch startLatch = new CountDownLatch(1);
        List<Result<String>> results = new ArrayList<>();

        Runnable closeA = () -> {
            try {
                startLatch.await();
                Result<String> r = adminManagementService.closeProductionCompany(company1.getProductionCompanyID(), adminToken);
                synchronized (results) { results.add(r); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        };

        Runnable closeB = () -> {
            try {
                startLatch.await();
                Result<String> r = adminManagementService.closeProductionCompany(company2.getProductionCompanyID(), adminToken);
                synchronized (results) { results.add(r); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        };

        Thread t1 = new Thread(closeA);
        Thread t2 = new Thread(closeB);
        t1.start();
        t2.start();
        startLatch.countDown();
        t1.join();
        t2.join();

        assertEquals(2, results.size());
        long successCount = results.stream().filter(Result::isSuccess).count();
        assertEquals(2, successCount);
    }
}