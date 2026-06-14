package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Exceptions.OrderExpiredException;
import com.group16b.ApplicationLayer.Exceptions.PaymentFailedException;
import com.group16b.ApplicationLayer.Exceptions.PaymentStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.TicketGenerationException;
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
import com.group16b.DomainLayer.Order.OrderType;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchaseContext;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicyException;


@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
	private final IAuthenticationService authenticationService;
    private final ITicketGateway ticketGateway;
	private final IOrderRepository orderRepo;
	private final IRepository<Venue> venueRepo;
	private final IEventRepository eventRepo;
	private final IRepository<User> userRepo;
    private final IProductionCompanyRepository productionCompanyRepo;
	private final IPaymentGateway paymentService;

	private static final String POSSIBLE_REFUND_MSG ="If you were charged, the amount will be refunded automatically. If not resolved, please contact support with your order ID.";
	private static final String REFUND_MSG="you will be refunded automatically. If not resolved, please contact support with your order ID.";

    public OrderService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepo, IPaymentGateway paymentGateway, IRepository<Venue> venueRepo, IEventRepository eventRepo, IRepository<User> userRepo, IOrderRepository orderRepo, ITicketGateway ticketGateway) {
		this.authenticationService = authenticationService;
		this.productionCompanyRepo=productionCompanyRepo;
		this.venueRepo = venueRepo;
		this.eventRepo = eventRepo;
		this.userRepo = userRepo;
		this.orderRepo = orderRepo;
		this.paymentService = paymentGateway;
		this.ticketGateway = ticketGateway;
	}

    public Result<String> CompleteActiveOrder(String orderID, String sTocken, PaymentInfo paymentInfo) {
		Integer transactionId = null;
		try {
			logger.info("OrderService.CompleteActiveOrder: Attempting to complete order {} ", orderID);

			// 1. user verification
            logger.info("Verifying session token for completion.");
			String subjectID = validateAssureNotAdminGetSubjectID(sTocken);

			// 2. order verification 
			Order order = orderRepo.findByID(orderID);

			logger.info("OrderService.CompleteActiveOrder: validate Order {} is activeOrder", orderID);
			order.validiteOrderIsActive();
			
			logger.info("OrderService.CompleteActiveOrder: verifying that order {} belongs to user {}", orderID, subjectID);
			order.verifyBelongsToSubject(subjectID);

			//3. price calculation
			logger.info("OrderService.CompleteActiveOrder: calculating price for order {} for user {}", orderID, subjectID);
			double price = order.getTotalOrderprice();

			// 4.Payment processing
			logger.info("OrderService.CompleteActiveOrder: processing payment for order {} for user {} with price {}", orderID, subjectID, price);
			transactionId = paymentService.processPayment(paymentInfo, price); // hander feiler well caouse it will happend regularly
			
			// 5. ticket generation
			logger.info("OrderService.CompleteActiveOrder: generating ticket for order {} for user {}", orderID, subjectID);
			String ticket = generateTicketForOrder(order, subjectID);
			
			// 6. complete order with optimistic locking retry
			logger.info("OrderService.CompleteActiveOrder: completing order {} for user {} with optimistic locking retry", orderID, subjectID);
			completeOrderWithOptimisticRetry(orderID, subjectID);
			
			// 7. return tickets
			return Result.makeOk(ticket);

		} catch (OrderExpiredException e) {
			logger.error("OrderService.CompleteActiveOrder: Order {} expired: {}.", orderID, e.getMessage());
			safeRefund(transactionId);
			_cancelOrder(orderID);
			return Result.makeFail("Order expired: " + e.getMessage()+". "+POSSIBLE_REFUND_MSG);

		} catch (PaymentFailedException e) {
			logger.warn("OrderService.CompleteActiveOrder: Payment failed for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Payment failed: " + e.getMessage());

		} catch (TicketGenerationException e) {
			logger.error("OrderService.CompleteActiveOrder: Ticket generation failed for order {}: {}", orderID, e.getMessage());
			safeRefund(transactionId);

			return Result.makeFail("Ticket generation failed: " + e.getMessage()+". "+REFUND_MSG);

		} catch (AuthException e) {
			logger.warn("OrderService.CompleteActiveOrder: Authentication failed for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());

		} catch (IllegalArgumentException e) {
			logger.warn("OrderService.CompleteActiveOrder: Invalid argument for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Invalid argument: " + e.getMessage());

		} catch (IllegalStateException e) {
			logger.warn("OrderService.CompleteActiveOrder: Illegal state for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Illegal state: " + e.getMessage());

		} catch (OptimisticLockingFailureException e) {
			safeRefund(transactionId);
			return Result.makeFail("Could not complete order due to concurrent update. Please try again. "+POSSIBLE_REFUND_MSG);
		
		} catch(PaymentStatusUnknownException e){
			logger.error("OrderService.CompleteActiveOrder: payment status unknown for order {}. Requires manual reconciliation.",orderID,e);
			return Result.makeFail("Payment could not be verified. "+POSSIBLE_REFUND_MSG);
		
		}catch (Exception e) {
			logger.error("OrderService.CompleteActiveOrder: Unexpected error for order {}: {}", orderID, e.getMessage());
			safeRefund(transactionId);

			return Result.makeFail("An unexpected error occurred: " + e.getMessage()+". "+POSSIBLE_REFUND_MSG);
		}
	}

	private String generateTicketForOrder(Order order, String subjectID) {
		if (order.getOrderType() == OrderType.SEAT) {
			return ticketGateway.generateSeatingTicket(order.getEventId(), subjectID, order.getSegmentId(), order.getSeats());
		}else{
			return ticketGateway.generateGeneralAdmissionTicket(order.getEventId(), subjectID, order.getSegmentId(), order.getNumOfTickets());
		}
	}

	private void completeOrderWithOptimisticRetry(String orderID, String subjectID) {
		final int maxRetries = 3;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				Order freshOrder = orderRepo.findByID(orderID);

				freshOrder.validiteOrderIsActive();
				freshOrder.verifyBelongsToSubject(subjectID);

				freshOrder.CompleteOrder();
				orderRepo.save(freshOrder);
				return;

			} catch (OptimisticLockingFailureException e) {
				logger.warn(
					"OrderService.completeOrderWithOptimisticRetry: Optimistic lock failed for order {} on attempt {}/{}",
					orderID,
					attempt,
					maxRetries
				);

				if (attempt == maxRetries) {
					throw e;
				}
			}
		}

		throw new OptimisticLockingFailureException("Failed to complete order after retries");
	}
    
	private void _cancelOrder(String orderID) {
	try {
		Order order = orderRepo.findByID(orderID);
		orderRepo.delete(orderID);
		int eventID = order.getEventId();
		Event event = eventRepo.findByID(String.valueOf(eventID));
		Venue venue = venueRepo.findByID(event.getEventVenueID());
		if (venue.segmentType(order.getSegmentId()) == OrderType.SEAT) {
			venue.cancelSeatReservation(order.getSegmentId(), order.getSeats(), eventID);
		} else {
			venue.cancelFieldReservation(order.getSegmentId(), order.getNumOfTickets(), eventID);
		}

		venueRepo.save(venue);
		} catch (Exception e) {
			logger.error("OrderService._cancelOrder: Failed to cancel reservation for order {}: {}", orderID, e.getMessage());
			 // we log the error but do not throw it further as the main goal of this method is to cancel the order and we don't want a failure in cancelling the reservation to prevent the order cancellation.
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
			orderRepo.save(order);

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
			orderRepo.save(order);
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

	public Result<Boolean> cancelOrder(String orderId, String sTocken) { // to call when order is expired
		try {

			logger.info("Verifying session token for completion.");
			String subjectID = validateAssureNotAdminGetSubjectID(sTocken);
            logger.info("Session token verified successfully.");
			
			
			logger.info("Attempting to cancel order {}.", orderId);
			Order order = orderRepo.findByID(orderId);
			order.verifyBelongsToSubject(subjectID);
			
			order.validiteOrderIsActive();
			_cancelOrder(orderId);
			return Result.makeOk(true);
		}catch (AuthException e) {
			logger.error("OrderService.cancelOrder: Authentication error during cancelling order {}: {}", orderId, e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
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
                discountPolicy = Set.of(); // if no discount policy for the event, we will just use the company discount policy (if exists)
            }
            if (companyDiscountPolicy == null) {
                logger.error("No discount policy found for production company {}", event.getEventProductionCompanyID());
				companyDiscountPolicy = Set.of(); // if no discount policy for the production company, we will just use the event discount policy (if exists)
			}
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
                purchasePolicy = Set.of(); // if no purchase policy for the event, we will just use the company purchase policy (if exists)
            }
            if (companyPurchasePolicy == null) {
                logger.error("No purchase policy found for production company {}", event.getEventProductionCompanyID());
                companyPurchasePolicy = Set.of(); // if no purchase policy for the production company, we will just use the event purchase policy (if exists)
            }

            purchasePolicy.addAll(companyPurchasePolicy);
			for (PurchasePolicy pp : purchasePolicy) {
				try {
					pp.validatePurchase(new PurchaseContext(0, 0));
				} catch (PurchasePolicyException e) {
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

	//no need to care for the exception type, as it is a automatic refund, meaning that any issue here is critical and should betreated the same way
	private void safeRefund(Integer transactionId) {
		//payment didnt proceed
		if (transactionId == null) 
			return;

		try {
			paymentService.cancelPayment(transactionId);
		} catch (Exception e) {
			logger.error("Refund failed for transaction {}", transactionId, e);
		}
	}
	
}
