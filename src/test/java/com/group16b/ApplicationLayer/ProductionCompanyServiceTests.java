package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
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
    private User managerForHistory;
    private User managerForRevenue;
    private User managerWithNoUsefullPerms;
    private User founder;
    private User non_manager;
    private User owner;

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
    private final int BAD_COMPANY_ID = 999;
    private final String BAD_USER_ID = "999";
    private final String COMPANY1_ID_STRING = String.valueOf(COMPANY1_ID);
    private final String COMPANY2_ID_STRING = String.valueOf(COMPANY2_ID);
    private final String FOUNDER_EMAIL="founder@example.com";
    private final String COMPANY1_NAME="Company1";
    private final String COMPANY2_NAME="Company2";

    private Event company1Event1;
    private Event company1Event2;
    private Event company2Event1;

    private Order company1Order1;
    private Order company1Order2;
    private Order company1Order3;
    private Order company2Order1;


    
    
    
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

        service = new ProductionCompanyService(
            authService,
            orderRepo,
            eventRepo,
            userRepo,
            companyRepo
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

        assignManagerToCompany(company1, MANAGER_FOR_HISTORY_ID, FOUNDER_EMAIL, Set.of(ManagerPermissions.VIEW_PURCHASE_HISTORY));
        assignOwnerToCompany(company1, OWNER_ID, FOUNDER_EMAIL);
        assignManagerToCompany(company1, MANAGER_FOR_REVENUE_ID, OWNER_ID, Set.of(ManagerPermissions.SALES_REPORT));
        assignManagerToCompany(company1, MANAGER_WITH_NO_USEFUL_PERMS_ID, OWNER_ID, Set.of(ManagerPermissions.CUSTOMER_SUPPORT));

        companyRepo.save(company1);
        companyRepo.save(company2);

        company1Event1 = createEvent(1, COMPANY1_ID);
        company1Event2 = createEvent(2, COMPANY1_ID);
        company2Event1 = createEvent(3, COMPANY2_ID);

        eventRepo.save(company1Event1);
        eventRepo.save(company1Event2);
        eventRepo.save(company2Event1);

        company1Order1 = createOrder(company1Event1.getEventID(), 100, NON_MANAGER_ID);
        company1Order2 = createOrder(company1Event2.getEventID(), 200, NON_MANAGER_ID);
        company1Order3 = createOrder(company1Event2.getEventID(), 150, NON_MANAGER_ID);
        company2Order1 = createOrder(company2Event1.getEventID(), 999, NON_MANAGER_ID);

        orderRepo.save(company1Order1);
        orderRepo.save(company1Order2);
        orderRepo.save(company1Order3);
        orderRepo.save(company2Order1);
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

    private Event createEvent(int eventId, int companyId) {
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
            ),"owner"
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

        List<OrderDTO> orders = result.getValue();

        assertEquals(3, orders.size());

        double total =
            orders.stream()
                .mapToDouble(OrderDTO::getTocalOrderPrice)
                .sum();

        assertEquals(450, total);
    }

    @Test
    void viewSalesHistory_HistoryManager_ReturnsAllCompanyOrders() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_HISTORY_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        List<OrderDTO> orders = result.getValue();

        assertEquals(3, orders.size());

        double total =
            orders.stream()
                .mapToDouble(OrderDTO::getTocalOrderPrice)
                .sum();

        assertEquals(450, total);
    }

    @Test
    void viewSalesHistory_Owner_ReturnsAllCompanyOrders() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_OWNER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        List<OrderDTO> orders = result.getValue();

        assertEquals(3, orders.size());

        double total =
            orders.stream()
                .mapToDouble(OrderDTO::getTocalOrderPrice)
                .sum();

        assertEquals(450, total);
    }

    @Test
    void viewSalesHistory_RevenueManagerWithoutHistoryPermission_ReturnsFailure() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_REVENUE_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
    }

    @Test
    void viewSalesHistory_ManagerWithoutPermission_ReturnsFailure() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_NO_USEFUL_PERMS_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
    }

    @Test
    void viewSalesHistory_NonManager_ReturnsFailure() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_NON_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
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
    }

    @Test
    void viewSalesHistory_StaleUserToken_ReturnsFailure() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                STALE_USER_TOKEN,
                COMPANY1_ID
            );

        assertFalse(result.isSuccess());
    }

    @Test
    void viewSalesHistory_BadCompanyID_ReturnsFailure() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_FOUNDER_TOKEN,
                BAD_COMPANY_ID
            );

        assertFalse(result.isSuccess());
    }

    @Test
    void viewSalesHistory_DoesNotReturnForeignCompanyOrders() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(
                VALID_HISTORY_MANAGER_TOKEN,
                COMPANY1_ID
            );

        assertTrue(result.isSuccess());

        List<OrderDTO> orders = result.getValue();

        assertEquals(3, orders.size());

        boolean containsForeignOrder =
            orders.stream()
                .anyMatch(order ->
                    order.getTocalOrderPrice() == 999
                );

        assertFalse(containsForeignOrder);
    }




}