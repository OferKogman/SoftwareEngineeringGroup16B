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
import com.group16b.DomainLayer.Order.OrderType;
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
    private final int EVENT_ID = 50;

    @BeforeEach
    void setUp() throws Exception {
        mockAuthService = mock(IAuthenticationService.class);
        mockOrderRepo = mock(IOrderRepository.class);
        mockEventRepo = mock(IEventRepository.class);
        mockUserRepo = mock(IUserRepository.class);
        mockProductionCompanyRepository=mock(IProductionCompanyRepository.class);

        productionCompanyService = new ProductionCompanyService(mockAuthService,mockOrderRepo,mockEventRepo,mockUserRepo,mockProductionCompanyRepository);

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
        assertEquals("Invalid Token", result.getError());
    }

    @Test
    void testViewSalesHistory_PermissionDenied_Fail() {
        User mockUser = mock(User.class);
        ProductionCompany mockCompany=mock(ProductionCompany.class);
        when(mockUserRepo.getUserByID(USER_ID)).thenReturn(mockUser);
        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID))).thenReturn(mockCompany);
        
        doThrow(new IllegalArgumentException("Not allowed")).when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.VIEW_PURCHASE_HISTORY);

        Result<List<OrderDTO>> result = productionCompanyService.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Not allowed"));
    }


    @Test
    void testDisplayTotalRevenue_AsOwner_Success() {

        when(mockAuthService.validateToken(VALID_TOKEN))
            .thenReturn(true);

        when(mockAuthService.isUserToken(VALID_TOKEN))
            .thenReturn(true);

        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockProductionCompanyRepository.findByID(
                String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        doNothing().when(mockCompany)
            .validateUserPermissions(
                USER_ID,
                ManagerPermissions.SALES_REPORT);

        // company event
        Event matchingEvent = mock(Event.class);

        when(matchingEvent.getEventID())
            .thenReturn(EVENT_ID);

        when(mockEventRepo.searchEvents(
                null, null, null, null,
                null, null,
                null, null,
                null,
                List.of(COMPANY_ID)))
            .thenReturn(List.of(matchingEvent));

        // matching order
        Order matchingOrder = mock(Order.class);

        when(matchingOrder.getEventId())
            .thenReturn(EVENT_ID);

        when(matchingOrder.getTotalOrderprice())
            .thenReturn(500D);

        // non-matching order
        Order unrelatedOrder = mock(Order.class);

        when(unrelatedOrder.getEventId())
            .thenReturn(999);

        when(unrelatedOrder.getTotalOrderprice())
            .thenReturn(1000D);

        when(mockOrderRepo.getAllCompletedOrders())
            .thenReturn(List.of(
                matchingOrder,
                unrelatedOrder
            ));

        Result<Double> result =
            productionCompanyService.displayTotalRevenue(
                VALID_TOKEN,
                COMPANY_ID
            );

        assertTrue(result.isSuccess(), result.getError());

        // only matching company order counted
        assertEquals(500D, result.getValue());
    }

    @Test
    void testDisplayTotalRevenue_MultipleEvents_SumsCorrectly() {

        when(mockAuthService.validateToken(VALID_TOKEN))
            .thenReturn(true);

        when(mockAuthService.isUserToken(VALID_TOKEN))
            .thenReturn(true);

        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        doNothing().when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.SALES_REPORT);

        // Company has events
        Event event1 = mock(Event.class);
        when(event1.getEventID()).thenReturn(101);

        Event event2 = mock(Event.class);
        when(event2.getEventID()).thenReturn(102);

        when(mockEventRepo.searchEvents(
                null, null, null, null,
                null, null,
                null, null,
                null,
                List.of(COMPANY_ID)))
            .thenReturn(List.of(event1, event2));

        // Orders for event1 and event2
        Order order1 = mock(Order.class);
        when(order1.getEventId()).thenReturn(101);
        when(order1.getTotalOrderprice()).thenReturn(500D);

        Order order2 = mock(Order.class);
        when(order2.getEventId()).thenReturn(102);
        when(order2.getTotalOrderprice()).thenReturn(250D);

        when(mockOrderRepo.getAllCompletedOrders())
            .thenReturn(List.of(order1, order2));

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
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        when(mockUserRepo.getUserByID(USER_ID))
            .thenReturn(mockUser);

        doNothing().when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.VIEW_PURCHASE_HISTORY);

        // AUTH
        when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        // EVENT (IMPORTANT: uses searchEvents now, NOT findByID)
        Event mockEvent = mock(Event.class);
        when(mockEvent.getEventID()).thenReturn(50);

        when(mockEventRepo.searchEvents(
                null, null, null, null,
                null, null,
                null, null,
                null,
                List.of(COMPANY_ID)))
            .thenReturn(List.of(mockEvent));

        // ORDER linked to event
        Order mockOrder = mock(Order.class);
        when(mockOrder.getEventId()).thenReturn(50);

        when(mockOrder.getOrderType()).thenReturn(OrderType.FIELD); // or whatever exists

        when(mockOrderRepo.getAllCompletedOrders())
            .thenReturn(List.of(mockOrder));

        Result<List<OrderDTO>> result =
            productionCompanyService.viewSalesHistory(VALID_TOKEN, COMPANY_ID);

        assertTrue(result.isSuccess(), result.getError());
        assertEquals(1, result.getValue().size());
    }

    @Test
    void testViewSalesHistory_EventNotFound_SkipsOrder() {
        User mockUser = mock(User.class);
        when(mockUserRepo.getUserByID(USER_ID)).thenReturn(mockUser);
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
        when(mockUserRepo.getUserByID(USER_ID)).thenReturn(mockUser);

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

        when(mockAuthService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(VALID_TOKEN))
            .thenReturn(String.valueOf(USER_ID));

        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockProductionCompanyRepository.findByID(String.valueOf(COMPANY_ID)))
            .thenReturn(mockCompany);

        when(mockUserRepo.getUserByID(USER_ID))
            .thenReturn(mock(User.class));

        doNothing().when(mockCompany)
            .validateUserPermissions(USER_ID, ManagerPermissions.SALES_REPORT);

        // ---------- EVENTS ----------
        Event mockEvent = mock(Event.class);
        when(mockEvent.getEventID()).thenReturn(100);

        when(mockEventRepo.searchEvents(
                null, null, null, null,
                null, null,
                null, null,
                null,
                List.of(COMPANY_ID)))
            .thenReturn(List.of(mockEvent));

        // ---------- ORDERS ----------
        Order mockOrder = mock(Order.class);
        when(mockOrder.getEventId()).thenReturn(100);
        when(mockOrder.getTotalOrderprice()).thenReturn(300D);

        // required by DTO (avoid previous crash)
        when(mockOrder.getOrderType()).thenReturn(OrderType.FIELD);

        when(mockOrderRepo.getAllCompletedOrders())
            .thenReturn(List.of(mockOrder));

        // ---------- CALL ----------
        Result<Double> result =
            productionCompanyService.displayTotalRevenue(VALID_TOKEN, COMPANY_ID);

        // ---------- ASSERT ----------
        assertTrue(result.isSuccess(), result.getError());
        assertEquals(300D, result.getValue());
    }
}