package com.group16b.ApplicationLayer;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;

public class AdminManagementService {
    private final IOrderRepository orderRepo = OrderRepositoryMapImpl.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(AdminManagementService.class);
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
}
