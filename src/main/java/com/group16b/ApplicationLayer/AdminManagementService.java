package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.ProductionCompanyDTO;
import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Enums.RefundStatus;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Exceptions.RefundFailedException;
import com.group16b.ApplicationLayer.Exceptions.RefundStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.RevokeTicketFailureException;
import com.group16b.ApplicationLayer.Exceptions.TicketRevokeUnknownStatusException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.RefundResult;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.User;

@Service
@Transactional
public class AdminManagementService {
    private static final Logger logger = LoggerFactory.getLogger(AdminManagementService.class);
    private final IRepository<User> userRepository;
    private IProductionCompanyRepository productionCompanyRepo;
    private final IOrderRepository orderRepo;
    private final IEventRepository eventRepo;
    private final IAuthenticationService authenticationService;
    private final IPaymentGateway paymentService;
    private final ITicketGateway ticketService;
    private IRepository<SystemAdmin> systemAdminRepo;

    public AdminManagementService(IAuthenticationService authenticationService,
            IProductionCompanyRepository productionCompanyRepository, IOrderRepository orderRepo,
            IEventRepository eventRepo, IRepository<User> userRepository, IRepository<SystemAdmin> systemAdminRepo,
            IPaymentGateway paymentService, ITicketGateway ticketService) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepo = productionCompanyRepository;
        this.orderRepo = orderRepo;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
        this.systemAdminRepo = systemAdminRepo;
        this.ticketService = ticketService;
        this.paymentService = paymentService;
    }

    public Result<List<OrderDTO>> viewAllPurchesHistory(String sTocken) {
        try {
            logger.info(
                    "AdminManagementService.viewAllPurchesHistory: Retrieving all completed orders for purchase history");
            if (sTocken == null || sTocken.trim().isEmpty()) {
                logger.warn("AdminManagementService.viewAllPurchesHistory: Invalid token");
                return Result.makeFail("Invalid token");
            }
            // validate admin token (this is a placeholder, implement actual validation
            // logic)
            if (!authenticationService.validateToken(sTocken)) {
                logger.warn("AdminManagementService.viewAllPurchesHistory: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.warn(
                        "AdminManagementService.viewAllPurchesHistory: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAll();
            List<OrderDTO> orderDTOs = orders.stream()
                    .filter(order -> !order.isActive())
                    .map(order -> new OrderDTO(order))
                    .collect(Collectors.toList());
            return Result.makeOk(orderDTOs);
        } catch (Exception e) {
            logger.error(
                    "AdminManagementService.viewAllPurchesHistory: Error occurred while retrieving purchase history",
                    e);
            return Result.makeFail("Error occurred while retrieving purchase history");
        }
    }

    public Result<List<OrderDTO>> viewPurchesHistoryByCompany(String sTocken, int productionCompanyID) {
        try {
            logger.info(
                    "AdminManagementService.viewPurchesHistoryByCompany: Retrieving completed orders for specific company");
            if (sTocken == null || sTocken.trim().isEmpty()) {
                logger.warn("AdminManagementService.viewPurchesHistoryByCompany: Invalid token");
                return Result.makeFail("Invalid token");
            }
            // validate admin token (this is a placeholder, implement actual validation
            // logic)
            if (!authenticationService.validateToken(sTocken)) {
                logger.warn("AdminManagementService.viewPurchesHistoryByCompany: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.warn(
                        "AdminManagementService.viewPurchesHistoryByCompany: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAll();
            List<Order> filtered = orders.stream()
                    .filter(order -> {
                        Event event = eventRepo.findByID(String.valueOf(order.getEventId()));
                        return event.getEventProductionCompanyID() == productionCompanyID && order.isCompleted();
                    }).filter(order -> !order.isActive())
                    .toList();

            List<OrderDTO> orderDTOs = filtered.stream()
                    .map(order -> new OrderDTO(order))
                    .collect(Collectors.toList());
            return Result.makeOk(orderDTOs);
        } catch (Exception e) {
            logger.error(
                    "AdminManagementService.viewPurchesHistoryByCompany: Error occurred while retrieving purchase history",
                    e);
            return Result.makeFail("Error occurred while retrieving purchase history");
        }
    }

    public Result<List<OrderDTO>> viewPurchesHistoryByUser(String sTocken, String userId) {
        try {
            logger.info(
                    "AdminManagementService.viewPurchesHistoryByUser: Retrieving completed orders for specific user");
            if (sTocken == null || sTocken.trim().isEmpty()) {
                logger.warn("AdminManagementService.viewPurchesHistoryByUser: Invalid token");
                return Result.makeFail("Invalid token");
            }
            // validate admin token (this is a placeholder, implement actual validation
            // logic)
            if (!authenticationService.validateToken(sTocken)) {
                logger.warn("AdminManagementService.viewPurchesHistoryByUser: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sTocken)) {
                logger.warn(
                        "AdminManagementService.viewPurchesHistoryByUser: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }

            List<Order> orders = orderRepo.getAll();
            orders = orders.stream()
                    .filter(order -> order.isBelongsToSubject(userId + ""))
                    .filter(order -> !order.isActive())
                    .collect(Collectors.toList());
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(order -> new OrderDTO(order))
                    .collect(Collectors.toList());
            return Result.makeOk(orderDTOs);
        } catch (IllegalArgumentException e) {
            logger.error("AdminManagementService.viewPurchesHistoryByUser: Invalid user ID provided", e);
            return Result.makeFail("Invalid user ID");
        } catch (Exception e) {
            logger.error(
                    "AdminManagementService.viewPurchesHistoryByUser: Error occurred while retrieving purchase history",
                    e);
            return Result.makeFail("Error occurred while retrieving purchase history");
        }
    }

    public Result<String> deleteProductionCompany(int productionCompanyId, String sToken) {
        try {
            logger.info(
                    "AdminManagementService.deleteProductionCompany: Attempting to close production company with ID {}",
                    productionCompanyId);
            if (sToken == null || sToken.trim().isEmpty()) {
                logger.warn("AdminManagementService.deleteProductionCompany: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.validateToken(sToken)) {
                logger.warn("AdminManagementService.deleteProductionCompany: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sToken)) {
                logger.warn(
                        "AdminManagementService.deleteProductionCompany: Unauthorized access attempt by non-admin user");
                return Result.makeFail("Unauthorized access");
            }
            productionCompanyRepo.findByID(String.valueOf(productionCompanyId));

            List<Integer> productionCompanyIDs = new LinkedList<>();
            productionCompanyIDs.add(productionCompanyId);

            List<Event> companyEvents = eventRepo.searchEvents(null, null, null, null, null, null, null, null, null,
                    productionCompanyIDs);

            Set<Integer> eventIds = companyEvents.stream()
                    .map(Event::getEventID)
                    .collect(Collectors.toSet());
            List<Order> completedOrders = getCompletedOrdersByEventIDs(eventIds);

            for (Order order : completedOrders) {
                // Fetch the event and skip refunds if it's already in the past
                Event event = eventRepo.findByID(String.valueOf(order.getEventId()));
                if (event != null && !event.getEventStartTime().isAfter(LocalDateTime.now())) {
                    logger.info("Event {} has already passed, skipping refund for order {}", event.getEventID(),
                            order.getOrderId());
                    continue; // skips the refund and moves to the next order
                }
                if (cancelOrder(order.getOrderId())) {
                    try {
                        paymentService.cancelPayment(order.getTransactionId());
                        for (String ticket : (order.getState()).getTickets()) {
                            ticketService.revokeTicket(ticket);
                        }
                    } catch (Exception e) {
                        logger.error(
                                "AdminManagementService.deleteProductionCompany: Failed to refund or revoke for transaction {}",
                                order.getTransactionId(), e);
                    }
                }
            }
            logger.info(
                    "AdminManagementService.deleteProductionCompany: Refunded {} events associated with production company ID {}",
                    companyEvents.size(), productionCompanyId);

            if (!companyEvents.isEmpty()) {
                deactivateEventsNoRefund(companyEvents);
                logger.info(
                        "AdminManagementService.deleteProductionCompany: Deactivated {} events associated with production company ID {}",
                        companyEvents.size(), productionCompanyId);
            }

            productionCompanyRepo.delete(String.valueOf(productionCompanyId));
            logger.info(
                    "AdminManagementService.deleteProductionCompany: Successfully closed production company with ID {}",
                    productionCompanyId);

            return Result
                    .makeOk("Production company with ID " + productionCompanyId + " has been closed successfully.");
        } catch (IllegalArgumentException e) {
            logger.error("AdminManagementService.deleteProductionCompany: Production company with ID {} not found",
                    productionCompanyId, e);
            return Result.makeFail("Production company with ID " + productionCompanyId + " not found");
        } catch (Exception e) {
            logger.error(
                    "AdminManagementService.deleteProductionCompany: Error occurred while deleting production company with ID {}",
                    productionCompanyId, e);
            return Result.makeFail("Error occurred while deleting production company with ID " + productionCompanyId);
        }
    }

    public Result<String> removeUser(String userID, String sessionToken) {
        try {
            logger.info("Attempting to remove the user subscription of user ID {}", userID);
            if (sessionToken == null || sessionToken.trim().isEmpty()) {
                logger.warn("AdminManagementService.removeUser: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.validateToken(sessionToken)) {
                logger.error("AdminManagementService.removeUser: Invalid token");
                return Result.makeFail("Invalid token");
            }

            if (!authenticationService.isAdminToken(sessionToken)) {
                logger.error("AdminManagementService.removeUser: Must be in an active admive session to remove user");
                return Result.makeFail("Unauthorized access");
            }

            User user = userRepository.findByID(userID);

            List<Integer> companyIDs = productionCompanyRepo.getAllUserComapnies(user);
            for (Integer companyID : companyIDs) {
                boolean success = false;

                while (!success) {
                    try {
                        ProductionCompany company = productionCompanyRepo.findByID(String.valueOf(companyID));
                        if (company.isFounder(userID)) {
                            deleteProductionCompany(companyID, sessionToken);
                            // company no longer exists after closure
                            success = true;
                            logger.info(
                                    "AdminManagementService.removeUser: User {} was founder of company {}. Closed company as part of user removal process.",
                                    userID, companyID);
                            continue;
                        }
                        company.adminRemoveUser(userID);
                        productionCompanyRepo.save(company);
                        success = true;
                        logger.info(
                                "AdminManagementService.removeUser: Successfully removed user {} from company {} as part of user removal process.",
                                userID, companyID);
                    } catch (IllegalArgumentException e) {
                        logger.warn("AdminManagementService.removeUser: Company {} not found while removing user {}",
                                companyID, userID);
                        // stop retrying, company is gone
                        success = true;
                    } catch (OptimisticLockingFailureException e) {
                        logger.warn("Optimistic lock conflict while removing user {} from company {}. Retrying...",
                                userID, companyID);
                    }
                }
            }
            userRepository.delete(userID);
            logger.info("AdminManagementService.removeUser: Successfully removed user with ID {}", userID);

            return Result.makeOk("User with ID: " + userID + ", , has been removed");
        } catch (IllegalArgumentException e) {
            logger.error("AdminManagementService.removeUser: User with ID {} not found", userID, e);
            return Result.makeFail("User with ID " + userID + " not found");
        } catch (Exception e) {
            logger.error("AdminManagementService.removeUser: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while saving the layout.");
        }
    }

    private void deactivateEventsNoRefund(List<Event> events) {
        logger.info("AdminManagementService.deactivateEventsNoRefund: Deactivating {} events", events.size());
        for (Event e : events) {
            try {
                e.deactivateEvent();
                eventRepo.save(e);
                logger.info("AdminManagementService.deactivateEventsNoRefund: Deactivated event with ID {}",
                        e.getEventID());
            } catch (IllegalStateException ex) {
                logger.info(
                        "AdminManagementService.deactivateEventsNoRefund: Skipping deactivation for event {} (It has likely already passed)",
                        e.getEventID());
            } catch (Exception ex) {
                logger.error("AdminManagementService.deactivateEventsNoRefund: Unexpected error deactivating event {}",
                        e.getEventID(), ex);
            }
        }
    }

    private void deactivateEvents(List<Event> events) {
        logger.info("AdminManagementService.deactivateEvents: Deactivating {} events", events.size());
        for (Event e : events) {
            deactivateEventAndRefundOrders(e.getEventID());
            logger.info("AdminManagementService.deactivateEvents: Deactivated event with ID {}", e.getEventID());
        }
    }

    private void deactivateEventAndRefundOrders(int eventID) {
        logger.info("AdminManagementService.deactivateEventAndRefundUser: Deactivating event {}", eventID);
        try {
            Event event = null;
            while (true) {
                try {
                    event = eventRepo.findByID(String.valueOf(eventID));
                    event.deactivateEvent();
                    eventRepo.save(event);
                    break;
                } catch (OptimisticLockingFailureException e) {
                    logger.warn(
                            "AdminManagementService.deactivateEventAndRefundUser: optimistic lock exception when saving event {}",
                            eventID);
                    continue;

                } catch (IllegalArgumentException e) {
                    logger.warn("AdminManagementService.deactivateEventAndRefundUser: IllegalArgumentException: {}",
                            e.getMessage());
                    return;
                } catch (IllegalStateException e) {
                    logger.warn("AdminManagementService.deactivateEventAndRefundUser: IllegalStateException: {}",
                            e.getMessage());
                    break;
                }
            }

            logger.info("AdminManagementService.deactivateEventAndRefundUser: Deactivated event {}", eventID);
            if (!event.getEventStartTime().isAfter(LocalDateTime.now())) {
                logger.info(
                        "AdminManagementService.deactivateEventAndRefundUser: event {} has already passed, no need for refunds",
                        eventID);
                return;
            }
            List<Order> orders = orderRepo.getByEventId(eventID);
            List<RefundResult> refundResults = new ArrayList<>();
            int successCount = 0, failCount = 0;
            for (Order order : orders) {
                if (cancelOrder(order.getOrderId())) {
                    RefundResult res = refundOrder(order.getOrderId());
                    if (res.status() == RefundStatus.SUCCESS)
                        successCount++;
                    else
                        failCount++;
                    refundResults.add(res);
                }
            }

            logger.info(
                    "AdminManagementService.deactivateEventAndRefundUser: finished refunding, successes: {}, failures: {}.",
                    successCount, failCount);
            if (failCount != 0)
                logger.warn(
                        "AdminManagementService.deactivateEventAndRefundUser: SOME REFUNDS FAILED FOR EVENT {}, manuall reconsaliation is needed! Also don't forget to brushyour teeth!",
                        eventID);

        } catch (Exception e) {
            logger.error("AdminManagementService.deactivateEventAndRefundUser: An unexpected exception: ", e);
        }
    }

    // cancel the order and return whether a refund is needed or not
    private boolean cancelOrder(String orderID) {
        logger.info("AdminManagementService.cancelOrder: canceling order: {}", orderID);
        while (true) {
            try {
                Order order = orderRepo.findByID(orderID);
                boolean toRefund = false;

                if (order.isActive()) { // only cancel
                    order.CancelOrder();
                } else if (order.isCompleted()) { // refund
                    order.CancelOrder();
                    toRefund = true;
                } else { // already canceled
                    logger.info("AdminManagementService.cancelOrder: order {} was already canceled", orderID);
                    return false;
                }
                orderRepo.save(order);
                return toRefund;

            } catch (OptimisticLockingFailureException e) {
                logger.warn("AdminManagementService.cancelOrder: concurency issue while canceling order: {}", orderID);
                continue;
            } catch (IllegalArgumentException e) {
                logger.warn("AdminManagementService.cancelOrder: IllegalArgumentException: {}", e.getMessage());
                return false;
            }
        }
    }

    private RefundResult refundOrder(String orderID) {
        logger.info("AdminManagementService.refundOrder: refunding order: {}", orderID);
        try {
            Order order = orderRepo.findByID(orderID);
            paymentService.cancelPayment(order.getTransactionId());
            ticketService.revokeTicket(order.getExternalTicket());
            return new RefundResult(orderID, RefundStatus.SUCCESS, null);
        } catch (IllegalArgumentException e) {
            logger.warn("AdminManagementService.refundOrder: IllegalArgumentException: {}", e.getMessage());
            return new RefundResult(orderID, RefundStatus.DATA_INTEGRITY_ERROR, e.getMessage());
        } catch (RefundFailedException e) {
            logger.warn("AdminManagementService.refundOrder: RefundFailedException: {}", e.getMessage());
            return new RefundResult(orderID, RefundStatus.REFUND_FAIL, e.getMessage());
        } catch (RevokeTicketFailureException e) {
            logger.warn("AdminManagementService.refundOrder: RevokeTicketFailureException: {}", e.getMessage());
            return new RefundResult(orderID, RefundStatus.TICKET_FAIL, e.getMessage());
        } catch (RefundStatusUnknownException e) {
            logger.error("AdminManagementService.refundOrder: RefundStatusUnknownException: manual review requiered: ",
                    e);
            return new RefundResult(orderID, RefundStatus.REFUND_FAIL, e.getMessage());
        } catch (TicketRevokeUnknownStatusException e) {
            logger.error(
                    "AdminManagementService.refundOrder: TicketRevokeUnknownStatusException: manual review requiered: ",
                    e);
            return new RefundResult(orderID, RefundStatus.TICKET_FAIL, e.getMessage());
        } catch (Exception e) {
            logger.error("AdminManagementService.refundOrder: Unexpected Exception while refunding order: {}. error: ",
                    orderID, e);
            return new RefundResult(orderID, RefundStatus.UNKNOWN, e.getMessage());
        }
    }

    public Result<String> registerNewAdmin(String sToken, String newAdminUsername, String newAdminPassword,
            String newAdminEmail) {
        try {
            logger.info(
                    "AdminManagementService.registerNewAdmin: Attempting to register new system admin with username {}",
                    newAdminUsername);
            if (!authenticationService.validateToken(sToken)) {
                logger.warn("AdminManagementService.registerNewAdmin: Invalid token");
                return Result.makeFail("Invalid token");
            }
            if (!authenticationService.isAdminToken(sToken)) {
                logger.warn(
                        "AdminManagementService.registerNewAdmin: Must be in an active admin session to register new admin");
                return Result.makeFail("Unauthorized access");
            }

            SystemAdmin newAdmin = new SystemAdmin(newAdminUsername, newAdminPassword, newAdminEmail);
            systemAdminRepo.save(newAdmin);
            logger.info(
                    "AdminManagementService.registerNewAdmin: Successfully registered new system admin with username {}",
                    newAdminUsername);
            return Result
                    .makeOk("System admin with username: " + newAdminUsername + ", has been registered successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("AdminManagementService.registerNewAdmin: Invalid input - {}", e.getMessage());
            return Result.makeFail("Failed to register new admin: " + e.getMessage());
        } catch (OptimisticLockingFailureException e) {// if happens, then admin already created
            logger.warn(
                    "AdminManagementService.registerNewAdmin: Optimistic lock conflict while registering new admin with username {}.",
                    newAdminUsername);
            return Result.makeFail("Admin with username " + newAdminUsername + " already exists");
        } catch (Exception e) {
            logger.error("AdminManagementService.registerNewAdmin: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while registering the new admin.");
        }

    }

    public Result<List<ProductionCompanyDTO>> getAllProductionCompanies(String sToken) {
        try {
            logger.info(
                    "AdminManagementService.getAllProductionCompanies: Attempting to retrieve all production companies");
            verifyAdminToken(sToken);
            List<ProductionCompany> companies = productionCompanyRepo.getAll();
            List<ProductionCompanyDTO> companyDTOs = companies.stream()
                    .map(company -> new ProductionCompanyDTO(company))
                    .collect(Collectors.toList());
            logger.info(
                    "AdminManagementService.getAllProductionCompanies: Successfully retrieved {} production companies",
                    companyDTOs.size());
            return Result.makeOk(companyDTOs);
        } catch (IllegalArgumentException e) {
            logger.warn("AdminManagementService.getAllProductionCompanies: IllegalArgumentException: {}",
                    e.getLocalizedMessage());
            return Result.makeFail(e.getMessage());
        } catch (AuthException e) {
            logger.warn("AdminManagementService.getAllProductionCompanies: Authentication error: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("AdminManagementService.getAllProductionCompanies: unexpected error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while retrieving production companies.");
        }
    }

    public Result<List<UserDTO>> getAllUsers(String sToken) {
        try {
            logger.info("AdminManagementService.getAllUsers: Attempting to retrieve all users");
            verifyAdminToken(sToken);

            List<User> users = userRepository.getAll();
            List<UserDTO> userDTOs = users.stream()
                    .map(user -> new UserDTO(user))
                    .collect(Collectors.toList());

            logger.info("AdminManagementService.getAllUsers: Successfully retrieved {} users", userDTOs.size());
            return Result.makeOk(userDTOs);
        } catch (IllegalArgumentException e) {
            logger.warn("AdminManagementService.getAllUsers: IllegalArgumentException: {}", e.getLocalizedMessage());
            return Result.makeFail(e.getMessage());
        } catch (AuthException e) {
            logger.warn("AdminManagementService.getAllUsers: Authentication error: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("AdminManagementService.getAllUsers: unexpected error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while retrieving users.");
        }
    }

    private void verifyAdminToken(String sToken) throws IllegalArgumentException {
        if (!authenticationService.validateToken(sToken)) {
            logger.warn("AdminManagementService.verifyAdminToken: Invalid token");
            throw new AuthException("Invalid token");
        }
        if (!authenticationService.isAdminToken(sToken)) {
            logger.warn("AdminManagementService.verifyAdminToken: Unauthorized access attempt by non-admin user");
            throw new AuthException("Unauthorized access");
        }
        systemAdminRepo.findByID(authenticationService.extractSubjectFromToken(sToken)); // validate admin exists,
                                                                                         // throws error if not
    }

    private List<Order> getCompletedOrdersByEventIDs(Set<Integer> eventIDs) {// needed function in this context aswell
        return orderRepo.getAll().stream()
                .filter(Order::isCompleted)
                .filter(order -> eventIDs.contains(order.getEventId()))
                .toList();
    }
}
