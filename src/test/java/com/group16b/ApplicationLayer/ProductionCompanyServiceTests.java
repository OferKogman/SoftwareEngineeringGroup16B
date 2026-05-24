package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Order.OrderType;
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

    private ProductionCompany company;
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
    private final String MANAGER_FOR_HISTORY_ID = "1";
    private final String MANAGER_FOR_REVENUE_ID = "2";
    private final String MANAGER_WITH_NO_USEFUL_PERMS_ID = "3";
    private final String NON_MANAGER_ID = "4";
    private final String OWNER_ID = "5";
    private final int COMPANY_ID = 100;
    private final int BAD_COMPANY_ID = 999;
    private final String BAD_USER_ID = "999";
    private final String COMPANY_ID_STRING = String.valueOf(COMPANY_ID);
    private final String FOUNDER_EMAIL="founder@example.com";
    private final String TEST_COMPANY_NAME="TestCompany";


    
    
    
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

        manager = createUser(USER_ID);
        userRepo.save(manager);

        founder=createUser(FOUNDER_EMAIL);
        userRepo.save(founder);

        company = createCompany(COMPANY_ID, TEST_COMPANY_NAME, FOUNDER_EMAIL);
        companyRepo.save(company);

        addManagerPermissions(
            USER_ID,
            ManagerPermissions.SALES_REPORT,
            ManagerPermissions.VIEW_PURCHASE_HISTORY);
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

        try {
            return new Event(
                eventId,
                "Test Event " + eventId,
                "Description",
                "Hall",
                100,
                null,
                companyId
            );
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Order createOrder(int orderId, int eventId, double price) {

        try {
            return new Order(
                orderId,
                USER_ID,
                eventId,
                List.of(),
                price,
                OrderType.FIELD
            );
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addManagerPermissions(String userId,ManagerPermissions... permissions) {

        try {
            company.addManager(userId);

            for (ManagerPermissions permission : permissions) {
                company.addPermissionToManager(userId, permission);
            }

            companyRepo.update(company);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createCompanyEvent(int eventId) {

        try {
            Event event = createEvent(eventId, COMPANY_ID);
            eventRepo.add(event);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createForeignEvent(int eventId) {

        try {
            Event event = createEvent(eventId, 999);
            eventRepo.add(event);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createOrderForEvent(
        int orderId,
        int eventId,
        double price
    ) {

        try {
            orderRepo.add(createOrder(orderId, eventId, price));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}