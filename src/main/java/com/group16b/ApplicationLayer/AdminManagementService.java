package com.group16b.ApplicationLayer;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.ProductionCompanyDTO;
import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import org.springframework.stereotype.Service;

@Service
public class AdminManagementService {
    private static final Logger logger = LoggerFactory.getLogger(AdminManagementService.class);
    private final IRepository<User> userRepository;
    private IProductionCompanyRepository productionCompanyRepo;
    private final OrderRepositoryMapImpl orderRepo;
    private final IEventRepository eventRepo;
	private final IAuthenticationService authenticationService;
    private IRepository<SystemAdmin> systemAdminRepo;
    private final IPaymentGateway paymentGateway;
    private final ITicketGateway ticketGateway;

    public AdminManagementService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepository, OrderRepositoryMapImpl orderRepo, IEventRepository eventRepo, IRepository<User> userRepository, IRepository<SystemAdmin> systemAdminRepo, IPaymentGateway paymentGateway, ITicketGateway ticketGateway) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepo=productionCompanyRepository;
        this.orderRepo = orderRepo;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
        this.systemAdminRepo = systemAdminRepo;
        this.paymentGateway=paymentGateway;
        this.ticketGateway=ticketGateway;
    }


    public Result<List<OrderDTO>> viewAllPurchesHistory(String sTocken) {
        try {
            logger.info("AdminManagementService.viewAllPurchesHistory: Retrieving all completed orders for purchase history");

            // validate admin token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sTocken)  ) {
                logger.warn("AdminManagementService.viewAllPurchesHistory: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.warn("AdminManagementService.viewAllPurchesHistory: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAll();
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
                logger.warn("AdminManagementService.viewPurchesHistoryByCompany: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.warn("AdminManagementService.viewPurchesHistoryByCompany: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAll();
            for (Order order : orders) {
                Event event = eventRepo.findByID(String.valueOf(order.getEventId()));
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

    public Result<List<OrderDTO>> viewPurchesHistoryByUser(String sTocken, String userId){
        try {
            logger.info("AdminManagementService.viewPurchesHistoryByUser: Retrieving completed orders for specific user");

            // validate admin token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sTocken)  ) {
                logger.warn("AdminManagementService.viewPurchesHistoryByUser: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.warn("AdminManagementService.viewPurchesHistoryByUser: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAll();
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
                logger.warn("AdminManagementService.closeProductionCompany: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sToken)) {
                logger.warn("AdminManagementService.closeProductionCompany: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }
            productionCompanyRepo.findByID(String.valueOf(productionCompanyId)); // validate company exists, throws error if not
            

            List<Integer> productionCompanyIDs = new LinkedList<>();
            productionCompanyIDs.add(productionCompanyId);

            List<Event> companyEvents = eventRepo.searchEvents(null, null, null, null, null, null, null, null, null, productionCompanyIDs);
                if(!companyEvents.isEmpty()) {
                    deactivateEvents(companyEvents);
                    logger.info("AdminManagementService.closeProductionCompany: Deactivated {} events associated with production company ID {}", companyEvents.size(), productionCompanyId);
                }

                productionCompanyRepo.delete(String.valueOf(productionCompanyId));
                logger.info("AdminManagementService.closeProductionCompany: Successfully closed production company with ID {}", productionCompanyId);

            return Result.makeOk("Production company with ID " + productionCompanyId + " has been closed successfully.");
        }
        catch(IllegalArgumentException e) {
            logger.error("AdminManagementService.closeProductionCompany: Production company with ID {} not found", productionCompanyId, e);
            return Result.makeFail("Production company with ID " + productionCompanyId + " not found");
        }
        catch(Exception e) {
            logger.error("AdminManagementService.closeProductionCompany: Error occurred while closing production company with ID {}", productionCompanyId, e);
            return Result.makeFail("Error occurred while closing production company with ID " + productionCompanyId);
        }

    }

    public Result<String> removeUser(String userID, String sessionToken){
        try {
            logger.info("Attempting to remove the user subscription of user ID {}", userID);
            if (!authenticationService.validateToken(sessionToken)  ) {
                logger.error("AdminManagementService.removeUser: Invalid token");
                return Result.makeFail("Invalid token");
            }

            if (!authenticationService.isAdminToken(sessionToken)) {
                logger.error("AdminManagementService.removeUser: Must be in an active admive session to remove user");
                return Result.makeFail("Unauthorized access");
            }
            
            User user = userRepository.findByID(userID);

            List<Integer> companyIDs = productionCompanyRepo.getAllUserComapnies(user);
            for (Integer companyID : companyIDs)
            {
                boolean success = false;

                while (!success)
                {
                    try
                    {
                        ProductionCompany company = productionCompanyRepo.findByID(String.valueOf(companyID));
                        if (company.isFounder(userID))
                        {
                            closeProductionCompany(companyID, sessionToken);
                            // company no longer exists after closure
                            success = true;
                            logger.info("AdminManagementService.removeUser: User {} was founder of company {}. Closed company as part of user removal process.", userID, companyID);
                            continue;
                        }
                        company.adminRemoveUser(userID);
                        productionCompanyRepo.save(company);
                        success = true;
                        logger.info("AdminManagementService.removeUser: Successfully removed user {} from company {} as part of user removal process.", userID, companyID);
                    }
                    catch (IllegalArgumentException e)
                    {
                        logger.warn("AdminManagementService.removeUser: Company {} not found while removing user {}",companyID,userID);
                        // stop retrying, company is gone
                        success = true;
                    }
                    catch (OptimisticLockingFailureException e)
                    {
                        logger.warn("Optimistic lock conflict while removing user {} from company {}. Retrying...",userID,companyID);
                    }
                }
            }
            userRepository.delete(userID);
            logger.info("AdminManagementService.removeUser: Successfully removed user with ID {}", userID);
            
            return Result.makeOk("User with ID: " + userID + ", , has been removed");
        }catch (IllegalArgumentException e) {
            logger.error("AdminManagementService.removeUser: User with ID {} not found", userID, e);
            return Result.makeFail("User with ID " + userID + " not found");
        }catch (Exception e) {
            logger.error("AdminManagementService.removeUser: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while saving the layout.");
        }    
    }

    private void deactivateEvents(List<Event> events) {
        logger.info("AdminManagementService.deactivateEvents: Deactivating {} events", events.size());
        for (Event e : events) {
            deactivateEventAndRefundUser(e.getEventID());
            logger.info("AdminManagementService.deactivateEvents: Deactivated event with ID {}", e.getEventID());
        }
    }
    private void deactivateEventAndRefundUser(int eventID)
    {
         logger.info("AdminManagementService.deactivateEventAndRefundUser: Deactivating event {}", eventID);
         try{
            while(true){
                try{
                    Event event=eventRepo.findByID(String.valueOf(eventID));
                    event.deactivateEvent();
                    eventRepo.save(event);
                    break;
                } catch(OptimisticLockingFailureException e){
                    logger.warn("AdminManagementService.deactivateEventAndRefundUser: optimistic lock exception when saving event {}",eventID);
                    continue;

                } catch (IllegalArgumentException e){
                    logger.warn("AdminManagementService.deactivateEventAndRefundUser: IllegalArgumentException: {}",e.getMessage());
                    return;
                } catch (IllegalStateException e){
                    logger.warn("AdminManagementService.deactivateEventAndRefundUser: IllegalStateException: {}",e.getMessage());
                    break;
                }
            }
             logger.info("AdminManagementService.deactivateEventAndRefundUser: Deactivated event {}", eventID);
            List<Order> orders=orderRepo.getByEventId(eventID);
            for(Order order : orders)
            {
                if(cancelOrder(order.getOrderId()))
                    refundOrder(order.getOrderId());
            }


         } catch(Exception e){
            
         }
    }

    private boolean cancelOrder(String orderID)
    {
        logger.info("AdminManagementService.cancelOrder: canceling order: {}", orderID);
        while(true)
        {   
            try{
                Order order= orderRepo.findByID(orderID);
                boolean toRefund=false;

                if(order.isActive()){ //only cancel
                    order.CancelOrder();
                }
                else if(order.isCompleted())
                { //refund
                    order.CancelOrder();
                    toRefund=true;
                }
                else{ //already canceled
                    logger.info("AdminManagementService.cancelOrder: order {} was already canceled", orderID);
                    return false;
                }
                orderRepo.save(order);
                return toRefund;

            } catch(OptimisticLockingFailureException e){
                logger.warn("AdminManagementService.cancelOrder: concurency issue while canceling order: {}", orderID);
                continue;
            } catch(IllegalArgumentException e){
                logger.warn("AdminManagementService.cancelOrder: IllegalArgumentException: {}", e.getMessage());
                return false;
            }
        }
    } 
    

    private void refundOrder(String orderID)
    {
        logger.info("AdminManagementService.refundOrder: refunding order: {}", orderID);

    } 

    public Result<String> registerNewAdmin(String sToken, String newAdminUsername, String newAdminPassword, String newAdminEmail){
        try{
            logger.info("AdminManagementService.registerNewAdmin: Attempting to register new system admin with username {}", newAdminUsername);
            if (!authenticationService.validateToken(sToken)  ) {
                logger.warn("AdminManagementService.registerNewAdmin: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sToken)) {
                logger.warn("AdminManagementService.registerNewAdmin: Must be in an active admin session to register new admin");
                return Result.makeFail("Unauthorized access");
            }

            SystemAdmin newAdmin = new SystemAdmin(newAdminUsername, newAdminPassword, newAdminEmail);
            systemAdminRepo.save(newAdmin);
            logger.info("AdminManagementService.registerNewAdmin: Successfully registered new system admin with username {}", newAdminUsername);
            return Result.makeOk("System admin with username: " + newAdminUsername + ", has been registered successfully");
        }
        catch(IllegalArgumentException e) {
            logger.warn("AdminManagementService.registerNewAdmin: Invalid input - {}", e.getMessage());
            return Result.makeFail("Failed to register new admin: " + e.getMessage());
        } catch (OptimisticLockingFailureException e) {//if happens, then admin already created
            logger.warn("AdminManagementService.registerNewAdmin: Optimistic lock conflict while registering new admin with username {}.", newAdminUsername);
            return Result.makeFail("Admin with username " + newAdminUsername + " already exists");
        }
        catch (Exception e) {
            logger.error("AdminManagementService.registerNewAdmin: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while registering the new admin.");
        }
    
    }

    public Result<List<ProductionCompanyDTO>> getAllProductionCompanies(String sToken){
        try{
            logger.info("AdminManagementService.getAllProductionCompanies: Attempting to retrieve all production companies");
            verifyAdminToken(sToken);
            List<ProductionCompany> companies = productionCompanyRepo.getAll();
            List<ProductionCompanyDTO> companyDTOs = companies.stream()
                    .map(company -> new ProductionCompanyDTO(company))
                    .collect(Collectors.toList());
            logger.info("AdminManagementService.getAllProductionCompanies: Successfully retrieved {} production companies", companyDTOs.size());
            return Result.makeOk(companyDTOs);
        } catch(IllegalArgumentException e){
            logger.warn("AdminManagementService.getAllProductionCompanies: IllegalArgumentException: {}", e.getLocalizedMessage());
            return Result.makeFail(e.getMessage());
        } catch(AuthException e){
            logger.warn("AdminManagementService.getAllProductionCompanies: Authentication error: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch(Exception e){
            logger.error("AdminManagementService.getAllProductionCompanies: unexpected error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while retrieving production companies.");
        }
    }

    public Result<List<UserDTO>> getAllUsers(String sToken){
        try{
            logger.info("AdminManagementService.getAllUsers: Attempting to retrieve all users");
            verifyAdminToken(sToken);

            List<User> users = userRepository.getAll();
            List<UserDTO> userDTOs = users.stream()
                    .map(user -> new UserDTO(user))
                    .collect(Collectors.toList());
            
            logger.info("AdminManagementService.getAllUsers: Successfully retrieved {} users", userDTOs.size());
            return Result.makeOk(userDTOs);
        } catch(IllegalArgumentException e){
            logger.warn("AdminManagementService.getAllUsers: IllegalArgumentException: {}", e.getLocalizedMessage());
            return Result.makeFail(e.getMessage());
        } catch(AuthException e){
            logger.warn("AdminManagementService.getAllUsers: Authentication error: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch(Exception e){
            logger.error("AdminManagementService.getAllUsers: unexpected error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while retrieving users.");
        }
    }

    private void verifyAdminToken(String sToken) throws IllegalArgumentException {
        if (!authenticationService.validateToken(sToken)  ) {
            logger.warn("AdminManagementService.verifyAdminToken: Invalid token");
            throw new AuthException("Invalid token");
        }
        if (!authenticationService.isAdminToken(sToken)) {
            logger.warn("AdminManagementService.verifyAdminToken: Unauthorized access attempt by non-admin user");
            throw new AuthException("Unauthorized access");
        }
        systemAdminRepo.findByID(authenticationService.extractSubjectFromToken(sToken)); // validate admin exists, throws error if not
    }
}
