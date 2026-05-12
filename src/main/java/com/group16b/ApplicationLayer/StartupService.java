package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.LocationServicePhotonImpl;
import com.group16b.InfrastructureLayer.MapDBs.SystemAdminRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.PaymentService;
import com.group16b.InfrastructureLayer.TicketGateway;

public class StartupService {
    private static Logger logger = LoggerFactory.getLogger(StartupService.class);
    private AdminManagementService adminManagementService;
    private CompanyHierarchyService companyHierarchyService;
    private EventService eventService;
    private OrderService orderService;
    private ProductionCompanyService productionCompanyService;
    private PurchaseHistoryService purchaseHistoryService;
    private PurchasePolicyService purchasePolicyService;
    private ReserveService reserveService;
    private UserLoginService userLoginService;
    private UserService userService;


    public StartupService() {
        logger.info("Initializing infrastructures...");
        AuthenticationServiceJWTImpl authService = new AuthenticationServiceJWTImpl("mySuperSecretKeyForUsers123456789", "mySuperSecretKeyForAdmins123456789");
        LocationServicePhotonImpl locationService = new LocationServicePhotonImpl();
        UserRepositoryMapImpl userRepositoryMapImpl = UserRepositoryMapImpl.getInstance();
        PaymentService paymentService = new PaymentService();
        TicketGateway ticketGateway = new TicketGateway();

        logger.info("Initializing domain services...");
        CompanyHierarchyDomainService companyHierarchyDomainService = new CompanyHierarchyDomainService();
        EventFilteringService eventFilteringService = new EventFilteringService();
        

        logger.info("Initializing application services...");
        adminManagementService = new AdminManagementService(authService);
        companyHierarchyService = new CompanyHierarchyService(authService, companyHierarchyDomainService);
        eventService = new EventService(authService, locationService, eventFilteringService);
        orderService = new OrderService(authService);
        productionCompanyService = new ProductionCompanyService(authService);
        purchaseHistoryService = new PurchaseHistoryService();
        purchasePolicyService = new PurchasePolicyService(authService);
        reserveService = new ReserveService(authService);
        userLoginService = new UserLoginService(authService, userRepositoryMapImpl);
        userService = new UserService(authService, ticketGateway);

        logger.info("Adding default system admin...");
        ISystemAdminRepository systemAdminRepository = SystemAdminRepositoryMapImpl.getInstance();
        SystemAdmin systemAdmin = new SystemAdmin(1, "admin", "password", "admin@example.com");
        systemAdminRepository.addSystemAdmin(systemAdmin);
        logger.info("StartupService initialization complete.");
    }
}
