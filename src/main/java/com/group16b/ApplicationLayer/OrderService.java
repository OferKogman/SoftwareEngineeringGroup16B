package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Order.OrderType;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.PaymentService;
import com.group16b.InfrastructureLayer.TicketGateway;

import io.jsonwebtoken.JwtException;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
	private final IOrderRepository orderRepo = OrderRepositoryMapImpl.getInstance();
	private final IVenueRepository venueRepo = VenueRepositoryMapImpl.getInstance();
	private final IEventRepository eventRepo = EventRepositoryMapImpl.getInstance();
    private final ITicketGateway ticketGateway = new TicketGateway();
	private final IAuthenticationService authenticationService;
	private final IUserRepository userRepository;

    public OrderService(IAuthenticationService authenticationService, IUserRepository userRepository) {
		this.authenticationService = authenticationService;
		this.userRepository = userRepository;
	}

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

            logger.info("Verifying session token for completion.");
			if (!authenticationService.validateToken(sTocken)) {
				logger.warn("Invalid session token provided for completion.");
				return Result.makeFail("Invalid session token.");
			}
            if (authenticationService.extractRoleFromToken(sTocken) == "Admin") {
				logger.warn("Invalid session token provided for completion.");
				return Result.makeFail("Invalid session token.");
			}
			String subjectID = authenticationService.extractSubjectFromToken(sTocken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);

				// 1.5 System - verify order belungs to the user.
			
			if (!order.isBelongsToSubject(subjectID)) {
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
			_cancelOrder(orderID); // @TODO: implement order cancellation logic
			return Result.makeFail(e.getMessage());
		}catch (Exception e) {
			cancelPayment(paymentInfo); // @TODO: implement payment cancellation logic
			_cancelOrder(orderID); // @TODO: implement order cancellation logic
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}

	}
    
	private void cancelPayment(PaymentInfo paymentInfo) {} 

	private void _cancelOrder(String orderID) {
		Order order = orderRepo.getOrder(orderID);
		if (order == null) {
			logger.error("UserService._cancelOrder: Order {} not found while attempting to cancel order {}", orderID, orderID);
			return;
		}
		orderRepo.cancelOrder(orderID);
		Event event = eventRepo.getEventByID(order.getEventId());
		if (event == null) {
			logger.error("UserService._cancelOrder: Event {} not found while attempting to cancel order {}", order.getEventId(), orderID);
			return;
		}

		Venue venue = venueRepo.getVenueByID(event.getEventVenueID());
		if (venue == null) {
			logger.error("UserService._cancelOrder: Venue {} not found while attempting to cancel order {}", event.getEventVenueID(), orderID);
			return;
		}
		Segment segment = venue.getSegmentByID(order.getSegmentId());
		if (segment == null) {
			logger.error("UserService._cancelOrder: Segment {} not found while attempting to cancel order {}", order.getSegmentId(), orderID);
			return;
		}

        switch (segment.getSegmentType()) {
            case "S" -> segment.cancelReservation(ReservationRequest.forSeats(order.getEventId(), order.getSeats(), order.getSegmentId()));
            case "F" -> segment.cancelReservation(ReservationRequest.forField(order.getEventId(), order.getNumOfTickets(), order.getSegmentId()));
            default -> logger.error("UserService._cancelOrder: Unknown segment type {} for segment {} while attempting to cancel order {}", segment.getSegmentType(), segment.getSegmentID(), orderID);
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
            if (authenticationService.extractRoleFromToken(sessionToken) != "Signed") {
				logger.warn("Only user can get order history.");
				return Result.makeFail("Only user can get order history.");
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
			List<Order> orders = orderRepo.getOrdersBySubjectID(String.valueOf(userID));
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

    public Result<List<String>> changeSeatsToOrder(String orderId, String sTocken, List<String> newSeatIds){
        
        try {
            // if seatsToAdd and SeatsToReamove's intersection is not empty, abort
            logger.info("Attempting to change seats for order {} with new seats {}.", orderId, newSeatIds);
            if (newSeatIds == null || newSeatIds.isEmpty()) {
                return Result.makeFail("New seat IDs list cannot be null or empty");
            }

            logger.info("Verifying session token for change.");
			if (!authenticationService.validateToken(sTocken)) {
				logger.warn("Invalid session token provided for reservation.");
				return Result.makeFail("Invalid session token.");
			}
            if (authenticationService.extractRoleFromToken(sTocken) == "Admin") {
				logger.warn("Invalid session token provided for reservation.");
				return Result.makeFail("Invalid session token.");
			}
			String subjectID = authenticationService.extractSubjectFromToken(sTocken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);

            // get order seats. 
            Order order = orderRepo.getOrder(orderId);
            if (order == null) {
                logger.error("Order {} not found for changing seats.", orderId);
                return Result.makeFail("Order not found");
            }
            if (!order.isBelongsToSubject(subjectID)) {
                logger.error("Order {} does not belong to the user with the provided token for changing seats.", orderId);
                return Result.makeFail("Order does not belong to the given user");
            }   
            if (order.getOrderType() != OrderType.SEAT) {
                logger.error("Order {} is not a seat order for changing seats.", orderId);
                return Result.makeFail("Cannot change seats for a non-seat order");
            }
            if(!order.isActive()) {
                logger.error("Order {} is not active for changing seats.", orderId);
                return Result.makeFail("Cannot change seats for a non-active order");
            }
            
            List<String> oldSeats = order.getSeats();

            List<String> intersection = getIntersection(newSeatIds, oldSeats);

            // remove intersection from new seats -> seatsToAdd
            List<String> seatsToAdd = removeFromList(newSeatIds, intersection);
            // remove intersection from old seats -> seatsToRemove
            List<String> seatsToRemove = removeFromList(oldSeats, intersection);
            // reserve new seatsToAdd
            Event event = eventRepo.getEventByID(order.getEventId());
            if (event == null) {
                return Result.makeFail("Event not found");
            }

            logger.info("Reserving new seats {} for order {}.", seatsToAdd, orderId);
            venueRepo.reserveTickets(event.getEventVenueID(), order.getSegmentId(), seatsToAdd, order.getEventId());
            
            // free seatsToRemove
            logger.info("Freeing old seats {} for order {}.", seatsToRemove, orderId);
            venueRepo.freeTickets(event.getEventVenueID(), order.getSegmentId(), seatsToRemove, order.getEventId());

            logger.info("Updating order {} with new seats {}.", orderId, newSeatIds);
            order.updateSeats(newSeatIds);

            return Result.makeOk(newSeatIds);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to change seats for order {}: " + e.getMessage(), orderId);
            return Result.makeFail(e.getMessage());
        }
        
        
        catch (Exception e) {
            logger.error("Unexpected error during changing seats: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Integer> changeNumOfSeatsInFieldOrder(String orderId, String sTocken, int newSeatsNum){
        try {
            // if seatsToAdd and SeatsToReamove's intersection is not empty, abort
            logger.info("Attempting to change number of seats for order {} with new number of seats {}.", orderId, newSeatsNum);
            if (newSeatsNum <= 0) {
                return Result.makeFail("New number of seats must be greater than zero");
            }

            logger.info("Verifying session token for edit.");
			if (!authenticationService.validateToken(sTocken)) {
				logger.warn("Invalid session token provided for edit.");
				return Result.makeFail("Invalid session token.");
			}
            if (authenticationService.extractRoleFromToken(sTocken) == "Admin") {
				logger.warn("Invalid session token provided for edit.");
				return Result.makeFail("Invalid session token.");
			}
			String subjectID = authenticationService.extractSubjectFromToken(sTocken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);

            // get order seats. 
            Order order = orderRepo.getOrder(orderId);
            if (order == null) {
                logger.error("Order {} not found for changing seats.", orderId);
                return Result.makeFail("Order not found");
            }
            if (!order.isBelongsToSubject(subjectID)) {
                logger.error("Order {} does not belong to the user with the provided token for changing seats.", orderId);
                return Result.makeFail("Order does not belong to the given user");
            }   
            if (order.getOrderType() != OrderType.FIELD) {
                logger.error("Order {} is not a field order for changing seats.", orderId);
                return Result.makeFail("Cannot change seats for a non-field order");
            }
            if(!order.isActive()) {
                logger.error("Order {} is not active for changing seats.", orderId);
                return Result.makeFail("Cannot change seats for a non-active order");
            }
            
            int oldNumOfTickets = order.getNumOfTickets();

            if (oldNumOfTickets == newSeatsNum){
                logger.info("New number of seats is the same as the old number of seats for order {}. No changes needed.", orderId);
                return Result.makeOk(newSeatsNum);
            }
            Event event = eventRepo.getEventByID(order.getEventId());
            if (event == null) {
                return Result.makeFail("Event not found");
            }

            if (oldNumOfTickets < newSeatsNum) {
                // reserve new seatsToAdd
                int seatsToAdd = newSeatsNum - oldNumOfTickets;

                logger.info("Reserving {} new seats for order {}.", seatsToAdd, orderId);
                venueRepo.reserveTickets(event.getEventVenueID(), order.getSegmentId(), seatsToAdd, order.getEventId());
            } else {
                // free seatsToRemove
                int seatsToRemove = oldNumOfTickets - newSeatsNum;
                logger.info("Freeing {} old seats for order {}.", seatsToRemove, orderId);
                venueRepo.freeTickets(event.getEventVenueID(), order.getSegmentId(), seatsToRemove, order.getEventId());
            }

            logger.info("Updating order {} with new seats {}.", orderId, newSeatsNum);
            order.updateNumOfTickets(newSeatsNum);

            return Result.makeOk(newSeatsNum);

        } catch (IllegalArgumentException e) {
            logger.error("Failed to change seats for order {}: " + e.getMessage(), orderId);
            return Result.makeFail(e.getMessage());
        }
        
        
        catch (Exception e) {
            logger.error("Unexpected error during changing seats: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }


    private List<String> getIntersection(List<String> list1, List<String> list2) {
        List<String> intersection = new ArrayList<>();
        for (String item : list1) {
            if (list2.contains(item)) {
                intersection.add(item);
            }
        }
        return intersection;
    }
    private List<String> removeFromList(List<String> original, List<String> toRemove) {
        List<String> result = new ArrayList<>(original);
        result.removeAll(toRemove);
        return result;
    }

	public Result<Boolean> cancelOrder(String orderId) { // to call when order is expired
		try {
			logger.info("Attempting to cancel order {}.", orderId);
			Order order = orderRepo.getOrder(orderId);
			if (order == null) {
				logger.error("Order {} not found for cancellation.", orderId);
				return Result.makeFail("Order not found");
			}
			if (!order.isActive()) {
				logger.error("Order {} is not active for cancellation.", orderId);
				return Result.makeFail("Order is not active");
			}
			_cancelOrder(orderId);
        return Result.makeOk(true);
		} catch (IllegalArgumentException e) {
			logger.error("Failed to cancel order {}: " + e.getMessage(), orderId);
			return Result.makeFail(e.getMessage());
		} catch (Exception e) {	
			logger.error("Unexpected error during cancelling order {}: " + e.getMessage(), orderId);
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
    }
}
