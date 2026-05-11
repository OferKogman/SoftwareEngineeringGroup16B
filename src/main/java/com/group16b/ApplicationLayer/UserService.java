package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.UserDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.TicketGateway;

import io.jsonwebtoken.JwtException;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final IOrderRepository orderRepo = OrderRepositoryMapImpl.getInstance();
	private final IVenueRepository venueRepo = VenueRepositoryMapImpl.getInstance();
	private final IEventRepository eventRepo = EventRepositoryMapImpl.getInstance();
	private final ITicketGateway ticketGateway = new TicketGateway();

	private final IAuthenticationService authenticationService;
	private final IUserRepository userRepository;

	

	public UserService(IAuthenticationService authenticationService, IUserRepository userRepository) {
		this.authenticationService = authenticationService;
		this.userRepository = userRepository;
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

	
}