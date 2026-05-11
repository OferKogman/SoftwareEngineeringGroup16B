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
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;

public class AdminManagementService {
    private static final Logger logger = LoggerFactory.getLogger(AdminManagementService.class);

    private final IOrderRepository orderRepo = OrderRepositoryMapImpl.getInstance();
    private final IEventRepository eventRepo = EventRepositoryMapImpl.getInstance();
	private final IAuthenticationService authenticationService;

    public AdminManagementService(IAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    public Result<List<OrderDTO>> viewAllPurchesHistory(String sTocken) {
        try {
            logger.info("AdminManagementService.viewAllPurchesHistory: Retrieving all completed orders for purchase history");

            // validate admin token (this is a placeholder, implement actual validation logic)
            if (!authenticationService.validateToken(sTocken)  ) {
                logger.error("AdminManagementService.viewAllPurchesHistory: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!"Admin".equals(authenticationService.extractRoleFromToken(sTocken))) {
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
            if (!"Admin".equals(authenticationService.extractRoleFromToken(sTocken))) {
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
            if (!"Admin".equals(authenticationService.extractRoleFromToken(sTocken))) {
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
}
