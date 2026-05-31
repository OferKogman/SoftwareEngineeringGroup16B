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
		logger.info("Creating new User with email: " + email);
		User newUser = new User(email, password);
		userRepo.save(newUser);
		return Result.makeOk(new UserDTO(newUser));
	}

	public Result<Boolean> updateUserPassword(String sessionToken, String oldPassword, String newPassword) {
		try {
			logger.info("Verifying session token for event deactivation."); // TODO: change log message to be more
																			// specific to password update
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event deactivation.");
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepo.findByID(authenticationService.extractSubjectFromToken(sessionToken));
			logger.info("Session token verified successfully.");
			logger.info("Validating old password");
			if (!user.confirmPassword(oldPassword)) {
				logger.error("Old password is incorrect.");
				return Result.makeFail("Old password is incorrect.");
			}
			logger.info("Validating new password");
			if (!user.confirmPassword(newPassword)) {
				logger.error("New password cannot be the same as the old password.");
				return Result.makeFail("New password cannot be the same as the old password.");
			} // else, user is not null and old password is correct and new password is
				// different from old password
			try {
				user.changePassword(oldPassword, newPassword);
				userRepo.save(user);
				logger.info("Password changed successfully");
				return Result.makeOk(true);
			} catch (IllegalArgumentException e) {
				logger.error("Failed to change password: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
		} catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

}