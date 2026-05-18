package com.group16b.ApplicationLayer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;


public class ProductionCompanyService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductionCompanyService.class);

    private final IOrderRepository orderRepo;
    private final IEventRepository eventRepo;
    private final IUserRepository userRepo;
    private final IProductionCompanyRepository productionRepo;
	private final IAuthenticationService authenticationService;

    public ProductionCompanyService(IAuthenticationService authenticationService,IOrderRepository orderRepo, IEventRepository eventRepo, IUserRepository userRepo, IProductionCompanyRepository productionRepo) {
        this.authenticationService = authenticationService;
        this.productionRepo=productionRepo;
        this.orderRepo=orderRepo;
        this.eventRepo=eventRepo;
        this.userRepo=userRepo;
    }

    public Result<List<OrderDTO>> viewSalesHistory(String sessionToken, int productionCompanyID){
        try {
            logger.info("ProductionCompanyService.viewSalesHistory: Retrieving sales history for specific company");
            
            int userID=validateAndGetUserID(sessionToken);
            logger.info("ProductionCompanyService.viewSalesHistory: Session token verified successfully.");
            
            logger.info("ProductionCompanyService.viewSalesHistory: retrieving company {}",productionCompanyID);
            ProductionCompany company=productionRepo.findByID(String.valueOf(productionCompanyID));

            logger.info("ProductionCompanyService.viewSalesHistory: validating user {} have permissions in company {}",userID,productionCompanyID);
            company.validateUserPermissions(userID, ManagerPermissions.VIEW_PURCHASE_HISTORY);

            logger.info("ProductionCompanyService.viewSalesHistory: retrieving all events for company {}",productionCompanyID);
            

            logger.info("ProductionCompanyService.viewSalesHistory: collecting all orders for the company {} events",productionCompanyID);
            List<OrderDTO> orderDTOs =getAllCompletedCompanyOrders(productionCompanyID).stream()
                    .map(OrderDTO::new)
                    .toList();

            logger.info("ProductionCompanyService.viewSalesHistory: succesfully collected all orders.");
            return Result.makeOk(orderDTOs);
        }catch(AuthException e){
            logger.warn("ProductionCompanyService.viewSalesHistory: Auth error: "+e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("ProductionCompanyService.viewSalesHistory: Illegal Argument Error: ", e);
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("ProductionCompanyService.viewSalesHistory: An unexpected error occurred: ", e);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Double> displayTotalRevenue(String sessionToken, int productionCompanyID) {
        try {
            logger.info("ProductionCompanyService.displayTotalRevenue: Calculating total revenue for company {}",productionCompanyID);

            int userID=validateAndGetUserID(sessionToken);
            logger.info("ProductionCompanyService.displayTotalRevenue: Session token verified successfully.");

            logger.info("ProductionCompanyService.displayTotalRevenue: retrieving company {}",productionCompanyID);
            ProductionCompany company =productionRepo.findByID(String.valueOf(productionCompanyID));

            // validate permission through company
            logger.info("ProductionCompanyService.displayTotalRevenue: validating user {} permissions in company {}",userID,productionCompanyID);
            company.validateUserPermissions(userID,ManagerPermissions.SALES_REPORT);

            logger.info("ProductionCompanyService.displayTotalRevenue: calculating total revenue for company {}",productionCompanyID);
            double totalRevenue = getAllRevenue(getAllCompletedCompanyOrders(productionCompanyID));

            logger.info("ProductionCompanyService.displayTotalRevenue: successfuly calculated total revenue for company {}.",productionCompanyID);
            return Result.makeOk(totalRevenue);

        }
        catch(AuthException e)
        {
            logger.warn("ProductionCompanyService.displayTotalRevenue: Auth error: "+e.getMessage());
            return Result.makeFail(e.getMessage());
        } 
        catch(IllegalArgumentException e)
        {
            logger.warn("ProductionCompanyService.displayTotalRevenue: IllegalArgumentException: "+e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
            logger.error("ProductionCompanyService.displayTotalRevenue: Unexpected error",e);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    //gets all orders for the company
    private List<Order> getAllCompletedCompanyOrders(int companyID)
    {
        Set<Integer> companyEventIDs =
                eventRepo.searchEvents(
                    null, null, null, null,
                    null, null,
                    null, null,
                    null,
                    List.of(companyID)
                ).stream()
                .map(Event::getEventID)
                .collect(Collectors.toSet());
        return orderRepo.getAllCompletedOrders().stream()
                    .filter(order -> companyEventIDs.contains(order.getEventId()))
                    .toList();
    }

    private double getAllRevenue(List<Order> orders) 
    {
        return orders.stream()
                .mapToDouble(Order::getTotalOrderprice)
                .sum();
    }
    
    private int validateAndGetUserID(String sessionToken)
    {
        if (!authenticationService.validateToken(sessionToken)  ) {
            throw new AuthException("Invalid Token");
        }
        if (!authenticationService.isUserToken(sessionToken)) {
            throw new AuthException("Only users are allowed to perform operation");
        }
        int userID=Integer.parseInt(authenticationService.extractSubjectFromToken(sessionToken));
        //verify user exists in the database, i.e not a stale user
        userRepo.getUserByID(userID);
        return userID;
    }

}