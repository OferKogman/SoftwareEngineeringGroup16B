package com.group16b.ApplicationLayer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;

public class ProductionCompanyServiceTests {

    private ProductionCompanyService productionCompanyService;
    private IAuthenticationService mockAuthService;
    private IOrderRepository mockOrderRepo;
    private IEventRepository mockEventRepo;
    private IUserRepository mockUserRepo;
    private IProductionCompanyRepository mockProductionCompanyRepository;

    private final String VALID_TOKEN = "valid-token";
    private final int COMPANY_ID = 100;
    private final int USER_ID = 1;

    @BeforeEach
    void setUp() throws Exception {
        mockAuthService = mock(IAuthenticationService.class);
        mockOrderRepo = mock(IOrderRepository.class);
        mockEventRepo = mock(IEventRepository.class);
        mockUserRepo = mock(IUserRepository.class);
        mockProductionCompanyRepository=mock(IProductionCompanyRepository.class);

        productionCompanyService = new ProductionCompanyService(mockAuthService,mockProductionCompanyRepository);

        //Inject Repository Mocks using Reflection - bypassing singletons initializations(user, event order)
        Field orderField = ProductionCompanyService.class.getDeclaredField("orderRepo");
        orderField.setAccessible(true);
        orderField.set(productionCompanyService, mockOrderRepo);

        Field eventField = ProductionCompanyService.class.getDeclaredField("eventRepo");
        eventField.setAccessible(true);
        eventField.set(productionCompanyService, mockEventRepo);

        Field userField = ProductionCompanyService.class.getDeclaredField("userRepo");
        userField.setAccessible(true);
        userField.set(productionCompanyService, mockUserRepo);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(USER_ID));
        
    }


    @Test
    void testViewSalesHistory_InvalidToken_Fail() {
        when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(false);

        Result<List<OrderDTO>> result = productionCompanyService.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());
        assertEquals("Invalid token", result.getError());
    }

    @Test
    void testViewSalesHistory_PermissionDenied_Fail() {
        User mockUser = mock(User.class);
        ProductionCompany mockCompany=mock(ProductionCompany.class);
        when(mockUserRepo.findByID(USER_ID)).thenReturn(mockUser);
        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID))).thenReturn(mockCompany);
        
        doThrow(new IllegalArgumentException("Not allowed")).when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.VIEW_PURCHASE_HISTORY);

        Result<List<OrderDTO>> result = productionCompanyService.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Permission error"));
    }


    @Test
    void testDisplayTotalRevenue_AsOwner_Success() {

        when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        doNothing().when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.SALES_REPORT);

        when(mockCompany.getAllSubordinates(USER_ID))
            .thenReturn(List.of());

        Order mockOrder = mock(Order.class);

        
        when(mockOrder.getSubjectId())
            .thenReturn(String.valueOf(USER_ID));

        when(mockOrder.getTotalOrderprice())
            .thenReturn(500D);

        List<Order> orderList = new ArrayList<>();
        orderList.add(mockOrder);

        when(mockOrderRepo.getAllCompletedOrders())
            .thenReturn(orderList);

        Result<Double> result =
            productionCompanyService.displayTotalRevenue(
                VALID_TOKEN,
                COMPANY_ID
            );

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(500D, result.getValue());
    }

    @Test
    void testDisplayTotalRevenue_RecursionWithMultipleManagers() {

        when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        doNothing().when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.SALES_REPORT);

        int managerID = 2;

        when(mockCompany.getAllSubordinates(USER_ID))
            .thenReturn(List.of(managerID));

        // Orders
        Order ownerOrder = mock(Order.class);
        when(ownerOrder.getSubjectId())
            .thenReturn(String.valueOf(USER_ID));
        when(ownerOrder.getTotalOrderprice())
            .thenReturn(500D);

        Order managerOrder = mock(Order.class);
        when(managerOrder.getSubjectId())
            .thenReturn(String.valueOf(managerID));
        when(managerOrder.getTotalOrderprice())
            .thenReturn(250D);

        when(mockOrderRepo.getAllCompletedOrders())
            .thenReturn(List.of(ownerOrder, managerOrder));

        Result<Double> result =
            productionCompanyService.displayTotalRevenue(
                VALID_TOKEN,
                COMPANY_ID
            );

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(750D, result.getValue());
    }

    @Test
    void testViewSalesHistory_Success() {
        User mockUser = mock(User.class);
        ProductionCompany mockCompany=mock(ProductionCompany.class);
        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID))).thenReturn(mockCompany);
        when(mockUserRepo.findByID(USER_ID)).thenReturn(mockUser);
        doNothing().when(mockCompany).validateUserPermissions(USER_ID, ManagerPermissions.VIEW_PURCHASE_HISTORY);

        // FIX 1: RETURNS_DEEP_STUBS prevents OrderDTO from throwing NullPointerExceptions!
        Order mockOrder = mock(Order.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);//allows to control and mock return of return in chains
        Event mockEvent = mock(Event.class);
        
        // FIX 2: CopyOnWriteArrayList prevents the ConcurrentModificationException in the for-loop!
        List<Order> orderList = new CopyOnWriteArrayList<>();
        orderList.add(mockOrder);
        
        when(mockOrderRepo.getAllCompletedOrders()).thenReturn(orderList);
        when(mockOrder.getEventId()).thenReturn(50);
        when(mockEventRepo.findByID(String.valueOf(50))).thenReturn(mockEvent);
        when(mockEvent.getEventProductionCompanyID()).thenReturn(COMPANY_ID); 

        Result<List<OrderDTO>> result = productionCompanyService.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        // FIX 3: If it fails, print out the hidden exception message so we can see it!
        assertTrue(result.isSuccess(), "Service crashed with error: " + result.getError());
        assertEquals(1, result.getValue().size());
    }

    @Test
    void testViewSalesHistory_EventNotFound_SkipsOrder() {
        User mockUser = mock(User.class);
        when(mockUserRepo.findByID(USER_ID)).thenReturn(mockUser);
        when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        doNothing().when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.VIEW_PURCHASE_HISTORY);

        Order mockOrder = mock(Order.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        List<Order> orderList = new CopyOnWriteArrayList<>();
        orderList.add(mockOrder);
        
        when(mockOrderRepo.getAllCompletedOrders()).thenReturn(orderList);
        when(mockOrder.getEventId()).thenReturn(50);
        
        when(mockEventRepo.findByID(String.valueOf(50))).thenReturn(null); 

        Result<List<OrderDTO>> result = productionCompanyService.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), "Service crashed with error: " + result.getError());
        assertEquals(0, result.getValue().size(), "Order should be skipped if event is missing");
    }

    @Test
    void testViewSalesHistory_WrongCompany_SkipsOrder() {
        User mockUser = mock(User.class);
        when(mockUserRepo.findByID(USER_ID)).thenReturn(mockUser);

        Order mockOrder = mock(Order.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        Event mockEvent = mock(Event.class);
        
        List<Order> orderList = new CopyOnWriteArrayList<>();
        orderList.add(mockOrder);

                when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        doNothing().when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.VIEW_PURCHASE_HISTORY);
        
        when(mockOrderRepo.getAllCompletedOrders()).thenReturn(orderList);
        when(mockOrder.getEventId()).thenReturn(50);
        when(mockEventRepo.findByID(String.valueOf(50))).thenReturn(mockEvent);
        
        when(mockEvent.getEventProductionCompanyID()).thenReturn(999); 

        Result<List<OrderDTO>> result = productionCompanyService.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), "Service crashed with error: " + result.getError());
        assertEquals(0, result.getValue().size(), "Order should be skipped if it belongs to a different company");
    }

    
    @Test
    void testDisplayTotalRevenue_AsManagerUnderOwner_Success() {

        int ownerID = 2;

        User mockManagerUser = mock(User.class);
        User mockOwnerUser = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        when(mockUserRepo.findByID(USER_ID)).thenReturn(mockManagerUser);
        when(mockManagerUser.getUserID()).thenReturn(USER_ID);

        when(mockUserRepo.findByID(ownerID)).thenReturn(mockOwnerUser);

        doNothing().when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.SALES_REPORT);

        Order mockOrder = mock(Order.class);
        when(mockOrder.getSubjectId()).thenReturn(String.valueOf(USER_ID));
        when(mockOrder.getTotalOrderprice()).thenReturn(300D);

        when(mockOrderRepo.getAllCompletedOrders())
            .thenReturn(List.of(mockOrder));

        Result<Double> result =
            productionCompanyService.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(300D, result.getValue());
    }
}