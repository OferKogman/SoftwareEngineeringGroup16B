package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.ProductionCompanyDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.IdGenerators.ProductionCompanyIdGen;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class ProductionCompanyServiceTests {

    private ProductionCompanyService service;

    private IAuthenticationService authService;

    private IRepository<Order> orderRepo;
    private IEventRepository eventRepo;
    private IRepository<User> userRepo;
    private IProductionCompanyRepository companyRepo;

    private ProductionCompany company1;
    private ProductionCompany company2;
    private ProductionCompany companyWithNoEvents;
    private ProductionCompany companyWithNoOrders;
    private User managerForHistory;
    private User managerForRevenue;
    private User managerWithNoUsefullPerms;
    private User founder;
    private User non_manager;
    private User owner;

    private ProductionCompanyIdGen idGen;

    private final String VALID_FOUNDER_TOKEN = "valid-foudner-token";
    private final String VALID_HISTORY_MANAGER_TOKEN = "valid-history-manager-token";
    private final String VALID_REVENUE_MANAGER_TOKEN = "valid-revenue-manager-token";
    private final String VALID_NO_USEFUL_PERMS_TOKEN = "valid-no-useful-perms-token";
    private final String VALID_NON_MANAGER_TOKEN = "valid-non-manager-token";
    private final String VALID_OWNER_TOKEN = "valid-owner-token";
    private final String INVALID_TOKEN = "invalid-token";
    private final String STALE_USER_TOKEN = "stale-user-token";
    private final String MANAGER_FOR_HISTORY_ID = "1";
    private final String MANAGER_FOR_REVENUE_ID = "2";
    private final String MANAGER_WITH_NO_USEFUL_PERMS_ID = "3";
    private final String NON_MANAGER_ID = "4";
    private final String OWNER_ID = "5";
    private final int COMPANY1_ID = 100;
    private final int COMPANY2_ID = 101;
    private final int COMPANY_WITH_NO_EVENTS_ID = 102;
    private final int COMPANY_WITH_NO_ORDERS_ID = 103; 
    private final int BAD_COMPANY_ID = 999;
    private final String BAD_USER_ID = "999";
    private final String FOUNDER_EMAIL="founder@example.com";
    private final String COMPANY1_NAME="Company1";
    private final String COMPANY2_NAME="Company2";
    private final String BAD_COMPANY_NAME="new company";
    

    private Event company1Event1;
    private Event company1Event2;
    private Event company2Event1;
    private Event companyWithNoOrdersEvent1;

    private Order company1Order1;
    private Order company1Order2;
    private Order company1Order3;
    private Order company2Order1;
    private Order company1ActiveOrder1;


    
    
    
    @BeforeEach
    void setUp() throws Exception {
        authService = mock(IAuthenticationService.class);
        when(authService.validateToken(VALID_FOUNDER_TOKEN)).thenReturn(true);
        when(authService.isUserToken(VALID_FOUNDER_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(VALID_FOUNDER_TOKEN)).thenReturn(FOUNDER_EMAIL);

        when(authService.validateToken(VALID_HISTORY_MANAGER_TOKEN)).thenReturn(true);
        when(authService.isUserToken(VALID_HISTORY_MANAGER_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(VALID_HISTORY_MANAGER_TOKEN)).thenReturn(MANAGER_FOR_HISTORY_ID);

        when(authService.validateToken(VALID_NO_USEFUL_PERMS_TOKEN)).thenReturn(true);
        when(authService.isUserToken(VALID_NO_USEFUL_PERMS_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(VALID_NO_USEFUL_PERMS_TOKEN)).thenReturn(MANAGER_WITH_NO_USEFUL_PERMS_ID);

        when(authService.validateToken(VALID_REVENUE_MANAGER_TOKEN)).thenReturn(true);
        when(authService.isUserToken(VALID_REVENUE_MANAGER_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(VALID_REVENUE_MANAGER_TOKEN)).thenReturn(MANAGER_FOR_REVENUE_ID);

        when(authService.validateToken(VALID_NON_MANAGER_TOKEN)).thenReturn(true);
        when(authService.isUserToken(VALID_NON_MANAGER_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(VALID_NON_MANAGER_TOKEN)).thenReturn(NON_MANAGER_ID);

        when(authService.validateToken(VALID_OWNER_TOKEN)).thenReturn(true);
        when(authService.isUserToken(VALID_OWNER_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(VALID_OWNER_TOKEN)).thenReturn(OWNER_ID);

        when(authService.validateToken(STALE_USER_TOKEN)).thenReturn(true);
        when(authService.isUserToken(STALE_USER_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(STALE_USER_TOKEN)).thenReturn(BAD_USER_ID);

        when(authService.validateToken(INVALID_TOKEN)).thenReturn(false);

        orderRepo = new OrderRepositoryMapImpl();
        eventRepo = new EventRepositoryMapImpl();
        userRepo = new UserRepositoryMapImpl();
        companyRepo = new ProductionCompanyRepositoryMapImpl();

        idGen = new ProductionCompanyIdGen();
        idGen=Mockito.spy(idGen);

        service = new ProductionCompanyService(
            authService,
            orderRepo,
            eventRepo,
            userRepo,
            companyRepo,
            idGen
        );

        seedBaseData();
    }

    private void seedBaseData() throws Exception {
        founder = createUser(FOUNDER_EMAIL);
        managerForHistory = createUser(MANAGER_FOR_HISTORY_ID);
        managerForRevenue = createUser(MANAGER_FOR_REVENUE_ID);
        managerWithNoUsefullPerms = createUser(MANAGER_WITH_NO_USEFUL_PERMS_ID);
        non_manager = createUser(NON_MANAGER_ID);
        owner = createUser(OWNER_ID);

        userRepo.save(founder);
        userRepo.save(managerForHistory);
        userRepo.save(managerForRevenue);
        userRepo.save(managerWithNoUsefullPerms);
        userRepo.save(non_manager);
        userRepo.save(owner);

        company1 = createCompany(COMPANY1_ID, COMPANY1_NAME, FOUNDER_EMAIL);
        company2 = createCompany(COMPANY2_ID, COMPANY2_NAME, FOUNDER_EMAIL);
        companyWithNoEvents = createCompany(COMPANY_WITH_NO_EVENTS_ID, "NoEventsCo", FOUNDER_EMAIL);
        companyWithNoOrders = createCompany(COMPANY_WITH_NO_ORDERS_ID, "NoOrdersCo", FOUNDER_EMAIL);

        assignManagerToCompany(company1, MANAGER_FOR_HISTORY_ID, FOUNDER_EMAIL, Set.of(ManagerPermissions.VIEW_PURCHASE_HISTORY));
        assignOwnerToCompany(company1, OWNER_ID, FOUNDER_EMAIL);
        assignManagerToCompany(company1, MANAGER_FOR_REVENUE_ID, OWNER_ID, Set.of(ManagerPermissions.SALES_REPORT));
        assignManagerToCompany(company1, MANAGER_WITH_NO_USEFUL_PERMS_ID, OWNER_ID, Set.of(ManagerPermissions.CUSTOMER_SUPPORT));

        companyRepo.save(company1);
        companyRepo.save(company2);
        companyRepo.save(companyWithNoEvents);
        companyRepo.save(companyWithNoOrders);

        company1Event1 = createEvent(1, COMPANY1_ID, FOUNDER_EMAIL);
        company1Event2 = createEvent(2, COMPANY1_ID, OWNER_ID);
        company2Event1 = createEvent(3, COMPANY2_ID, OWNER_ID);
        companyWithNoOrdersEvent1 = createEvent(4, COMPANY_WITH_NO_ORDERS_ID, FOUNDER_EMAIL);

        eventRepo.save(company1Event1);
        eventRepo.save(company1Event2);
        eventRepo.save(company2Event1);
        eventRepo.save(companyWithNoOrdersEvent1);

        company1Order1 = createCompletedOrder(company1Event1.getEventID(), 100, NON_MANAGER_ID);
        company1Order2 = createCompletedOrder(company1Event2.getEventID(), 200, NON_MANAGER_ID);
        company1Order3 = createCompletedOrder(company1Event2.getEventID(), 150, NON_MANAGER_ID);
        company2Order1 = createCompletedOrder(company2Event1.getEventID(), 999, NON_MANAGER_ID);

        company1ActiveOrder1 = createOrder(company1Event1.getEventID(), 50, NON_MANAGER_ID);

        orderRepo.save(company1Order1);
        orderRepo.save(company1Order2);
        orderRepo.save(company1Order3);
        orderRepo.save(company2Order1);
        orderRepo.save(company1ActiveOrder1);
    }

    private User createUser(String userId) {

        try {
            return new User(
                userId, "password");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ProductionCompany createCompany(int companyId,String name,String founderEmail) {
        return new ProductionCompany(companyId,name,1.1,founderEmail);
    }

    private Event createEvent(int eventId, int companyId,String ownerId) {
        return new Event(
            new EventRecord(
                "venue1",
                "Event " + eventId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                "Artist",
                "Category",
                companyId,
                100,
                4.5
            ),ownerId
            );
    }

    private Order createOrder(int eventId, double price, String subjectID) {
        Order order=new Order(
            "seg",
            List.of("A1", "A2"),
            price,
            eventId,
            subjectID
        );
        return order;
    }
    private Order createCompletedOrder(int eventId, double price, String subjectID) {
        Order order = createOrder(eventId, price, subjectID);
        order.CompleteOrder();
        return order;
    }

    private void assignManagerToCompany(ProductionCompany company, String managerId, String founderID,Set<ManagerPermissions> perms) {
        company.AssignManager(founderID, managerId, perms);
        company.acceptInvite(managerId, founderID);
    }

    private void assignOwnerToCompany(ProductionCompany company, String newOwnerId, String founderID) {
        company.AssignOwner(founderID, newOwnerId);
        company.acceptInvite(newOwnerId, founderID);
    }


    @Test
    void viewSalesHistory_Founder_ReturnsAllCompanyOrders() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_FOUNDER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        assertCompany1SalesHistory(result.getValue());
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_HistoryManager_ReturnsAllCompanyOrders() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_HISTORY_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        assertCompany1SalesHistory(result.getValue());
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_Owner_ReturnsAllCompanyOrders() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_OWNER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        assertCompany1SalesHistory(result.getValue());
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_NoCompletedOrders_ReturnsEmptyList() {
        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_FOUNDER_TOKEN,
                COMPANY_WITH_NO_ORDERS_ID
            );

        assertTrue(result.isSuccess());
        assertTrue(result.getValue().isEmpty());
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_CompanyWithoutEvents_ReturnsEmptyList() {
        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_FOUNDER_TOKEN,
                COMPANY_WITH_NO_EVENTS_ID
            );

        assertTrue(result.isSuccess());
        assertTrue(result.getValue().isEmpty());
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_RevenueManagerWithoutHistoryPermission_ReturnsFailure() {
        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_REVENUE_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("dont have correct permissions"));
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_NonManager_ReturnsFailure() {
        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_NON_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("dont have correct permissions"));
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_InvalidToken_ReturnsFailure() {
        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                INVALID_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());

        assertEquals(
            "Invalid Token",
            result.getError()
        );
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_StaleUserToken_ReturnsFailure() {
        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                STALE_USER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());

        assertEquals(
            "User with ID " + BAD_USER_ID + " not found.",
            result.getError()
        );
        assertRepositoriesUnchanged();
    }

    @Test
    void viewSalesHistory_BadCompanyID_ReturnsFailure() {
        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_FOUNDER_TOKEN,
                BAD_COMPANY_ID
            );

        assertFalse(result.isSuccess());
        assertEquals(
            "Production company with ID " + BAD_COMPANY_ID + " is not found.",
            result.getError()
        );
        assertRepositoriesUnchanged();
    }

    private void assertCompany1SalesHistory(List<OrderDTO> orders) {
        assertEquals(3, orders.size());

        double total =orders.stream().mapToDouble(OrderDTO::getTocalOrderPrice).sum();

        assertEquals(450, total);

        assertFalse(
            orders.stream()
                .anyMatch(order ->
                    order.getTocalOrderPrice() == 999
                )
        );

        assertFalse(
            orders.stream()
                .anyMatch(order ->
                    order.getTocalOrderPrice() == 50
                )
        );

        Set<Integer> returnedEventIds =
            orders.stream()
                .map(OrderDTO::getEventId)
                .collect(Collectors.toSet());

        assertEquals(
            Set.of(
                company1Event1.getEventID(),
                company1Event2.getEventID()
            ),
            returnedEventIds
        );
    }
    private void assertRepositoriesUnchanged() {
        assertEquals(5, orderRepo.getAll().size());
        assertEquals(4, eventRepo.getAll().size());
        assertEquals(4, companyRepo.getAll().size());
    }

    // =======================================================
    // displayTotalRevenue TESTS
    // =======================================================

    @Test
    void displayTotalRevenue_Founder_ReturnsAllCompanyRevenue() {
        Result<Double> result =
            service.displayTotalRevenue(
                VALID_FOUNDER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        // company1:
        // order1 = 100 (event1)
        // order2 = 200 (event2)
        // order3 = 150 (event2)
        // active order excluded
        // total = 450
        assertEquals(450.0, result.getValue());

        assertRepositoriesUnchanged();
    }
    @Test
    void displayTotalRevenue_Owner_ReturnsAllCompanyRevenue() {
        Result<Double> result =
            service.displayTotalRevenue(
                VALID_OWNER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        // company1:
        // order2 = 200 (event2)
        // order3 = 150 (event2)
        // active order excluded
        // total = 350
        assertEquals(350.0, result.getValue());

        assertRepositoriesUnchanged();
    }

    @Test
    void displayTotalRevenue_ManagerWithNoEventsUnderHim_ReturnsAllCompanyRevenue() {
        Result<Double> result =
            service.displayTotalRevenue(
                VALID_REVENUE_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        assertEquals(0.0, result.getValue());

        assertRepositoriesUnchanged();
    }

    @Test
    void displayTotalRevenue_ManagerWithoutSalesPermission_ReturnsFailure() {
        Result<Double> result =
            service.displayTotalRevenue(
                VALID_HISTORY_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("permissions"));

        assertRepositoriesUnchanged();
    }

    @Test
    void displayTotalRevenue_NonManager_ReturnsFailure() {
        Result<Double> result =
            service.displayTotalRevenue(
                VALID_NON_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("permissions"));

        assertRepositoriesUnchanged();
    }

    @Test
    void displayTotalRevenue_InvalidToken_ReturnsFailure() {
        Result<Double> result =
            service.displayTotalRevenue(
                INVALID_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
        assertEquals("Invalid Token", result.getError());

        assertRepositoriesUnchanged();
    }

    @Test
    void displayTotalRevenue_StaleUser_ReturnsFailure() {
        Result<Double> result =
            service.displayTotalRevenue(
                STALE_USER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
        assertEquals(
            "User with ID " + BAD_USER_ID + " not found.",
            result.getError()
        );

        assertRepositoriesUnchanged();
    }

    @Test
    void displayTotalRevenue_InvalidCompany_ReturnsFailure() {
        Result<Double> result =
            service.displayTotalRevenue(
                VALID_FOUNDER_TOKEN,
                BAD_COMPANY_ID
            );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("not found"));

        assertRepositoriesUnchanged();
    }

    @Test
    void displayTotalRevenue_CompanyWithNoEvents_ReturnsZero() {
        Result<Double> result =
            service.displayTotalRevenue(
                VALID_FOUNDER_TOKEN,
                COMPANY_WITH_NO_EVENTS_ID
            );

        assertTrue(result.isSuccess());
        assertEquals(0.0, result.getValue());

        assertRepositoriesUnchanged();
    }

    @Test
    void displayTotalRevenue_CompanyWithNoCompletedOrders_ReturnsZero() {
        Result<Double> result =
            service.displayTotalRevenue(
                VALID_FOUNDER_TOKEN,
                COMPANY_WITH_NO_ORDERS_ID
            );

        assertTrue(result.isSuccess());
        assertEquals(0.0, result.getValue());

        assertRepositoriesUnchanged();
    }

    @Test
    void createCompany_ValidFounderAndName_ReturnsSuccess() {
        Result<ProductionCompanyDTO> result =
            service.createProductionCompany(
                VALID_FOUNDER_TOKEN,
                BAD_COMPANY_NAME
            );

        assertTrue(result.isSuccess());
        ProductionCompanyDTO dto = result.getValue();
        assertEquals(BAD_COMPANY_NAME, dto.getName());
        assertEquals(FOUNDER_EMAIL, dto.getFounderID());
        assertTrue(dto.getId() > 0);
        assertTrue(dto.getMembers().size()==1);
        assertEquals(FOUNDER_EMAIL, dto.getMembers().get(0).getUserId());
        assertEquals(null,dto.getMembers().get(0).getAssignerId());

        assertEquals(companyRepo.getIDByName(BAD_COMPANY_NAME), dto.getId());
        verify(idGen, times(1)).getNextId();
    }

    @Test
    void createCompany_duplicateCompanyName_ReturnsFailure() {
        Result<ProductionCompanyDTO> result =
            service.createProductionCompany(
                VALID_FOUNDER_TOKEN,
                COMPANY1_NAME
            );

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("already exists"));
        assertEquals(company1.getProductionCompanyID(), companyRepo.getIDByName(COMPANY1_NAME));
        verify(idGen, times(1)).getNextId();
    }

    @Test
    void createCompany_nullCompanyName_ReturnsFailure() {
        Result<ProductionCompanyDTO> result =
            service.createProductionCompany(
                VALID_FOUNDER_TOKEN,
                null
            );

        assertFalse(result.isSuccess());
        assertEquals("Production company name cannot be empty.", result.getError());
        assertThrows(IllegalArgumentException.class, () -> companyRepo.getIDByName(null));
        verify(idGen, times(1)).getNextId();
    }

    @Test
    void createCompany_emptyCompanyName_ReturnsFailure() {
        Result<ProductionCompanyDTO> result =
            service.createProductionCompany(
                VALID_FOUNDER_TOKEN,
                ""
            );

        assertFalse(result.isSuccess());
        assertEquals("Production company name cannot be empty.", result.getError());
        assertThrows(IllegalArgumentException.class, () -> companyRepo.getIDByName(null));
        verify(idGen, times(1)).getNextId();
    }

    @Test
    void createCompany_blankCompanyName_ReturnsFailure() {
        Result<ProductionCompanyDTO> result =
            service.createProductionCompany(
                VALID_FOUNDER_TOKEN,
                "   "
            );

        assertFalse(result.isSuccess());
        assertEquals("Production company name cannot be empty.", result.getError());
        assertThrows(IllegalArgumentException.class, () -> companyRepo.getIDByName("   "));
        verify(idGen, times(1)).getNextId();
    }

    @Test
    void createCompany_invalidFounderToken_ReturnsFailure() {
        Result<ProductionCompanyDTO> result =
            service.createProductionCompany(
                INVALID_TOKEN,
                BAD_COMPANY_NAME
            );

        assertFalse(result.isSuccess());
        assertEquals("Invalid Token", result.getError());
        assertThrows(IllegalArgumentException.class, () -> companyRepo.getIDByName(BAD_COMPANY_NAME));
        verify(idGen, times(0)).getNextId();
    }

     @Test
     void createCompany_staleFounderToken_ReturnsFailure() {
         Result<ProductionCompanyDTO> result =
             service.createProductionCompany(
                 STALE_USER_TOKEN,
                 BAD_COMPANY_NAME
             );

         assertFalse(result.isSuccess());
         assertEquals(
             "User with ID " + BAD_USER_ID + " not found.",
             result.getError()
         );
         assertThrows(IllegalArgumentException.class, () -> companyRepo.getIDByName(BAD_COMPANY_NAME));
         verify(idGen, times(0)).getNextId();
    }

    @Test
    void concurrentCreateSameName_onlyOneSucceeds() throws Exception {
        String companyName = BAD_COMPANY_NAME; // name that doesn't exist yet

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Runnable task = () -> {
            try {
                start.await();

                Result<ProductionCompanyDTO> result =
                    service.createProductionCompany(VALID_FOUNDER_TOKEN, companyName);

                if (result.isSuccess()) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }

            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                done.countDown();
            }
        };

        executor.submit(task);
        executor.submit(task);

        start.countDown();   // release both threads at same time
        done.await();

        executor.shutdown();

        assertEquals(1, successCount.get());
        assertEquals(1, failCount.get());
        assertEquals(companyRepo.findByID(String.valueOf(companyRepo.getIDByName(companyName))).getName(), BAD_COMPANY_NAME);
    }

    @Test
    void concurrentCreateDifferentNames_bothSucceed() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Runnable task1 = () -> {
            try {
                start.await();

                Result<ProductionCompanyDTO> result =
                    service.createProductionCompany(VALID_FOUNDER_TOKEN, "new1");

                if (result.isSuccess()) successCount.incrementAndGet();
                else failCount.incrementAndGet();

            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                done.countDown();
            }
        };

        Runnable task2 = () -> {
            try {
                start.await();

                Result<ProductionCompanyDTO> result =
                    service.createProductionCompany(VALID_FOUNDER_TOKEN, "new2");

                if (result.isSuccess()) successCount.incrementAndGet();
                else failCount.incrementAndGet();

            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                done.countDown();
            }
        };

        executor.submit(task1);
        executor.submit(task2);

        start.countDown();
        done.await();

        executor.shutdown();

        assertEquals(2, successCount.get());
        assertEquals(0, failCount.get());
        assertEquals(companyRepo.findByID(String.valueOf(companyRepo.getIDByName("new1"))).getName(), "new1");
        assertEquals(companyRepo.findByID(String.valueOf(companyRepo.getIDByName("new2"))).getName(), "new2");
    }


    




}