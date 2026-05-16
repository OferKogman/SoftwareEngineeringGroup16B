package com.group16b.ApplicationLayer;


import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.Role;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;


public class ProductionCompanyService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductionCompanyService.class);

    private final IOrderRepository orderRepo = OrderRepositoryMapImpl.getInstance();
    private final IEventRepository eventRepo = EventRepositoryMapImpl.getInstance();
    private final IUserRepository userRepo = UserRepositoryMapImpl.getInstance();
	private final IAuthenticationService authenticationService;

    public ProductionCompanyService(IAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public Result<List<OrderDTO>> viewSalesHistory(String sTocken, int productionCompanyID){

    try {
            logger.info("ProductionCompanyService.viewSalesHistory: Retrieving sales history for specific company");

            // validate production company token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sTocken)  ) {
                logger.error("ProductionCompanyService.viewSalesHistory: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isUserToken(sTocken)) {
                logger.error("ProductionCompanyService.viewSalesHistory: Unauthorized access attempt by non-production company user");
                return Result.makeFail("Unauthorized access");
            }
            User user = userRepo.getUserByEmail(Integer.valueOf((authenticationService.extractSubjectFromToken(sTocken))));

            user.validatePermissions(productionCompanyID, ManagerPermissions.VIEW_PURCHASE_HISTORY);

            List<Order> orders = orderRepo.getAllCompletedOrders();
            for (Order order : orders) {
                Event event = eventRepo.getEventByID(order.getEventId());
                if (event == null) {
                    logger.warn("ProductionCompanyService.viewSalesHistory: Event with ID {} not found for order {}", order.getEventId(), order.getOrderId());
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
        } catch (IllegalArgumentException e) {
            logger.error("ProductionCompanyService.viewSalesHistory: Permission error while retrieving sales history", e);
            return Result.makeFail("Permission error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("ProductionCompanyService.viewSalesHistory: An unexpected error occurred while retrieving sales history", e);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Integer> displayTotalRevenue(String sTocken, int productionCompanyID) {
        
        // validate user
        try {
            logger.info("ProductionCompanyService.displayTotalRevenue: Calculating total revenue for production company");

            // validate production company token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sTocken)  ) {
                logger.error("ProductionCompanyService.displayTotalRevenue: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isUserToken(sTocken)) {
                logger.error("ProductionCompanyService.displayTotalRevenue: Unauthorized access attempt by non-production company user");
                return Result.makeFail("Unauthorized access");
            }
            User user = userRepo.getUserByEmail(Integer.valueOf((authenticationService.extractSubjectFromToken(sTocken))));
            if (user == null) {
                logger.error("ProductionCompanyService.displayTotalRevenue: User not found for token");
                return Result.makeFail("User not found");
            }
            user.validatePermissions(productionCompanyID, ManagerPermissions.SALES_REPORT);
            
            // if user is managaer. look for his father and call the recursion
            Role userRole = user.getRole(productionCompanyID);
            if (userRole instanceof Manager manager){
                int ownerId = manager.getAssignerID();
                User parentUser = userRepo.getUserByEmail(ownerId);
                Role parentRole = parentUser.getRole(productionCompanyID);
                if (!(parentRole instanceof Owner)) {
                    logger.error("ProductionCompanyService.displayTotalRevenue: Parent user with ID {} is not an owner", parentUser.getEmail());
                    return Result.makeFail("Parent user is not an owner");
                }
                int totalRevenue = getAllrevinue(user.getEmail(), productionCompanyID, 0);
                return Result.makeOk(totalRevenue);
            }
            // if user is owner/founder, call the recursion directly
            else if (userRole instanceof Owner) {
                int totalRevenue = getAllrevinue(user.getEmail(), productionCompanyID, 0);
                return Result.makeOk(totalRevenue);
            }
            else{
                logger.error("ProductionCompanyService.displayTotalRevenue: User with ID {} does not have a valid role for revenue calculation", user.getEmail());
                return Result.makeFail("User does not have a valid role for revenue calculation");
            }
            
        } catch (Exception e) {
            logger.error("ProductionCompanyService.displayTotalRevenue: An unexpected error occurred while calculating total revenue", e);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        
        }
    }

    private int getAllrevinue(int userId, int productionCompanyID, int totalRevenue) {
        List<Order> userOrders = orderRepo.getAllCompletedOrders();
            userOrders = userOrders.stream()
                    .filter(order -> order.isBelongsToSubject(userId + ""))
                    .collect(Collectors.toList());
        for (Order order : userOrders) {
                totalRevenue += order.getTotalOrderprice();
        }

        Role userRole = userRepo.getUserByEmail(userId).getRole(productionCompanyID);
        if (userRole instanceof Owner owner){
            List<Manager> managers = owner.getAssignedManagers();
            if (managers == null || managers.isEmpty()) {
                return totalRevenue;
            }
            for (Manager manager : managers) {
                totalRevenue = getAllrevinue(manager.getUserID(), productionCompanyID, totalRevenue);
            }
        }

        return totalRevenue;
    }

}