package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepositoryMapImpl;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Order.OrderRepository;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepositoryImp;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.Role;
import com.group16b.DomainLayer.User.Roles.UserRepositoryImpl;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	private final IUserRepository userRepository = IUserRepository.getInstance();
	private final OrderRepository orderRepo = OrderRepository.getInstance();
	private final IVenueRepositoryImp venueRepo = IVenueRepositoryImp.getInstance();
	private final IEventRepositoryMapImpl eventRepo = IEventRepositoryMapImpl.getInstance();

	private IUserRepository userRepository;
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	private final IAuthenticationService authenticationService;
  
	public UserService(IAuthenticationService authenticationService, IUserRepository userRepository) {
		this.userRepository = userRepository;
		this.authenticationService = authenticationService;
	}

	public void registerUser(String email, String password) {
		User newUser = new User(email, password);
		userRepository.addUser(newUser);
	}

	public void updateUserPassword(int userID, String oldPassword, String newPassword) {
		User user = userRepository.getUserByID(userID);
		if (user == null) {
			System.out.println("User not found.");
			return;
		}
		if (!user.confirmPassword(oldPassword)) {
			System.out.println("Old password is incorrect.");
			return;
		}
		if (!user.confirmPassword(newPassword)) {
			System.out.println("New password cannot be the same as the old password.");
			return;
		} // else, user is not null and old password is correct and new password is
			// different from old password
		user.setPassword(newPassword);
		userRepository.updateUser(user);

	}

	public boolean authenticateUser(int userID, String password) {
		User user = userRepository.getUserByID(userID);
		if (user != null) {
			return user.confirmPassword(password);
		}
		return false;
	}

	public void deleteUser(int userID) {
		userRepository.deleteUser(userID);
	}

	public boolean userExists(int userID) {
		return userRepository.userExists(userID);
	}

	// should be here? 
	public Result<List<TicketDTO>> CompleteActiveOrder(int userId, String orderID, String sTocken, PaymentInfo paymentInfo) {
		logger.info("UserService.CompleteActiveOrder: Attempting to complete order {} for user {}", orderID, userId);
		/*
			1. System - check active order status.
			1.5 System - verify order belungs to the user.
			2. System - calculates price of tickets according to company and event policies.
			3. System - charges the user for the designed price.
			4. System - creates Tickets for each of the tickets.
			5. System - sends the user his acquired tickets.
		*/
		try {
				// 1. System - check active order status.
			Order order = orderRepo.getOrder(orderID);
			if (order == null) {
				logger.error("UserService.CompleteActiveOrder: Order {} not found for user {}", orderID, userId);
				return Result.makeFail("Order not found");
			}
			if (!order.isActive()) {
				logger.error("UserService.CompleteActiveOrder: Order {} is not active for user {}", orderID, userId);
				return Result.makeFail("Order is not active");
			}

				// 1.5 System - verify order belungs to the user.
			
			if (!order.isBelongsToUser(sTocken)) {
				logger.error("UserService.CompleteActiveOrder: Order {} does not belong to user {}", orderID, userId);
				return Result.makeFail("Order does not belong to the given user");
			}

			// 2. System - calculates price of tickets according to company and event policies.
			double price = order.getSumOrderprice(); // @TODO: implement price calculation logic


			// 3. System - charges the user for the designed price.
			logger.info("UserService.CompleteActiveOrder: user {} is attempting to pay {} for order {}", userId, price, orderID);
			User user = userRepository.getUserByID(userId);
			if (user == null) {
				logger.error("UserService.CompleteActiveOrder: User {} not found while attempting to complete order {}", userId, orderID);
				return Result.makeFail("User not found");
			}

			
			PaymentService paymentService = new PaymentService();
			if (!paymentService.processPayment(paymentInfo, price)) {
				return Result.makeFail("Payment failed");
			}
			logger.info("UserService.CompleteActiveOrder: user {} paid {} successfully for order {}", userId, price, orderID);

			// 4. System - creates Tickets for each of the tickets.
			List<TicketDTO> tikketDTOs = new ArrayList<>();
			for (int i = 0; i < order.getNumOfTickets(); i++) {
				ITicketGateway ticketGateway = new ITicketGateway();
				TicketDTO ticketDTO = null; // @TODO: implement actual ticket generation logic
				tikketDTOs.add(ticketDTO);
			}
			order.CompleteOrder();
			logger.info("UserService.CompleteActiveOrder: Order {} completed successfully for user {}", orderID, userId);

			
			// 5. System - sends the user his acquired tickets.
				return Result.makeOk(tikketDTOs);


		} catch (IllegalStateException e) { 
			logger.error("UserService.CompleteActiveOrder: Failed to generate tickets for order {} for user {}: {}", orderID, userId, e.getMessage());
			cancelPayment(paymentInfo); // @TODO: implement payment cancellation logic
			cancelOrder(orderID); // @TODO: implement order cancellation logic
			return Result.makeFail(e.getMessage());
		}catch (Exception e) {
			cancelPayment(paymentInfo); // @TODO: implement payment cancellation logic
			cancelOrder(orderID); // @TODO: implement order cancellation logic
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}


		
	}
	private void cancelPayment(PaymentInfo paymentInfo) {} 

	private void cancelOrder(String orderID) {
		Order order = orderRepo.getOrder(orderID);
		if (order != null) {
			orderRepo.cancelOrder(orderID);
		}
		Event event = eventRepo.getEventByID(order.getEventId());
		if (event == null) {
			logger.error("UserService.cancelOrder: Event {} not found while attempting to cancel order {}", order.getEventId(), orderID);
			return;
		}

		Venue venue = venueRepo.getVenueByID(event.getEventVenueID());
		if (venue == null) {
			logger.error("UserService.cancelOrder: Venue {} not found while attempting to cancel order {}", event.getEventVenueID(), orderID);
			return;
		}
		Segment segment = venue.getSegmentByID(order.getSegmentId());
		if (segment == null) {
			logger.error("UserService.cancelOrder: Segment {} not found while attempting to cancel order {}", order.getSegmentId(), orderID);
			return;
		}

        switch (segment.getSegmentType()) {
            case "S" -> segment.cancelReservation(ReservationRequest.forSeats(order.getEventId(), order.getSeats(), order.getSegmentId()));
            case "F" -> segment.cancelReservation(ReservationRequest.forField(order.getEventId(), order.getNumOfTickets(), order.getSegmentId()));
            default -> logger.error("UserService.cancelOrder: Unknown segment type {} for segment {} while attempting to cancel order {}", segment.getSegmentType(), segment.getSegmentID(), orderID);
        }

	}

	public Result<Boolean> assignOwnerToCompany(int userID, int companyID, int targetID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for Owner assignment of user {0} to company {1} by user {2}.", targetID, companyID, userID);
			if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for Owner assignment of user {0} to company {1} by user {2}.", targetID, companyID, userID);
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
			logger.info("Session token verified successfully.");

			//get perms
			logger.info("Validating user permissions for owner assignment.");
			user.validatePermissions(companyID, Owner.class);
			logger.info("User permissions validated successfully.");

			//get target user
			logger.info("retrieving target user for Owner assignment.");
			User targetUser = userRepository.getUserByID(targetID);
			if (targetUser==null) {
				logger.warn("Target user with ID {0} not found for Owner assignment.", targetID);
				return Result.makeFail("Target user not found.");
			}

			//ensure not owner already
			logger.info("ensuring target isnt already an owner for company.");
			targetUser.getUserInvitesLock().lock();
			try {
				if (targetUser.getRole(companyID) != null && targetUser.getRole(companyID) instanceof Owner) {
					logger.warn("Target user with ID {0} already OWNER for company {1}.", targetID, companyID);
					return Result.makeFail("Target user already OWNER for this company.");
				}
				//add invite to target user
				logger.info("Adding owner assignment invite to target user.");
				targetUser.addInvite(companyID, userID, new Owner(userID));
			} finally {
				targetUser.getUserInvitesLock().unlock();
			}
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to deactivate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> acceptOwnerAssigmentToCompany(int userID, int companyID, int assignerID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for accepting owner assignment to company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
			if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for accepting owner assignment to company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
			logger.info("Session token verified successfully.");
			if(!userRepository.userExists(assignerID))
			{
				logger.warn("Assigner user with ID {0} not found for accepting owner assignment to company {1} by user {2}.", assignerID, companyID, userID);
				return Result.makeFail("Assigner user not found.");
			}

			//check that invite exists and accept it
			logger.info("accepting owner assignment invite for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
			user.getUserInvitesLock().lock();
			try {
				user.acceptOwnerInvite(companyID, assignerID);
				logger.info("Owner assignment invite accepted successfully for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
			} 
			catch (IllegalArgumentException e) {
				logger.error("Failed to find event: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
			finally {
				user.getUserInvitesLock().unlock();
			}

			return Result.makeOk(true);
		} catch (IllegalArgumentException e) {
			logger.error("Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to deactivate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
}