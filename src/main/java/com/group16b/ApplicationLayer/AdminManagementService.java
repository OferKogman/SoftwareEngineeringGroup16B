package com.group16b.ApplicationLayer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Role;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.SystemAdminRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class AdminManagementService {
    private static final Logger logger = LoggerFactory.getLogger(AdminManagementService.class);
    private final IUserRepository userRepository = UserRepositoryMapImpl.getInstance();
    private IProductionCompanyRepository productionCompanyRepo;
    private final IOrderRepository orderRepo = OrderRepositoryMapImpl.getInstance();
    private final IEventRepository eventRepo = EventRepositoryMapImpl.getInstance();
    private final CompanyHierarchyDomainService companyHierarchyDomainService = new CompanyHierarchyDomainService();
	private final IAuthenticationService authenticationService;
    private SystemAdminRepositoryMapImpl systemAdminRepo = new SystemAdminRepositoryMapImpl();

    public AdminManagementService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepository) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepo=productionCompanyRepository;
    }


    public Result<List<OrderDTO>> viewAllPurchesHistory(String sTocken) {
        try {
            logger.info("AdminManagementService.viewAllPurchesHistory: Retrieving all completed orders for purchase history");

            // validate admin token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sTocken)  ) {
                logger.error("AdminManagementService.viewAllPurchesHistory: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.error("AdminManagementService.viewAllPurchesHistory: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAllCompletedOrders();
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(order -> new OrderDTO(order))
                    .collect(Collectors.toList());
            return Result.makeOk(orderDTOs);
        } catch (Exception e) {
            logger.error("AdminManagementService.viewAllPurchesHistory: Error occurred while retrieving purchase history", e);
            return Result.makeFail("Error occurred while retrieving purchase history");
        }
    }

    public Result<List<OrderDTO>> viewPurchesHistoryByCompany(String sTocken, int productionCompanyID){
        try {
            logger.info("AdminManagementService.viewPurchesHistoryByCompany: Retrieving completed orders for specific company");

            // validate admin token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sTocken)  ) {
                logger.error("AdminManagementService.viewPurchesHistoryByCompany: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.error("AdminManagementService.viewPurchesHistoryByCompany: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAllCompletedOrders();
            for (Order order : orders) {
                Event event = eventRepo.getEventByID(order.getEventId());
                if (event == null) {
                    logger.warn("AdminManagementService.viewPurchesHistoryByCompany: Event with ID {} not found for order {}", order.getEventId(), order.getOrderId());
                    orders.remove(order);
                    continue; 
                }
                if (event.getEventProductionCompanyID() != productionCompanyID) {
                    orders.remove(order);
                }
            }
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(order -> new OrderDTO(order))
                    .collect(Collectors.toList());
            return Result.makeOk(orderDTOs);
        } catch (Exception e) {
            logger.error("AdminManagementService.viewPurchesHistoryByCompany: Error occurred while retrieving purchase history", e);
            return Result.makeFail("Error occurred while retrieving purchase history");
        }
    }

    public Result<List<OrderDTO>> viewPurchesHistoryByUser(String sTocken, int userId){
        try {
            logger.info("AdminManagementService.viewPurchesHistoryByUser: Retrieving completed orders for specific user");

            // validate admin token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sTocken)  ) {
                logger.error("AdminManagementService.viewPurchesHistoryByUser: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.error("AdminManagementService.viewPurchesHistoryByUser: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAllCompletedOrders();
            orders = orders.stream()
                    .filter(order -> order.isBelongsToSubject(userId + ""))
                    .collect(Collectors.toList());
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(order -> new OrderDTO(order))
                    .collect(Collectors.toList());
            return Result.makeOk(orderDTOs);
        } catch (IllegalArgumentException e) {
            logger.error("AdminManagementService.viewPurchesHistoryByUser: Invalid user ID provided", e);
            return Result.makeFail("Invalid user ID");
        }
        catch (Exception e) {
            logger.error("AdminManagementService.viewPurchesHistoryByUser: Error occurred while retrieving purchase history", e);
            return Result.makeFail("Error occurred while retrieving purchase history");
        }
    }

    public Result<String> closeProductionCompany(int productionCompanyId, String sToken) {
        try{
            logger.info("AdminManagementService.closeProductionCompany: Attempting to close production company with ID {}", productionCompanyId);
            // validate admin token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sToken)  ) {
                logger.error("AdminManagementService.closeProductionCompany: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sToken)) {
                logger.error("AdminManagementService.closeProductionCompany: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }
            ProductionCompany company = productionCompanyRepo.findByID(String.valueOf(productionCompanyId));
            
            if(company == null) {
                System.out.println("Production company with ID " + productionCompanyId + " does not exist.");
                return Result.makeFail("Production company with ID " + productionCompanyId + " does not exist.");
            }

            List<Integer> productionCompanyIDs = new LinkedList<>();
            productionCompanyIDs.add(productionCompanyId);

            List<Event> companyEvents = eventRepo.searchEvents(null, null, null, null, null, null, null, null, null, productionCompanyIDs);
            List<User> companyUsers = company.getAssociatedUsers();
                if(!companyEvents.isEmpty()) {
                    deactivateEvents(companyEvents);
                    logger.info("AdminManagementService.closeProductionCompany: Deactivated {} events associated with production company ID {}", companyEvents.size(), productionCompanyId);
                }

                if(!companyUsers.isEmpty()) {
                    deactivateUsers(companyUsers, productionCompanyId);
                    logger.info("AdminManagementService.closeProductionCompany: Deactivated {} users associated with production company ID {}", companyUsers.size(), productionCompanyId);
                }
                productionCompanyRepo.delete(String.valueOf(productionCompanyId));
                logger.info("AdminManagementService.closeProductionCompany: Successfully closed production company with ID {}", productionCompanyId);
            return Result.makeOk("Production company with ID " + productionCompanyId + " has been closed successfully.");
        }
        catch(Exception e) {
            logger.error("AdminManagementService.closeProductionCompany: Error occurred while closing production company with ID {}", productionCompanyId, e);
            return Result.makeFail("Error occurred while closing production company with ID " + productionCompanyId);
        }

    }

    public Result<String> removeUser(int userID, String sessionToken){
        try {
            logger.info("Attempting to remove the user subscription of user ID {}", userID);
            if (!authenticationService.validateToken(sessionToken)  ) {
                logger.error("Invalid token");
                return Result.makeFail("Invalid token");
            }

            if (!authenticationService.isAdminToken(sessionToken)) {
                logger.error("Must be in an active admive session to remove user");
                return Result.makeFail("Unauthorized access");
            }
            
            if(!userRepository.userExists(userID)){
                logger.error("user to remove doesn't exist! given ID: {}", userID);
                return Result.makeFail("Invalid ID");    
            }

            User userToRemove = userRepository.getUserByID(userID);
            for (Map.Entry<Integer, Role> entry : userToRemove.getRoles().entrySet()) {
                int companyID = entry.getKey();
                Role companyRole = entry.getValue();
                if(userToRemove.isFounderOfCompany(companyID)){
                    closeProductionCompany(companyID, sessionToken);//Cannot simply delete and give to someone else this role - whole company goes kapoot        
                } else if(companyRole != null){
                    companyHierarchyDomainService.removeUserFromCompany(userToRemove, companyID);
                }
            }

            userRepository.deleteUser(userID);
            
            return Result.makeOk("User with ID: " + userID + ", , has been removed");
        } catch (Exception e) {
            logger.error("System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while saving the layout.");
        }    
    }

    private void deactivateEvents(List<Event> events) {
        for (Event e : events) {
            e.deactivateEvent();
        }
    }

    private void deactivateUsers(List<User> users, int productionCompanyId) {
        for (User u : users) {
            u.removeRole(productionCompanyId);
        }
    }

    public Result<String> registerNewAdmin(String sToken, String newAdminID, String newAdminUsername, String newAdminPassword, String newAdminEmail){
        try {
            logger.info("AdminManagementService.registerNewAdmin: Attempting to register new admin with ID {}", newAdminID);
            if (!authenticationService.validateToken(sToken)  ) {
                logger.error("AdminManagementService.registerNewAdmin: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sToken)) {
                logger.error("AdminManagementService.registerNewAdmin: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }
            
            boolean checkIfAdminAlreadyExists = systemAdminRepo.findByID(newAdminID) != null;
            boolean checkIfUsernameAlreadyExists = systemAdminRepo.getSystemAdminByUsername(newAdminUsername) != null;
            
            if(checkIfAdminAlreadyExists ) {
                logger.warn("AdminManagementService.registerNewAdmin: Attempt to register admin with existing ID {}", newAdminID);
                return Result.makeFail("Admin with ID " + newAdminID + " already exists");
            }
            if(checkIfUsernameAlreadyExists){
                logger.warn("AdminManagementService.registerNewAdmin: Attempt to register admin with existing username {}", newAdminUsername);
                return Result.makeFail("Admin with username " + newAdminUsername + " already exists");
            }
            SystemAdmin newAdmin = new SystemAdmin(newAdminID, newAdminUsername, newAdminPassword, newAdminEmail);
            systemAdminRepo.save(newAdmin);
            logger.info("AdminManagementService.registerNewAdmin: Successfully registered new admin with ID {}", newAdminID);
            return Result.makeOk("Admin with ID " + newAdminID + " has been registered successfully.");
        }
        catch(Exception e) {
            logger.error("AdminManagementService.registerNewAdmin: Error occurred while registering new admin with ID {}", newAdminID, e);
            return Result.makeFail("Error occurred while registering new admin with ID " + newAdminID);
        }
    }
}
