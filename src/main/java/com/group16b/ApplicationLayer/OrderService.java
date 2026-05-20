package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.PaymentInfo;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.TicketGateway;

import io.jsonwebtoken.JwtException;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
	private final IAuthenticationService authenticationService;
    private final ITicketGateway ticketGateway = new TicketGateway();
	private final IOrderRepository orderRepo = OrderRepositoryMapImpl.getInstance();
	private final IRepository<Venue> venueRepo;
	private final IEventRepository eventRepo = new EventRepositoryMapImpl();
	private final IRepository<User> userRepo = new UserRepositoryMapImpl();
    private final IProductionCompanyRepository productionCompanyRepo;
	private final IPaymentGateway paymentService;

    public OrderService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepo, IPaymentGateway paymentGateway, IRepository<Venue> venueRepo) {
		this.authenticationService = authenticationService;
		this.productionCompanyRepo=productionCompanyRepo;
		this.venueRepo = venueRepo;
		this.paymentService = paymentGateway;
	}

    public Result<List<TicketDTO>> CompleteActiveOrder(String userId, String orderID, String sTocken, PaymentInfo paymentInfo) {
		try {
			logger.info("OrderService.CompleteActiveOrder: Attempting to complete order {} for user {}", orderID, userId);

			// 1. System - check active order status.
			Order order = orderRepo.findByID(orderID);

			logger.info("OrderService.CompleteActiveOrder: validate Order {} is activeOrder for user {}", orderID, userId);
			order.validiteOrderIsActive();
			

            logger.info("Verifying session token for completion.");
			String subjectID = validateAssureNotAdminGetSubjectID(sTocken);
            logger.info("Session token verified successfully.");


			// 1.5 System - verify order belungs to the user.
			logger.info("OrderService.CompleteActiveOrder: verifying that order {} belongs to user {}", orderID, userId);
			order.verifyBelongsToSubject(subjectID);

			// 2. System - calculates price of tickets according to company and event policies.
			logger.info("OrderService.CompleteActiveOrder: calculating price for order {} for user {}", orderID, userId);
			double price = order.getTotalOrderprice();


			// 3. System - charges the user for the designed price.
			logger.info("OrderService.CompleteActiveOrder: processing payment for order {} for user {} with price {}", orderID, userId, price);
			paymentService.processPayment(paymentInfo, price); // hander feiler well caouse it will happend regularly
			logger.info("OrderService.CompleteActiveOrder: user {} paid {} successfully for order {}", userId, price, orderID);


			// 4. System - creates Tickets for each of the tickets.
			logger.info("OrderService.CompleteActiveOrder: generating tickets for order {} for user {}", orderID, userId);
			List<TicketDTO> tikketDTOs = new ArrayList<>();
			for (int i = 0; i < order.getNumOfTickets(); i++) {
				TicketDTO ticketDTO = ticketGateway.generateTicket(order.getEventId(), String.valueOf(userId), order.getSegmentId(), order.getSeats().get(i), price); // TODO: implement actual ticket generation logic
				tikketDTOs.add(ticketDTO);
            }

			logger.info("OrderService.CompleteActiveOrder: generated {} tickets successfully for order {} for user {}", tikketDTOs.size(), orderID, userId);
			order.CompleteOrder();
			logger.info("OrderService.CompleteActiveOrder: Order {} completed successfully for user {}", orderID, userId);

			
			// 5. System - sends the user his acquired tickets.
			return Result.makeOk(tikketDTOs);


		} catch (AuthException e) {
			logger.error("Authentication error during retrieving orders: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (IllegalStateException e) { 
			logger.error("OrderService.CompleteActiveOrder: Cannot complete Already completed order {} for user {}: {}", orderID, userId, e.getMessage());
			paymentService.cancelPayment(); 
			_cancelOrder(orderID);
			return Result.makeFail(e.getMessage());
		} catch (IllegalArgumentException e) { 
			logger.error("OrderService.CompleteActiveOrder: {}", e.getMessage());
			paymentService.cancelPayment(); 
			_cancelOrder(orderID);
			return Result.makeFail(e.getMessage());
		}catch (PaymentFailedException e) {
			logger.error("OrderService.CompleteActiveOrder: Payment failed for order {} for user {}: {}", orderID, userId, e.getMessage());
			return Result.makeFail("Payment failed: " + e.getMessage());
		}
		catch (Exception e) {
			paymentService.cancelPayment();
			_cancelOrder(orderID); 
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
    
	private void _cancelOrder(String orderID) {
	try {
		Order order = orderRepo.findByID(orderID);
		orderRepo.delete(orderID);
		int eventID = order.getEventId();
		Event event = eventRepo.findByID(String.valueOf(eventID));

		Venue venue = venueRepo.findByID(event.getEventVenueID());
		Segment segment = venue.getSegmentByID(order.getSegmentId());
		
			switch (segment.getSegmentType()) {
            case "S" -> segment.cancelReservation(ReservationRequest.forSeats(order.getEventId(), order.getSeats(), order.getSegmentId()));
            case "F" -> segment.cancelReservation(ReservationRequest.forField(order.getEventId(), order.getNumOfTickets(), order.getSegmentId()));
            default -> logger.error("OrderService._cancelOrder: Unknown segment type {} for segment {} while attempting to cancel order {}", segment.getSegmentType(), segment.getSegmentID(), orderID);
        }
		} catch (Exception e) {
			logger.error("OrderService._cancelOrder: Failed to cancel reservation for order {}: {}", orderID, e.getMessage());
			 // we log the error but do not throw it further as the main goal of this method is to cancel the order and we don't want a failure in cancelling the reservation to prevent the order cancellation.
		}
        

	}

    public Result<List<OrderDTO>> getUserOrders(String sessionToken) {
		try {
			//auth
			logger.info("OrderService.getUserOrders: Verifying session token for retrieving orders of user with session token {0}.", sessionToken);
			String userID = validateAndGetUserID(sessionToken);
			logger.info("OrderService.getUserOrders: Session token verified successfully.");

			//get orders
			logger.info("OrderService.getUserOrders: Retrieving orders for user {0}.", userID);
			List<Order> orders = orderRepo.getBySubjectId(String.valueOf(userID));
			
			logger.info("OrderService.getUserOrders: Mapping orders to OrderDTOs for user {0}.", userID);
			List<OrderDTO> orderDTOs = new ArrayList<>();
			for (Order order : orders) {
				OrderDTO orderDTO = new OrderDTO(order); 
				orderDTOs.add(orderDTO);
			}
			logger.info("OrderService.getUserOrders: Orders retrieved successfully for user {0}.", userID);
			return Result.makeOk(orderDTOs);

		}catch (AuthException e) {
			logger.error("OrderService.getUserOrders: Authentication error during retrieving orders: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (IllegalArgumentException e) {
			logger.error("OrderService.getUserOrders: Invalid argument provided: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e){
			logger.error("OrderService.getUserOrders: Illegal state encountered: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		}catch (JwtException e) {
			logger.error("OrderService.getUserOrders: JWT authentication error during retrieving orders: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("OrderService.getUserOrders: Unexpected error during retrieving orders: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	
    public Result<List<String>> changeSeatsToOrder(String orderId, String sTocken, List<String> newSeatIds){
        
        try {
            // if seatsToAdd and SeatsToReamove's intersection is not empty, abort
            logger.info("OrderService.changeSeatsToOrder: Attempting to change seats for order {} with new seats {}.", orderId, newSeatIds);
            if (newSeatIds == null || newSeatIds.isEmpty()) {
                return Result.makeFail("New seat IDs list cannot be null or empty");
            }

            logger.info("OrderService.changeSeatsToOrder: Verifying session token for change.");
			String subjectID = validateAssureNotAdminGetSubjectID(sTocken);
			logger.info("OrderService.changeSeatsToOrder: Session token verified successfully.");

            // get order seats. 
            Order order = orderRepo.findByID(orderId);

			logger.info("OrderService.changeSeatsToOrder: verifying that order {} belongs to the user with the provided token for changing seats.", orderId);
            order.verifyBelongsToSubject(subjectID);

			logger.info("OrderService.changeSeatsToOrder: verifying that order {} is a seat order for changing seats.", orderId);
            order.verifyTypeSeats();

			logger.info("OrderService.changeSeatsToOrder: verifying that order {} is active for changing seats.", orderId);
            order.validiteOrderIsActive();

            List<String> oldSeats = order.getSeats();
            List<String> intersection = getIntersection(newSeatIds, oldSeats);

            // remove intersection from new seats -> seatsToAdd
            List<String> seatsToAdd = removeFromList(newSeatIds, intersection);
            // remove intersection from old seats -> seatsToRemove
            List<String> seatsToRemove = removeFromList(oldSeats, intersection);
            // reserve new seatsToAdd
			int eventID = order.getEventId();
            Event event = eventRepo.findByID(String.valueOf(eventID));

            logger.info("OrderService.changeSeatsToOrder: Reserving new seats {} for order {}.", seatsToAdd, orderId);
			Venue venue = venueRepo.findByID(event.getEventVenueID());
			venue.reserveSeats(ReservationRequest.forSeats(order.getEventId(), seatsToAdd, order.getSegmentId()));
            
            // free seatsToRemove
            logger.info("OrderService.changeSeatsToOrder: Freeing old seats {} for order {}.", seatsToRemove, orderId);
			venue.freeSeats(ReservationRequest.forSeats(order.getEventId(), seatsToRemove, order.getSegmentId()));
			
			logger.info("OrderService.changeSeatsToOrder: Validating purchase policy for event {} for order {}.", eventID, orderId);
			validatePurchasePolicy(event.getEventID());

			Segment segment = venue.getSegmentByID(order.getSegmentId());
			double pricePerSeat = segment.getPrice(eventID);
			logger.info("OrderService.changeSeatsToOrder: Calculating discount policies for event {} for order {}.", eventID, orderId);
            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, newSeatIds.size());

            logger.info("OrderService.changeSeatsToOrder: Updating order {} with new seats {}.", orderId, newSeatIds);
            order.updateSeats(newSeatIds, priceAfterDiscountPolicy);

            return Result.makeOk(newSeatIds);
        } catch (AuthException e) {
			logger.error("OrderService.changeSeatsToOrder: Authentication error during changing seats for order {}: {}", orderId, e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (IllegalStateException e) {
			logger.error("OrderService.changeSeatsToOrder: {}", orderId, e.getMessage());
			return Result.makeFail(e.getMessage());
		}
		catch (IllegalArgumentException e) {
            logger.error("OrderService.changeSeatsToOrder: Failed to change seats for order {}: {}", orderId, e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
            logger.error("OrderService.changeSeatsToOrder: Unexpected error during changing seats: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
	
    public Result<Integer> changeNumOfSeatsInFieldOrder(String orderId, String sTocken, int newSeatsNum){
        try {
            // if seatsToAdd and SeatsToReamove's intersection is not empty, abort
            logger.info("OrderService.changeNumOfSeatsInFieldOrder: Attempting to change number of seats for order {} with new number of seats {}.", orderId, newSeatsNum);
            if (newSeatsNum <= 0) {
                return Result.makeFail("New number of seats must be greater than zero");
            }

            logger.info("OrderService.Verifying session token for edit.");
			String subjectID = validateAssureNotAdminGetSubjectID(sTocken);
			logger.info("OrderService.changeNumOfSeatsInFieldOrder: Verifying session token for edit.");

            // get order seats. 
			logger.info("OrderService.changeNumOfSeatsInFieldOrder: Attempting to reserve seats for {}", subjectID);
            Order order = orderRepo.findByID(orderId);

			logger.info("OrderService.changeNumOfSeatsInFieldOrder: verifying that order {} belongs to the user with the provided token for changing number of seats.", orderId);
            order.verifyBelongsToSubject(subjectID);

			logger.info("OrderService.changeNumOfSeatsInFieldOrder: verifying that order {} is a field order for changing number of seats.", orderId);
            order.verifyTypeField();

            logger.info("OrderService.changeNumOfSeatsInFieldOrder: verifying that order {} is active for changing number of seats.", orderId);
            order.validiteOrderIsActive();
            
            int oldNumOfTickets = order.getNumOfTickets();

            if (oldNumOfTickets == newSeatsNum){
                logger.info("OrderService.changeNumOfSeatsInFieldOrder: New number of seats is the same as the old number of seats for order {}. No changes needed.", orderId);
                return Result.makeOk(newSeatsNum);
            }
			int eventID = order.getEventId();
            Event event = eventRepo.findByID(String.valueOf(eventID));
			Venue venue = venueRepo.findByID(event.getEventVenueID());
            if (oldNumOfTickets < newSeatsNum) {
                // reserve new seatsToAdd
                int seatsToAdd = newSeatsNum - oldNumOfTickets;

                logger.info("OrderService.changeNumOfSeatsInFieldOrder: Reserving {} new seats for order {}.", seatsToAdd, orderId);
				venue.reserveSeats(ReservationRequest.forField(order.getEventId(), seatsToAdd, order.getSegmentId()));
            } else {
                // free seatsToRemove
                int seatsToRemove = oldNumOfTickets - newSeatsNum;
                logger.info("OrderService.changeNumOfSeatsInFieldOrder: Freeing {} old seats for order {}.", seatsToRemove, orderId);
				venue.freeSeats(ReservationRequest.forField(order.getEventId(), seatsToRemove, order.getSegmentId()));
            }

            logger.info("OrderService.changeNumOfSeatsInFieldOrder: Updating order {} with new seats {}.", orderId, newSeatsNum);
			Segment segment = venue.getSegmentByID(order.getSegmentId());

			double pricePerSeat = segment.getPrice(eventID);
			validatePurchasePolicy(event.getEventID());

            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, newSeatsNum);
			order.updateNumOfTickets(newSeatsNum, priceAfterDiscountPolicy);

            return Result.makeOk(newSeatsNum);

        } catch (IllegalArgumentException e) {
            logger.error("OrderService.changeNumOfSeatsInFieldOrder: Failed to change seats for order {}: {}", orderId, e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("OrderService.changeNumOfSeatsInFieldOrder: Failed to change seats for order {}: {}", orderId, e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (AuthException e) {
			logger.error("OrderService.changeNumOfSeatsInFieldOrder: Authentication error during changing number of seats for order {}: {}", orderId, e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (Exception e) {
            logger.error("OrderService.changeNumOfSeatsInFieldOrder: Unexpected error during changing seats: {}", e.getMessage());
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
			Order order = orderRepo.findByID(orderId);
			
			if (order == null) {
    			return Result.makeFail("Order not found");
}
			order.validiteOrderIsActive();
			_cancelOrder(orderId);
        return Result.makeOk(true);
		} catch (IllegalArgumentException e) {
			logger.error("OrderService.cancelOrder: Failed to cancel order {}: {}", orderId, e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("OrderService.cancelOrder: Failed to cancel order {}: {}", orderId, e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (Exception e) {	
			logger.error("OrderService.cancelOrder: Unexpected error during cancelling order {}: {}", orderId, e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
    }

    private double calculateDiscountPolicies(int eventID, double pricePerSeat, int amount) {
        Event event = eventRepo.findByID(String.valueOf(eventID));
        Set<DiscountPolicy> discountPolicy = event.getEventDiscountPolicy();
        Set<DiscountPolicy> companyDiscountPolicy = productionCompanyRepo.findByID(String.valueOf(event.getEventProductionCompanyID())).getDiscountPolicy();

            if (discountPolicy == null) {
                logger.error("No discount policy found for event {}", eventID);
                throw new IllegalArgumentException("No discount policy found for this event");
            }
            if (companyDiscountPolicy == null) {
                logger.error("No discount policy found for production company {}", event.getEventProductionCompanyID());
                throw new IllegalArgumentException("No discount policy found for this event's production company");}
            discountPolicy.addAll(companyDiscountPolicy);

            double priceAfterDiscountPolicy = pricePerSeat * amount;
            for (DiscountPolicy dp : discountPolicy) {
                priceAfterDiscountPolicy = dp.calculateDiscount(priceAfterDiscountPolicy);
            }
            return priceAfterDiscountPolicy;
    }
    private void validatePurchasePolicy(int eventID) {
        Event event = eventRepo.findByID(String.valueOf(eventID));
        Set<PurchasePolicy> purchasePolicy = event.getEventPurchasePolicy();
        Set<PurchasePolicy> companyPurchasePolicy = productionCompanyRepo.findByID(String.valueOf(event.getEventProductionCompanyID())).getPurchasePolicy();

            if (purchasePolicy == null) {
                logger.error("No purchase policy found for event {}", eventID);
                throw new IllegalArgumentException("No purchase policy found for this event");
            }
            if (companyPurchasePolicy == null) {
                logger.error("No purchase policy found for production company {}", event.getEventProductionCompanyID());
                throw new IllegalArgumentException("No purchase policy found for this event's production company");
            }

            purchasePolicy.addAll(companyPurchasePolicy);
            for (PurchasePolicy pp : purchasePolicy) {
                if (!pp.validatePurchase()) {
                    logger.error("User did not meet purchase policy requirements");
                    throw new IllegalArgumentException("User did not meet purchase policy requirements");
                }
            }
    }

	private String validateAssureNotAdminGetSubjectID(String sessionToken)
    {
        if (!authenticationService.validateToken(sessionToken)  ) {
            throw new AuthException("Invalid Token");
        }
        if (authenticationService.isAdminToken(sessionToken)) {
            throw new AuthException("Admins are not allowed to perform operation");
        }
        String subjectID = authenticationService.extractSubjectFromToken(sessionToken);
        return subjectID;
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
