package com.group16b.ApplicationLayer;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.PurchasePolicy.AgePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MinTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PurchasePolicyServiceTests {

    private PurchasePolicyService service;
    private IAuthenticationService authService;
    private IProductionCompanyRepository companyRepo;
    private IEventRepository eventRepo;
    private IRepository<User> userRepo;
    private ProductionCompany mockCompany;
    private User mockUser;

    private static final String VALID_TOKEN = "valid-token";
    private static final String USER_ID = "user1";
    private static final int COMPANY_ID = 1;

    @BeforeEach
    void setUp() {
        authService = mock(IAuthenticationService.class);
        companyRepo = mock(IProductionCompanyRepository.class);
        eventRepo = mock(IEventRepository.class);
        userRepo = mock(IRepository.class);

        service = new PurchasePolicyService(authService, companyRepo, eventRepo, userRepo);

        mockUser = mock(User.class);
        mockCompany = mock(ProductionCompany.class);

        when(authService.validateToken(VALID_TOKEN)).thenReturn(true);
        when(authService.isUserToken(VALID_TOKEN)).thenReturn(true);
        when(authService.extractSubjectFromToken(VALID_TOKEN)).thenReturn(USER_ID);
        when(userRepo.findByID(USER_ID)).thenReturn(mockUser);
        when(companyRepo.findByID(String.valueOf(COMPANY_ID))).thenReturn(mockCompany);
    }

    @Test
    public void testCreateCompanyPurchasePolicySuccess() {
        PurchasePolicy policy = new MinTicketsPolicy(1);
        doNothing().when(mockCompany).validateUserPermissions(USER_ID, ManagerPermissions.PURCHASE_POLICY);
        doNothing().when(mockCompany).addPurchasePolicy(policy);
        doNothing().when(companyRepo).save(mockCompany);
        Result<Boolean> result = service.createCompanyPurchasePolicy(VALID_TOKEN, COMPANY_ID, policy);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testCreateCompanyPurchasePolicyInvalidTokenFails() {
        when(authService.validateToken("bad-token")).thenReturn(false);
        Result<Boolean> result = service.createCompanyPurchasePolicy("bad-token", COMPANY_ID, new MinTicketsPolicy(1));
        assertFalse(result.isSuccess());
    }

    @Test
    public void testCreateCompanyPurchasePolicyAdminTokenFails() {
        when(authService.isUserToken(VALID_TOKEN)).thenReturn(false);
        Result<Boolean> result = service.createCompanyPurchasePolicy(VALID_TOKEN, COMPANY_ID, new MinTicketsPolicy(1));
        assertFalse(result.isSuccess());
    }

    @Test
    public void testCreateCompanyPurchasePolicyNoPermissionFails() {
        doThrow(new IllegalArgumentException("No permission")).when(mockCompany)
                .validateUserPermissions(USER_ID, ManagerPermissions.PURCHASE_POLICY);
        Result<Boolean> result = service.createCompanyPurchasePolicy(VALID_TOKEN, COMPANY_ID, new MinTicketsPolicy(1));
        assertFalse(result.isSuccess());
    }

    @Test
    public void testCreateCompanyPurchasePolicyCompanyNotFoundFails() {
        when(companyRepo.findByID(String.valueOf(COMPANY_ID)))
                .thenThrow(new IllegalArgumentException("Company not found"));
        Result<Boolean> result = service.createCompanyPurchasePolicy(VALID_TOKEN, COMPANY_ID, new MinTicketsPolicy(1));
        assertFalse(result.isSuccess());
    }
}