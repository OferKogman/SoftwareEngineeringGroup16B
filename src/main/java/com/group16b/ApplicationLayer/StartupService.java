package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.LocationServicePhotonImpl;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
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
    private final PurchasePolicyService purchasePolicyService;
    private final ReserveService reserveService;
    private final UserLoginService userLoginService;
    private final UserService userService;


    public StartupService() {
        logger.info("Initializing infrastructures...");
        AuthenticationServiceJWTImpl authService = new AuthenticationServiceJWTImpl("mySuperSecretKeyForUsers123456789", "mySuperSecretKeyForAdmins123456789");
        LocationServicePhotonImpl locationService = new LocationServicePhotonImpl();
        UserRepositoryMapImpl userRepositoryMapImpl = new UserRepositoryMapImpl();
        VenueRepositoryMapImpl venueRepositoryMapImpl = new VenueRepositoryMapImpl();
        OrderRepositoryMapImpl orderRepositoryMapImpl = new OrderRepositoryMapImpl();
        EventRepositoryMapImpl eventRepositoryMapImpl = new EventRepositoryMapImpl();
        VirtualQueueRepositoryMapImpl queueRepositoryMapImpl = new VirtualQueueRepositoryMapImpl();
        ProductionCompanyRepositoryMapImpl productionCompanyRepositoryMapImpl = new ProductionCompanyRepositoryMapImpl();
        SystemAdminRepositoryMapImpl systemAdminRepositoryMapImpl = new SystemAdminRepositoryMapImpl();
        PaymentService paymentService = new PaymentService();
        TicketGateway ticketGateway = new TicketGateway();

        logger.info("Initializing domain services...");
        EventFilteringService eventFilteringService = new EventFilteringService(productionCompanyRepositoryMapImpl, eventRepositoryMapImpl, venueRepositoryMapImpl);
        

        logger.info("Initializing application services...");
        adminManagementService = new AdminManagementService(authService,productionCompanyRepositoryMapImpl, orderRepositoryMapImpl, eventRepositoryMapImpl, userRepositoryMapImpl, systemAdminRepositoryMapImpl);
        companyHierarchyService = new CompanyHierarchyService(authService,productionCompanyRepositoryMapImpl, userRepositoryMapImpl);
        eventService = new EventService(authService, locationService, eventFilteringService, productionCompanyRepositoryMapImpl, queueRepositoryMapImpl, venueRepositoryMapImpl, eventRepositoryMapImpl, userRepositoryMapImpl);
        orderService = new OrderService(authService,productionCompanyRepositoryMapImpl, paymentService, venueRepositoryMapImpl, eventRepositoryMapImpl, userRepositoryMapImpl, orderRepositoryMapImpl, ticketGateway);
        productionCompanyService = new ProductionCompanyService(authService,orderRepositoryMapImpl,eventRepositoryMapImpl,userRepositoryMapImpl,productionCompanyRepositoryMapImpl);
        purchasePolicyService = new PurchasePolicyService(authService,productionCompanyRepositoryMapImpl, eventRepositoryMapImpl, userRepositoryMapImpl);
        reserveService = new ReserveService(authService,productionCompanyRepositoryMapImpl, queueRepositoryMapImpl, venueRepositoryMapImpl, eventRepositoryMapImpl, orderRepositoryMapImpl);
        userLoginService = new UserLoginService(userRepositoryMapImpl, authService);
        userService = new UserService(authService, ticketGateway, venueRepositoryMapImpl, userRepositoryMapImpl, orderRepositoryMapImpl, eventRepositoryMapImpl);

        logger.info("Adding default system admin...");
        ISystemAdminRepository systemAdminRepository = new SystemAdminRepositoryMapImpl();
        SystemAdmin systemAdmin = new SystemAdmin("1", "admin", "password", "admin@example.com");
        systemAdminRepository.save(systemAdmin);
        logger.info("StartupService initialization complete.");
    }
}
