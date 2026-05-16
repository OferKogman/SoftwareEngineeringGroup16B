package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.LocationServicePhotonImpl;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyPolicyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.SystemAdminRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.PaymentService;
import com.group16b.InfrastructureLayer.TicketGateway;

public class StartupService {
    private final static Logger logger = LoggerFactory.getLogger(StartupService.class);
    private final AdminManagementService adminManagementService;
    private final CompanyHierarchyService companyHierarchyService;
    private final EventService eventService;
    private final OrderService orderService;
    private final ProductionCompanyService productionCompanyService;
    //private final PurchaseHistoryService purchaseHistoryService;
    private final PurchasePolicyService purchasePolicyService;
    private final ReserveService reserveService;
    private final UserLoginService userLoginService;
    private final UserService userService;


    public StartupService() {
        logger.info("Initializing infrastructures...");
        AuthenticationServiceJWTImpl authService = new AuthenticationServiceJWTImpl("mySuperSecretKeyForUsers123456789", "mySuperSecretKeyForAdmins123456789");
        LocationServicePhotonImpl locationService = new LocationServicePhotonImpl();
        UserRepositoryMapImpl userRepositoryMapImpl = UserRepositoryMapImpl.getInstance();
        VenueRepositoryMapImpl venueRepositoryMapImpl = VenueRepositoryMapImpl.getInstance();
        OrderRepositoryMapImpl orderRepositoryMapImpl = OrderRepositoryMapImpl.getInstance();
        EventRepositoryMapImpl eventRepositoryMapImpl = EventRepositoryMapImpl.getInstance();
        VirtualQueueRepositoryMapImpl queueRepositoryMapImpl = VirtualQueueRepositoryMapImpl.getInstance();
        ProductionCompanyPolicyRepositoryMapImpl productionCompanyRepositoryMapImpl = ProductionCompanyPolicyRepositoryMapImpl.getInstance();
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
        purchasePolicyService = new PurchasePolicyService(authService);
        reserveService = new ReserveService(authService);
        userLoginService = new UserLoginService(authService);
        userService = new UserService(authService, ticketGateway);

        logger.info("Adding default system admin...");
        SystemAdminRepositoryMapImpl systemAdminRepository = new SystemAdminRepositoryMapImpl();
        SystemAdmin systemAdmin = new SystemAdmin("1", "admin", "password", "admin@example.com");
        systemAdminRepository.addSystemAdmin(systemAdmin);
        logger.info("StartupService initialization complete.");
    }
}
