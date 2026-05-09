package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Order.OrderRepository;
import com.group16b.DomainLayer.Order.Ticket;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	private final IUserRepository userRepository = IUserRepository.getInstance();
	private final OrderRepository orderRepo = OrderRepository.getInstance();



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
	public Result<List<TicketDTO>> CompleteActiveOrder(String userId, String orderID){
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
			if (!order.isBelongsToUser(userId)) {
				logger.error("UserService.CompleteActiveOrder: Order {} does not belong to user {}", orderID, userId);
				return Result.makeFail("Order does not belong to the given user");
			}

			// 2. System - calculates price of tickets according to company and event policies.
			double price = order.getSumOrderprice(); // @TODO: implement price calculation logic


			// 3. System - charges the user for the designed price.
			logger.info("UserService.CompleteActiveOrder: user {} is attempting to pay {} for order {}", userId, price, orderID);
			User user = userRepository.getUserByID(Integer.parseInt(userId));
			if (user == null) {
				logger.error("UserService.CompleteActiveOrder: User {} not found while attempting to complete order {}", userId, orderID);
				return Result.makeFail("User not found");
			}

			order.CompleteOrder();
				logger.info("UserService.CompleteActiveOrder: Order {} completed successfully for user {}", orderID, userId);

			PaymentService paymentService = new PaymentService();
			if (!paymentService.processPayment(order.getPaymentInfo(), price)) {
				return Result.makeFail("Payment failed");
			}
			logger.info("UserService.CompleteActiveOrder: user {} paid {} successfully for order {}", userId, price, orderID);

			// 4. System - creates Tickets for each of the tickets.
				
				List<TicketDTO> tikketDTOs = new ArrayList<>();
				for (Ticket ticket : order.getTickets()) {
					TicketDTO ticketDTO = new TicketDTO(ticket);
					tikketDTOs.add(ticketDTO);
				}
			// 5. System - sends the user his acquired tickets.
				return Result.makeOk(tikketDTOs);


		} catch (Exception e) {
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
		
	}

}