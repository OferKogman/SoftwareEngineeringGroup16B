package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Venue;

import io.jsonwebtoken.JwtException;

public class UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final IRepository<Order> orderRepo;
	private final IRepository<Venue> venueRepo;
	private final IEventRepository eventRepo;
	private final IRepository<User> userRepo;
	private final ITicketGateway ticketGateway;

	private final IAuthenticationService authenticationService;

	public UserService(IAuthenticationService authenticationService, ITicketGateway ticketGateway,
			IRepository<Venue> venueRepo, IRepository<User> userRepo, IRepository<Order> orderRepo,
			IEventRepository eventRepo) {
		this.authenticationService = authenticationService;
		this.ticketGateway = ticketGateway;
		this.venueRepo = venueRepo;
		this.userRepo = userRepo;
		this.orderRepo = orderRepo;
		this.eventRepo = eventRepo;

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
            
        } catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

}