package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.InfrastructureLayer.PaymentService;
import com.group16b.InfrastructureLayer.TicketGateway;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImp;

import io.jsonwebtoken.JwtException;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	private final OrderRepositoryMapImpl orderRepo = OrderRepositoryMapImpl.getInstance();
	private final IVenueRepository venueRepo = VenueRepositoryMapImp.getInstance();
	private final IEventRepository eventRepo = EventRepositoryMapImpl.getInstance();
	private final ITicketGateway ticketGateway = new TicketGateway();

	private final IAuthenticationService authenticationService;
	private final IUserRepository userRepository;

	private final CompanyHierarchyDomainService companyHierarchyDomainService;

	private final ConcurrentHashMap<Integer, Object> companyLocks = new ConcurrentHashMap<>();

	public UserService(IAuthenticationService authenticationService, IUserRepository userRepository, CompanyHierarchyDomainService companyHierarchyDomainService) {
		this.authenticationService = authenticationService;
		this.userRepository = userRepository;
		this.companyHierarchyDomainService=companyHierarchyDomainService;
	}

	public Result<UserDTO> registerUser(String email, String password) {
		logger.info("Creating new User with email: " + email);
		User newUser = new User(email, password);
		userRepository.addUser(newUser);
		return Result.makeOk(new UserDTO(newUser));
	}

	public Result<Boolean> updateUserPassword(String sessionToken, String oldPassword, String newPassword) {
		try {
			logger.info("Verifying session token for event deactivation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event deactivation.");
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
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
			try{
			user.changePassword(oldPassword, newPassword);
			userRepository.updateUser(user);
			logger.info("Password changed successfully");
			return Result.makeOk(true);
			}
			catch (IllegalArgumentException e) {
				logger.error("Failed to change password: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> deleteUser(String sessionToken) {
		try {
			logger.info("Verifying session token for event deactivation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event deactivation.");
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
			logger.info("Session token verified successfully.");
			return Result.makeOk(true);
		}
		catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
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
				TicketDTO ticketDTO = ticketGateway.generateTicket(order.getEventId(), String.valueOf(userId), order.getSegmentId(), order.getSeats().get(i), price); // @TODO: implement actual ticket generation logic
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

	public Result<Boolean> assignOwnerToCompany(int companyID, int targetID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for Owner assignment of user {0} to company {1}.", targetID, companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for Owner assignment of user {0} to company {1}.", targetID, companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
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
				//add invite to target user
				logger.info("Adding owner assignment invite to target user.");
				targetUser.addInvite(companyID, userID, new Owner(userID));
			}
			catch (IllegalArgumentException e) {
				logger.error("Failed to add owner assignment invite: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
			finally {
				targetUser.getUserInvitesLock().unlock();
			}
			logger.info("user {0} have been succesfully invited to be an owner in company {1} by user {2}",targetID,companyID,userID);
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to invite owner: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to invite owner: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during owner invitation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during owner invitation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	
	public Result<Boolean> assignManagerToCompany(int companyID, int targetID, Set<ManagerPermissions> permissions, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for Manager assignment of user {0} to company {1}.", targetID, companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for Manager assignment of user {0} to company {1}.", targetID, companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			logger.info("Session token verified successfully.");

			//get perms
			logger.info("Validating user permissions for manager assignment.");
			user.validatePermissions(companyID, Owner.class);
			logger.info("User permissions validated successfully.");

			//get target user
			logger.info("retrieving target user for Manager assignment.");
			User targetUser = userRepository.getUserByID(targetID);
			if (targetUser==null) {
				logger.warn("Target user with ID {0} not found for Manager assignment.", targetID);
				return Result.makeFail("Target user not found.");
			}

			//send invite
			logger.info("ensuring target isnt already an owner for company.");
			targetUser.getUserInvitesLock().lock();
			try {
				logger.info("Adding manager assignment invite to target user.");
				targetUser.addInvite(companyID, userID, new Manager(userID, permissions));
			}
			catch (IllegalArgumentException e) {
				logger.error("Failed to add manager assignment invite: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
			finally {
				targetUser.getUserInvitesLock().unlock();
			}
			logger.info("user {0} have been succesfully invited to be a manager in company {1} by user {2}",targetID,companyID,userID);
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to invite manager: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to invite manager: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during inviting manager: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during inviting manager: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Boolean> acceptInviteToCompany(int companyID, int assignerID, String sessionToken) {
		Object companyLock = getCompanyLock(companyID);
		try {
			//auth
			logger.info("Verifying session token for accepting invite assignment to company {0} by assigner {2}.", companyID, assignerID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for accepting invite assignment to company {0} by assigner {1}.", companyID, assignerID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			if(user==null)
			{
				logger.warn("user {0} was not found, maybe deleted",userID);
				return Result.makeFail("user not found");
			}
			logger.info("Session token verified successfully.");

			User assigner = userRepository.getUserByID(assignerID);
			if (assigner == null) {
				logger.warn("Assigner user with ID {0} not found for accepting invite assignment to company {1} by user {2}.", assignerID, companyID, userID);
				return Result.makeFail("Assigner user not found.");
			}
			synchronized(companyLock)
			{
				if(!assigner.isOwnerOfCompany(companyID))
				{
					logger.warn("Assigner user with ID {0} does not have permission to assign roles for company {1} for accepting invite assignment to company {1} by user {2}.", assignerID, companyID, userID);
					return Result.makeFail("Assigner user does not have permission to assign roles for this company.");
				}
				
				//check that invite exists and accept it
				logger.info("accepting invite assignment invite for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
				user.getUserInvitesLock().lock();
				try {
					user.acceptInvite(companyID, assignerID);
					assigner.addAssignee(companyID, (Manager) user.getRole(companyID));
					logger.info("Invite assignment invite accepted successfully for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
				} 
				catch (IllegalArgumentException e) {
					logger.error("Failed to accept invite: " + e.getMessage());
					user.removeRole(companyID);//just in case
					return Result.makeFail(e.getMessage());
				}
				finally {
					user.getUserInvitesLock().unlock();
				}
			}
			logger.info("user {0} have succesfully accepted an invite to company {1} by user {2}",userID,companyID,assignerID);
			return Result.makeOk(true);
		} catch (IllegalArgumentException e) {
			logger.error("Failed to accepting invite: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to accept invite: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during invite acceptance: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during accepting invite: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Boolean> rejectInviteToCompany( int companyID, int assignerID, String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for rejecting invite assignment to company {0} by assigner {1}.", companyID, assignerID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for rejecting invite assignment to company {0} by assigner {1}.", companyID, assignerID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			logger.info("Session token verified successfully.");
			if(!userRepository.userExists(assignerID))
			{
				logger.warn("Assigner user with ID {0} not found for rejecting invite assignment to company {1} by user {2}.", assignerID, companyID, userID);
				return Result.makeFail("Assigner user not found.");
			}

			//check that invite exists and reject it
			logger.info("rejecting invite assignment for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
			user.getUserInvitesLock().lock();
			try {
				user.rejectInvite(companyID, assignerID);
				logger.info("invite rejected successfully for company {0} by user {1} and assigner {2}.", companyID, userID, assignerID);
			} 
			catch (IllegalArgumentException e) {
				logger.error("Failed to reject invite: " + e.getMessage());
				return Result.makeFail(e.getMessage());
			}
			finally {
				user.getUserInvitesLock().unlock();
			}
			logger.info("user {0} have succesfully rejected an invite to company {1} by user {2}",userID,companyID,assignerID);
			return Result.makeOk(true);
		} catch (IllegalArgumentException e) {
			logger.error("Failed to reject invite: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to reject invite: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during invite rejection: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during invite rejection: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<List<OrderDTO>> getUserOrders(String sessionToken) {
		try {
			//auth
			logger.info("Verifying session token for retrieving orders of user with session token {0}.", sessionToken);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for retrieving orders of user with session token {0}.", sessionToken);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			if (user == null) {
				logger.warn("User with ID {0} not found for retrieving orders.", userID);
				return Result.makeFail("User not found.");
			}
			logger.info("Session token verified successfully.");

			//get orders
			logger.info("Retrieving orders for user {0}.", userID);
			List<Order> orders = orderRepo.getOrdersByUserID(userID);
			List<OrderDTO> orderDTOs = new ArrayList<>();
			for (Order order : orders) {
				OrderDTO orderDTO = new OrderDTO(order); 
				orderDTOs.add(orderDTO);
			}
			logger.info("Orders retrieved successfully for user {0}.", userID);
			return Result.makeOk(orderDTOs);
		} catch (IllegalArgumentException | IllegalStateException e) {
			logger.error("Failed to retrieve orders: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during retrieving orders: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during retrieving orders: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> forfeitOwnership(int companyID, String sessionToken) {
		Object lock = getCompanyLock(companyID);
		try {
			//auth
			logger.info("Verifying session token for forfeiten ownership for company {0}.", companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for forfeiten ownership for company {0}.", companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			if (user == null) {
				logger.warn("User with ID {0} not found for forfeiting ownership", userID);
				return Result.makeFail("User not found.");
			}
			logger.info("Session token verified successfully.");
			Integer assignerID;
			synchronized(lock)
			{
				if(!user.isOwnerOfCompany(companyID))
				{
					logger.warn("user {0} is not owner for comapny {1}, thus he cant forfeit his ownership there",userID,companyID);
					return Result.makeFail("user is not owner");
				}

				assignerID=user.getParentIDForCompany(companyID);
				if(assignerID==null)
				{
					logger.warn("user {0} is founder and thus can't leave the company {1}",userID, companyID);
					return Result.makeFail("founder cant leave company");
				}
				companyHierarchyDomainService.removeUserFromCompany(user, companyID);
			}
			logger.info("user {0} has succesfuly forfeited its role in company {1}, thus removing itself from the children of its company parent {2}",userID,companyID,assignerID);
			return Result.makeOk(true);

		} catch (IllegalArgumentException | IllegalStateException e) {
			logger.error("Failed to forfeit ownership: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during forfeitng ownership: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during forfeiting ownership: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Boolean> removeOwnerManager(int targetID, int companyID, String sessionToken) {
		Object lock = getCompanyLock(companyID);
		try {
			//auth
			logger.info("Verifying session token for removing manager with id {0} for company {1}.", targetID,companyID);
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for removing manager with id {0} for company {1}.", targetID,companyID);
				return Result.makeFail("Invalid session token.");
			}
			int userID=Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken));
			User user = userRepository.getUserByID(userID);
			if (user == null) {
				logger.warn("User with ID {0} not found for removing manager", userID);
				return Result.makeFail("User not found.");
			}
			logger.info("Session token verified successfully.");
			logger.info("retrieving target user {0} to remove manager from company {1} by user {2}",targetID,companyID,userID);
			User target=userRepository.getUserByID(targetID);
			if(target==null)
			{
				logger.warn("target user {0} was not found to remove him from the compny {1} by user {2}.",targetID,companyID,userID);
				return Result.makeFail("target user was not found");
			}
			synchronized(lock)
			{
				if(!user.isOwnerOfCompany(companyID))
				{
					logger.warn("user {0} is not owner for comapny {1}, thus he cant remove manager there",userID,companyID);
					return Result.makeFail("user is not owner");
				}

				if(target.getRole(companyID)==null)
				{
					logger.warn("target user {0} is not personal in the company {1} to remove them by user {2}",targetID,companyID,userID);
					return Result.makeFail("target user is not personal in company");
				}

				if(!companyHierarchyDomainService.isManagerUnderOwnerTreeTraversal(target, user, companyID))
				{
					logger.warn("User {0} isn't above target {1} in the hierarchy tree in company {2}, thus he cant remove them",userID,targetID,companyID);
					return Result.makeFail("User didn't apoint target so no permission to remove");
				}
				companyHierarchyDomainService.removeUserFromCompany(target, companyID);
			}
			logger.info("target {0} was removed from company {1} by user {2}",targetID,companyID,userID);
			return Result.makeOk(true);


		} catch (IllegalArgumentException | IllegalStateException e) {
			logger.error("Failed to remove manager from company: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during forfeitng ownership: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during forfeiting ownership: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}


	private Object getCompanyLock(int companyID) {
		return companyLocks.computeIfAbsent(companyID, id -> new Object());
	}
}