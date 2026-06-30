package com.group16b.ApplicationLayer;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group16b.ApplicationLayer.DTOs.ActiveOrderDTO;
import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.ProductionCompanyDTO;
import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Exceptions.OrderExpiredException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Venue;

import io.jsonwebtoken.JwtException;

@Service
@Transactional
public class UserService {
    private static final Logger logger = getLogger(UserService.class);

    private final IOrderRepository orderRepo;
    private final IRepository<Venue> venueRepo;
    private final IEventRepository eventRepo;
    private final IProductionCompanyRepository productionCompanyRepository;
    private final IRepository<User> userRepo;
    private final ITicketGateway ticketGateway;

    private final IAuthenticationService authenticationService;

    public UserService(IAuthenticationService authenticationService, ITicketGateway ticketGateway,
            IRepository<Venue> venueRepo, IRepository<User> userRepo, IOrderRepository orderRepo,
            IEventRepository eventRepo, IProductionCompanyRepository productionCompanyRepository) {
        this.authenticationService = authenticationService;
        this.ticketGateway = ticketGateway;
        this.venueRepo = venueRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.eventRepo = eventRepo;
        this.productionCompanyRepository = productionCompanyRepository;

    }

    @Transactional
    public Result<UserDTO> registerUser(String email, String password) {
        try {
            logger.info("UserService.regeisterUser: Attempting to create new User with email: " + email);
            try {
                userRepo.findByID(email);

                // if the line above DOES NOT throw an error, it means the user exists and we
                // must fail.
                logger.warn(
                        "UserService.regeisterUser: Failed to register user due to already existing user with email: "
                                + email);
                return Result.makeFail("User already exists");

            } catch (Exception e) {
                // if it throws an error, the user wasn't found
                logger.info("UserService.regeisterUser: Confirmed that the user doesn't exist yet.");
            }
            User newUser = new User(email, password);
            userRepo.save(newUser);

            logger.info("UserService.regeisterUser: Successfully registered user with email: " + email);
            return Result.makeOk(new UserDTO(newUser));

        } catch (IllegalArgumentException e) {
            logger.warn("UserService.regeisterUser: Failed to register user due to invalid domain arguments: "
                    + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error(
                    "UserService.regeisterUser: Unexpected system error during user registration: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Boolean> updateUserPassword(String sessionToken, String oldPassword, String newPassword) {
        try {
            logger.info("UserService.updateUserPassword: Extracting token subject and fetching User aggregate.");

            if (!authenticationService.isUserToken(sessionToken)) {
                logger.warn("UserService.updateUserPassword: update failed: not a session of user");
                return Result.makeFail("Invalid token for user update password");
            }

            User user = userRepo.findByID(authenticationService.extractSubjectFromToken(sessionToken));

            logger.info("UserService.updateUserPassword: Delegating password change logic to User domain object.");

            // here will be all of the errorrs regarding where and how a paswword could
            // change
            user.changePassword(oldPassword, newPassword);

            userRepo.save(user);
            logger.info("UserService.updateUserPassword: Password changed successfully.");
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.warn("UserService.updateUserPassword: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (JwtException e) {
            logger.error("UserService.updateUserPassword: JWT authentication error during user password update: "
                    + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error(
                    "UserService.updateUserPassword: Unexpected error during user password update: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<List<OrderDTO>> getUserOrderHistory(String sessionToken) {
        try {
            logger.info("UserService.getUserOrderHistory: Extracting token subject and fetching User aggregate.");
            if (sessionToken == null || sessionToken.isEmpty()) {
                logger.warn(
                        "UserService.getUserOrderHistory: Failed to fetch order history due to missing session token.");
                return Result.makeFail("Session token is required for fetching user order history");
            }
            if (!authenticationService.isUserToken(sessionToken)) {
                logger.warn(
                        "UserService.getUserOrderHistory: Failed to fetch order history due to invalid user token.");
                return Result.makeFail("Invalid token for fetching user order history");
            }
            String userId = authenticationService.extractSubjectFromToken(sessionToken);
            logger.info("UserService.getUserOrderHistory: Successfully fetched user. Retrieving order history.");

            List<Order> orders = orderRepo.findByUserIdAndActiveFalse(userId); 
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(order -> new OrderDTO(order))
                    .collect(Collectors.toList());

            logger.info("UserService.getUserOrderHistory: Successfully retrieved order history.");
            return Result.makeOk(orderDTOs);
        } catch (IllegalArgumentException e) {
            logger.warn("UserService.getUserOrderHistory: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (AuthException e) {
            logger.warn("UserService.getUserOrderHistory: Authentication failed during order history fetch: "
                    + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (JwtException e) {
            logger.error("UserService.getUserOrderHistory: JWT authentication error during user order history fetch: "
                    + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("UserService.getUserOrderHistory: Unexpected error during user order history fetch: "
                    + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<ActiveOrderDTO> getUserActiveOrder(String sessionToken) {
        try {
            logger.info("UserService.getUserActiveOrder: Getting user's active order.");

            logger.info("UserService.getUserActiveOrder: Extracting token subject and fetching User aggregate.");
            String userId = validateAndGetUserID(sessionToken);

            Order activeOrder = orderRepo.findFirstByUserIdAndActiveTrue(userId);
                
            if (activeOrder == null) {
                return Result.makeFail("No active order found for user.");
            }
            return Result.makeOk(new ActiveOrderDTO(activeOrder));
        } catch (OrderExpiredException e) {
            logger.info("UserService.getUserActiveOrder: User's active order is expired: " + e.getMessage());
            return Result.makeFail("No active order found for user.");
        } catch (IllegalArgumentException e) {
            logger.warn("UserService.getUserActiveOrder: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail("IllegalArgumentException: " + e.getMessage());
        } catch (AuthException e) {
            logger.warn("UserService.getUserActiveOrder: Authentication failed during active order fetch: "
                    + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (JwtException e) {
            logger.error("UserService.getUserActiveOrder: JWT authentication error during user active order fetch: "
                    + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error(
                    "UserService.getUserActiveOrder: Unexpected error during active order fetch: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Result<List<ProductionCompanyDTO>> getAllUserCompanies(String sessionToken) {
        try {
            logger.info("UserService.getAllUserCompanies: Extracting token subject and fetching User aggregate.");
            String userId = validateAndGetUserID(sessionToken);
            logger.info("UserService.getAllUserCompanies: Successfully fetched user. Retrieving companies.");

            List<ProductionCompany> companies = productionCompanyRepository.findCompaniesManagedByUser(userId);

            List<ProductionCompanyDTO> dtos = companies.stream()
                    .map(ProductionCompanyDTO::new)
                    .collect(Collectors.toList());

            logger.info("UserService.getAllUserCompanies: Successfully retrieved companies.");
            return Result.makeOk(dtos);
        } catch (IllegalArgumentException e) {
            logger.warn("UserService.getAllUserCompanies: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (AuthException e) {
            logger.warn(
                    "UserService.getAllUserCompanies: Authentication failed during companies fetch: " + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (JwtException e) {
            logger.error("UserService.getAllUserCompanies: JWT authentication error during user companies fetch: "
                    + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error(
                    "UserService.getAllUserCompanies: Unexpected error during user companies fetch: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Boolean> isRole(String sessionToken, String role) {
        try {
            logger.info("UserService.isRole: checking if seeion {} is of Role: {}", sessionToken, role);
            if (!authenticationService.validateToken(sessionToken)) {
                logger.warn("UserService.isRole: Invalid Token");
                return Result.makeFail("Invalid Token");
            }
            if (!role.equals(authenticationService.extractRoleFromToken(sessionToken))) {
                logger.info("UserService.isRole: seeion {} is Not of Role: {}", sessionToken, role);
                return Result.makeOk(false);
            }
            logger.info("UserService.isRole: seeion {} is of Role: {}", sessionToken, role);
            return Result.makeOk(true);
        } catch (JwtException e) {
            logger.error("UserService.isRole: JWT authentication error during user companies fetch: " + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("UserService.isRole: unexpected error occured: ", e);
            return Result.makeFail("An unexpected error occured, pls try again later.");
        }
    }

    private String validateAndGetUserID(String sessionToken) {
        if (!authenticationService.validateToken(sessionToken)) {
            throw new AuthException("Invalid Token");
        }
        if (!authenticationService.isUserToken(sessionToken)) {
            throw new AuthException("Only users are allowed to perform operation");
        }
        String userID = authenticationService.extractSubjectFromToken(sessionToken);
        
        userRepo.findByID(userID);
        
        return userID;
    }

}