package com.group16b.ApplicationLayer;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.ProductionCompanyDTO;
import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Venue;

import io.jsonwebtoken.JwtException;

@Service
public class UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final IRepository<Order> orderRepo;
	private final IRepository<Venue> venueRepo;
	private final IEventRepository eventRepo;
    private final IProductionCompanyRepository productionCompanyRepository;
	private final IRepository<User> userRepo;
	private final ITicketGateway ticketGateway;

	private final IAuthenticationService authenticationService;

	public UserService(IAuthenticationService authenticationService, ITicketGateway ticketGateway,
			IRepository<Venue> venueRepo, IRepository<User> userRepo, IRepository<Order> orderRepo,
			IEventRepository eventRepo, IProductionCompanyRepository productionCompanyRepository) {
		this.authenticationService = authenticationService;
		this.ticketGateway = ticketGateway;
		this.venueRepo = venueRepo;
		this.userRepo = userRepo;
		this.orderRepo = orderRepo;
		this.eventRepo = eventRepo;
		this.productionCompanyRepository = productionCompanyRepository;

	}

	public Result<UserDTO> registerUser(String email, String password) {
        try {
            logger.info("UserService.regeisterUser: Attempting to create new User with email: " + email);
            try {
                userRepo.findByID(email);
                
                // if the line above DOES NOT throw an error, it means the user exists and we must fail.
                logger.warn("UserService.regeisterUser: Failed to register user due to already existing user with email: " + email);
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
            logger.warn("UserService.regeisterUser: Failed to register user due to invalid domain arguments: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("UserService.regeisterUser: Unexpected system error during user registration: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }


	public Result<Boolean> updateUserPassword(String sessionToken, String oldPassword, String newPassword) {
		try {
            logger.info("UserService.updateUserPassword: Extracting token subject and fetching User aggregate.");
            
            if (!authenticationService.isUserToken(sessionToken)) {
                logger.warn("UserService.updateUserPassword: update failed: not a session of user");
                return Result.makeFail("Invalid token for user update password");
            }

			//test if token is valid and if there is a user with id from token in one line
            User user = userRepo.findByID(authenticationService.extractSubjectFromToken(sessionToken));
            
            logger.info("UserService.updateUserPassword: Delegating password change logic to User domain object.");
            
			//here will be all of the errorrs regarding where and how a paswword could change
            user.changePassword(oldPassword, newPassword);
            
            userRepo.save(user);
            logger.info("UserService.updateUserPassword: Password changed successfully.");
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.warn("UserService.updateUserPassword: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (JwtException e) {
			logger.error("UserService.updateUserPassword: JWT authentication error during user password update: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("UserService.updateUserPassword: Unexpected error during user password update: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}


    public Result<List<OrderDTO>> getUserOrderHistory(String sessionToken) {
        try {
            logger.info("UserService.getUserOrderHistory: Extracting token subject and fetching User aggregate.");
            if (sessionToken == null || sessionToken.isEmpty()) {
                logger.warn("UserService.getUserOrderHistory: Failed to fetch order history due to missing session token.");
                return Result.makeFail("Session token is required for fetching user order history");
            }
            if (!authenticationService.isUserToken(sessionToken)) {
                logger.warn("UserService.getUserOrderHistory: Failed to fetch order history due to invalid user token.");
                return Result.makeFail("Invalid token for fetching user order history");
            }
            String userId = authenticationService.extractSubjectFromToken(sessionToken);            
            logger.info("UserService.getUserOrderHistory: Successfully fetched user. Retrieving order history.");
            
            List<Order> orders = orderRepo.getAll();
            orders = orders.stream()
                    .filter(order -> order.isBelongsToSubject(userId))
                    .filter(Order::isCompleted)
                    .collect(Collectors.toList());
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(order -> new OrderDTO(order))
                    .collect(Collectors.toList());

            logger.info("UserService.getUserOrderHistory: Successfully retrieved order history.");
            return Result.makeOk(orderDTOs);        
        } catch (IllegalArgumentException e) {
            logger.warn("UserService.getUserOrderHistory: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail(e.getMessage());    
        } catch (AuthException e) {
            logger.warn("UserService.getUserOrderHistory: Authentication failed during order history fetch: " + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (JwtException e) {
            logger.error("UserService.getUserOrderHistory: JWT authentication error during user order history fetch: " + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("UserService.getUserOrderHistory: Unexpected error during user order history fetch: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<List<ProductionCompanyDTO>> getAllUserCompanies(String sessionToken) {
        try {
            logger.info("UserService.getAllUserCompanies: Extracting token subject and fetching User aggregate.");
            String userId = validateAndGetUserID(sessionToken);       
            logger.info("UserService.getAllUserCompanies: Successfully fetched user. Retrieving companies.");
            
            List<ProductionCompanyDTO> companies = productionCompanyRepository.getAll().stream()
                    .filter(company -> company.isManager(userId))
                    .map(company -> new ProductionCompanyDTO(company))
                    .collect(Collectors.toList());
            
            logger.info("UserService.getAllUserCompanies: Successfully retrieved companies.");
            return Result.makeOk(companies);        
        } catch (IllegalArgumentException e) {
            logger.warn("UserService.getAllUserCompanies: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail(e.getMessage());    
        } catch (AuthException e) {
            logger.warn("UserService.getAllUserCompanies: Authentication failed during companies fetch: " + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (JwtException e) {
            logger.error("UserService.getAllUserCompanies: JWT authentication error during user companies fetch: " + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("UserService.getAllUserCompanies: Unexpected error during user companies fetch: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
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