package com.group16b.ApplicationLayer;

import java.util.HashSet;
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
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;


public class ProductionCompanyService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductionCompanyService.class);

    private final IRepository<Order> orderRepo;
    private final IEventRepository eventRepo;
    private final IRepository<User> userRepo;
    private final IProductionCompanyRepository productionRepo;
	private final IAuthenticationService authenticationService;

    public ProductionCompanyService(IAuthenticationService authenticationService,IRepository<Order> orderRepo, IEventRepository eventRepo, IRepository<User> userRepo, IProductionCompanyRepository productionRepo) {
        this.authenticationService = authenticationService;
        this.productionRepo=productionRepo;
        this.orderRepo=orderRepo;
        this.eventRepo=eventRepo;
        this.userRepo=userRepo;
    }

    public Result<List<OrderDTO>> viewSalesHistory(String sessionToken, int productionCompanyID){
        try {
            logger.info("ProductionCompanyService.viewSalesHistory: Retrieving sales history for specific company");
            
            String userID=validateAndGetUserID(sessionToken);
            logger.info("ProductionCompanyService.viewSalesHistory: Session token verified successfully.");
            
            logger.info("ProductionCompanyService.viewSalesHistory: retrieving company {}",productionCompanyID);
            ProductionCompany company=productionRepo.findByID(String.valueOf(productionCompanyID));

            logger.info("ProductionCompanyService.viewSalesHistory: validating user {} have permissions in company {}",userID,productionCompanyID);
            company.validateUserPermissions(userID, ManagerPermissions.VIEW_PURCHASE_HISTORY);

            logger.info("ProductionCompanyService.viewSalesHistory: retrieving all events for company {}",productionCompanyID);
            

            logger.info("ProductionCompanyService.viewSalesHistory: collecting all orders for the company {} events",productionCompanyID);
            List<OrderDTO> orderDTOs =getCompletedOrdersByEventIDs(getCompanyEventIDs(productionCompanyID)).stream()
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

            String userID=validateAndGetUserID(sessionToken);
            logger.info("ProductionCompanyService.displayTotalRevenue: Session token verified successfully.");

            logger.info("ProductionCompanyService.displayTotalRevenue: retrieving company {}",productionCompanyID);
            ProductionCompany company =productionRepo.findByID(String.valueOf(productionCompanyID));

            // validate permission through company
            logger.info("ProductionCompanyService.displayTotalRevenue: validating user {} permissions in company {}",userID,productionCompanyID);
            company.validateUserPermissions(userID,ManagerPermissions.SALES_REPORT);

            logger.info("ProductionCompanyService.displayTotalRevenue: calculating total revenue for company {}",productionCompanyID);
            double totalRevenue = getAllRevenue(getCompletedOrdersByEventIDs(getManagedCompanyEventIDs(company,userID)));

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
    private Set<Integer> getCompanyEventIDs(int companyID)
    {
        return eventRepo.searchEvents(
                null, null, null, null,
                null, null,
                null, null,
                null,
                List.of(companyID)
            ).stream()
            .map(Event::getEventID)
            .collect(Collectors.toSet());
    }

    //duplication for efficency, instead of traversing the stream twice, traverse once and filter by permissions, then get the orders for those events
    private Set<Integer> getManagedCompanyEventIDs(ProductionCompany company, String userID)
    {
        if(company.isFounder(userID))//divert for efficency if founder, as they have access to all events of the company
            return getCompanyEventIDs(company.getProductionCompanyID());

        Set<String> allowedManagers =new HashSet<>(company.getOwnershipDescendants(userID));

        allowedManagers.add(userID);

        return eventRepo.searchEvents(
                null, null, null, null,
                null, null,
                null, null,
                null,
                List.of(company.getProductionCompanyID())
            ).stream()
            .filter(event ->
                allowedManagers.contains(event.getOwnerId())
            )
            .map(Event::getEventID)
            .collect(Collectors.toSet());
    }

    private List<Order> getCompletedOrdersByEventIDs(Set<Integer> eventIDs)
    {
        return orderRepo.getAll().stream()
                .filter(Order::isCompleted)
                .filter(order -> eventIDs.contains(order.getEventId()))
                .toList();
    }

    private double getAllRevenue(List<Order> orders) 
    {
        return orders.stream()
                .mapToDouble(Order::getTotalOrderprice)
                .sum();
    }
    
    private String validateAndGetUserID(String sessionToken)
    {
        if (!authenticationService.validateToken(sessionToken)  ) {
            throw new AuthException("Invalid Token");
        }
        if (!authenticationService.isUserToken(sessionToken)) {
            throw new AuthException("Only users are allowed to perform operation");
        }
        String userID=authenticationService.extractSubjectFromToken(sessionToken);
        //verify user exists in the database, i.e not a stale user
        userRepo.findByID(userID);
        return userID;
    }

}