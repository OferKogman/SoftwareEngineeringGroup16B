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
    private User manager;

    private final String VALID_TOKEN = "valid-token";
    private final String USER_ID = "1";
    private final int COMPANY_ID = 100;

    @BeforeEach
    void setUp() throws Exception {

        authService = mock(IAuthenticationService.class);
        when(authService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(authService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(VALID_TOKEN)).thenReturn(USER_ID);

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

        company = createCompany(COMPANY_ID);
        companyRepo.save(company);

        addManagerPermissions(
            USER_ID,
            ManagerPermissions.SALES_REPORT,
            ManagerPermissions.VIEW_PURCHASE_HISTORY
        );
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

    private ProductionCompany createCompany(int companyId) {

        try {
            return new ProductionCompany(
                companyId,
                "TestCompany"
            );
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private void addManagerPermissions(
        String userId,
        ManagerPermissions... permissions
    ) {

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

    // =====================================================
    // viewSalesHistory
    // =====================================================

    @Test
    void viewSalesHistory_InvalidToken_Fails() {

        authService.validToken = false;

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());
        assertEquals("Invalid Token", result.getError());
    }

    @Test
    void viewSalesHistory_NotUserToken_Fails() {

        authService.userToken = false;

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());

        assertEquals(
            "Only users are allowed to perform operation",
            result.getError()
        );
    }

    @Test
    void viewSalesHistory_StaleUser_Fails() {

        authService.subject = "999";

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("999"));
    }

    @Test
    void viewSalesHistory_CompanyNotFound_Fails() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(VALID_TOKEN, 99999);

        assertFalse(result.isSuccess());
    }

    @Test
    void viewSalesHistory_NoPermission_Fails() {

        try {
            ProductionCompany noPermissionCompany = createCompany(500);

            companyRepo.add(noPermissionCompany);

            Result<List<OrderDTO>> result =
                service.viewSalesHistory(VALID_TOKEN, 500);

            assertFalse(result.isSuccess());
        }
        catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void viewSalesHistory_NoOrders_ReturnsEmptyList() {

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(0, result.getValue().size());
    }

    @Test
    void viewSalesHistory_ReturnsOnlyCompanyOrders() {

        createCompanyEvent(10);
        createCompanyEvent(11);

        createForeignEvent(99);

        createOrderForEvent(1, 10, 100);
        createOrderForEvent(2, 11, 200);
        createOrderForEvent(3, 99, 999);

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(2, result.getValue().size());
    }

    @Test
    void viewSalesHistory_MultipleOrdersSameEvent_ReturnsAllOrders() {

        createCompanyEvent(10);

        createOrderForEvent(1, 10, 100);
        createOrderForEvent(2, 10, 200);
        createOrderForEvent(3, 10, 300);

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(3, result.getValue().size());
    }

    @Test
    void viewSalesHistory_DoesNotModifyOrders() {

        createCompanyEvent(10);

        createOrderForEvent(1, 10, 100);

        int beforeSize = orderRepo.getAll().size();

        Result<List<OrderDTO>> result =
            service.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        int afterSize = orderRepo.getAll().size();

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(beforeSize, afterSize);
    }

    // =====================================================
    // displayTotalRevenue
    // =====================================================

    @Test
    void displayTotalRevenue_InvalidToken_Fails() {

        authService.validToken = false;

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());
        assertEquals("Invalid Token", result.getError());
    }

    @Test
    void displayTotalRevenue_NotUserToken_Fails() {

        authService.userToken = false;

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());

        assertEquals(
            "Only users are allowed to perform operation",
            result.getError()
        );
    }

    @Test
    void displayTotalRevenue_StaleUser_Fails() {

        authService.subject = "999";

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("999"));
    }

    @Test
    void displayTotalRevenue_NoPermission_Fails() {

        try {
            ProductionCompany noPermissionCompany = createCompany(600);

            companyRepo.add(noPermissionCompany);

            Result<Double> result =
                service.displayTotalRevenue(VALID_TOKEN, 600);

            assertFalse(result.isSuccess());
        }
        catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void displayTotalRevenue_NoOrders_ReturnsZero() {

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(0D, result.getValue());
    }

    @Test
    void displayTotalRevenue_ReturnsCorrectSum() {

        createCompanyEvent(10);
        createCompanyEvent(11);

        createOrderForEvent(1, 10, 500);
        createOrderForEvent(2, 11, 250);

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(750D, result.getValue());
    }

    @Test
    void displayTotalRevenue_IgnoresForeignCompanyOrders() {

        createCompanyEvent(10);
        createForeignEvent(99);

        createOrderForEvent(1, 10, 500);
        createOrderForEvent(2, 99, 9999);

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(500D, result.getValue());
    }

    @Test
    void displayTotalRevenue_MultipleOrdersSameEvent_SumsCorrectly() {

        createCompanyEvent(10);

        createOrderForEvent(1, 10, 100);
        createOrderForEvent(2, 10, 200);
        createOrderForEvent(3, 10, 300);

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(600D, result.getValue());
    }

    @Test
    void displayTotalRevenue_DecimalRevenue_SumsCorrectly() {

        createCompanyEvent(10);

        createOrderForEvent(1, 10, 100.25);
        createOrderForEvent(2, 10, 200.75);

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(301D, result.getValue());
    }

    @Test
    void displayTotalRevenue_DoesNotModifyOrders() {

        createCompanyEvent(10);

        createOrderForEvent(1, 10, 100);

        int beforeSize = orderRepo.getAll().size();

        Result<Double> result =
            service.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        int afterSize = orderRepo.getAll().size();

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(beforeSize, afterSize);
    }

}