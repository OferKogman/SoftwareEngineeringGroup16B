package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.group16b.ApplicationLayer.Interfaces.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Exceptions.IllegalPaymentInfoException;
import com.group16b.ApplicationLayer.Exceptions.IllegalTicketInfoException;
import com.group16b.ApplicationLayer.Exceptions.IssueTicketStatusUnknownException;
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
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchaseContext;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicyException;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Venue;


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
	private final INotificationService notificationService;

	private static final String POSSIBLE_REFUND_MSG ="If you were charged, the amount will be refunded automatically. If not resolved, please contact support with your order ID.";
	private static final String REFUND_MSG="you will be refunded automatically. If not resolved, please contact support with your order ID.";

    public OrderService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepo, IPaymentGateway paymentGateway, IRepository<Venue> venueRepo, IEventRepository eventRepo, IRepository<User> userRepo, IOrderRepository orderRepo, ITicketGateway ticketGateway, INotificationService notificationService) {
		this.authenticationService = authenticationService;
		this.productionCompanyRepo=productionCompanyRepo;
		this.venueRepo = venueRepo;
		this.eventRepo = eventRepo;
		this.userRepo = userRepo;
		this.orderRepo = orderRepo;
		this.paymentService = paymentGateway;
		this.ticketGateway = ticketGateway;
		this.notificationService = notificationService;
	}

    public Result<String> CompleteActiveOrder(String orderID, String sTocken, PaymentInfo paymentInfo) {
		logger.info(paymentInfo.cardNumber() + "" + paymentInfo.currency());
		Integer transactionId = null;
		String ticket=null;
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
			ticket = generateTicketForOrder(order, subjectID);
			
			// 6. complete order with optimistic locking retry
			logger.info("OrderService.CompleteActiveOrder: completing order {} for user {} with optimistic locking retry", orderID, subjectID);
			completeOrderWithOptimisticRetry(orderID, subjectID,transactionId,ticket);

			try {
				String notificationMessage = String.format(
						"Your order is complete! Ticket: %s",
						ticket
				);

				notificationService.notify(subjectID, notificationMessage);
				logger.info("OrderService.CompleteActiveOrder: notification sent to user {} for order {}", subjectID, orderID);
			} catch (Exception notificationException) {
				logger.warn(
						"OrderService.CompleteActiveOrder: order {} was completed, but notification failed for user {}: {}",
						orderID,
						subjectID,
						notificationException.getMessage()
				);
			}

			// 7. return tickets
			return Result.makeOk(ticket);

		} catch (OrderExpiredException e) {
			logger.error("OrderService.CompleteActiveOrder: Order {} expired: {}.", orderID, e.getMessage());
			safeExternalRollback(transactionId,ticket);
			_cancelOrder(orderID);
			return Result.makeFail("Order expired: " + e.getMessage()+". "+POSSIBLE_REFUND_MSG);

		} catch (PaymentFailedException e) {
			logger.warn("OrderService.CompleteActiveOrder: Payment failed for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Payment failed: " + e.getMessage());

		} catch (TicketGenerationException e) {
			logger.error("OrderService.CompleteActiveOrder: Ticket generation failed for order {}: {}", orderID, e.getMessage());
			safeExternalRollback(transactionId,ticket);

			return Result.makeFail("Ticket generation failed: " + e.getMessage()+". "+REFUND_MSG);

		} catch (AuthException e) {
			logger.warn("OrderService.CompleteActiveOrder: Authentication failed for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.warn("OrderService.CompleteActiveOrder: Invalid argument for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Invalid argument: " + e.getMessage());
		} catch (IllegalPaymentInfoException e) {
			logger.warn("OrderService.CompleteActiveOrder: IllegalPaymentInfoException for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Bad payment Info: " + e.getMessage());
		} catch (IllegalTicketInfoException e) {
			logger.warn("OrderService.CompleteActiveOrder: IllegalTicketInfoException for order {}: {}", orderID, e.getMessage());
			safeExternalRollback(transactionId, ticket);
			return Result.makeFail("Bad ticket Info: " + e.getMessage());
		} catch (IllegalStateException e) {
			logger.warn("OrderService.CompleteActiveOrder: Illegal state for order {}: {}", orderID, e.getMessage());
			return Result.makeFail("Illegal state: " + e.getMessage());

		} catch (OptimisticLockingFailureException e) {
			safeExternalRollback(transactionId,ticket);
			return Result.makeFail("Could not complete order due to concurrent update. Please try again. "+POSSIBLE_REFUND_MSG);
		} catch(PaymentStatusUnknownException e){
			logger.error("OrderService.CompleteActiveOrder: payment status unknown for order {}. Requires manual reconciliation.",orderID,e);
			return Result.makeFail("Payment could not be verified. "+POSSIBLE_REFUND_MSG);
		}catch (IssueTicketStatusUnknownException e){
			logger.error("OrderService.CompleteActiveOrder: Issue Ticket status unknown for order {}. Requires manual reconciliation.",orderID,e);
			return Result.makeFail("Ticket could not be verified. "+POSSIBLE_REFUND_MSG);
		}catch (Exception e) {
			logger.error("OrderService.CompleteActiveOrder: Unexpected error for order {}: {}", orderID, e.getMessage());
			safeExternalRollback(transactionId,ticket);

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

	private void completeOrderWithOptimisticRetry(String orderID, String subjectID, int transactionId, String externalTicket) {
		final int maxRetries = 3;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				Order freshOrder = orderRepo.findByID(orderID);

				freshOrder.validiteOrderIsActive();
				freshOrder.verifyBelongsToSubject(subjectID);

				freshOrder.setTransactionId(transactionId);
				freshOrder.setExternalTicket(externalTicket);
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
		boolean venueChanged = false;
		String segmentId = null;
		List<String> seatsToAdd = new ArrayList<>();
		List<String> seatsToRemove = new ArrayList<>();
		Venue venue = null;
		int eventID = 0;
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

			eventID = order.getEventId();
            Event event = eventRepo.findByID(String.valueOf(eventID));
			venue = venueRepo.findByID(event.getEventVenueID());

			logger.info("OrderService.changeSeatsToOrder: Validating purchase policy for event {} for order {}.", eventID, orderId);
			validatePurchasePolicy(event.getEventID(), newSeatIds.size(), sTocken);

			double pricePerSeat = venue.getPriceForSegment(order.getSegmentId(), eventID);
			logger.info("OrderService.changeSeatsToOrder: Calculating discount policies for event {} for order {}.", eventID, orderId);
            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, newSeatIds.size());

            List<String> oldSeats = order.getSeats();
            List<String> intersection = getIntersection(newSeatIds, oldSeats);

            // remove intersection from new seats -> seatsToAdd
            seatsToAdd = removeFromList(newSeatIds, intersection);
            // remove intersection from old seats -> seatsToRemove
            seatsToRemove = removeFromList(oldSeats, intersection);
			segmentId = order.getSegmentId();
            // reserve new seatsToAdd
			
            logger.info("OrderService.changeSeatsToOrder: Reserving new seats {} for order {}.", seatsToAdd, orderId);
			if (!seatsToAdd.isEmpty()) {
				venue.reserveSeats(ReservationRequest.forSeats(eventID, seatsToAdd, segmentId));
				venueChanged = true;
			}
            // free seatsToRemove
            logger.info("OrderService.changeSeatsToOrder: Freeing old seats {} for order {}.", seatsToRemove, orderId);
			if (!seatsToRemove.isEmpty()) {
				venue.freeSeats(ReservationRequest.forSeats(eventID, seatsToRemove, segmentId));
				venueChanged = true;
			}
			
			
			logger.info("OrderService.changeSeatsToOrder: Updating order {} with new seats {}.", orderId, newSeatIds);
			order.updateSeats(newSeatIds, priceAfterDiscountPolicy);
			orderRepo.save(order);
			
			venueRepo.save(venue);

            return Result.makeOk(newSeatIds);
        } catch (AuthException e) {
			logger.error("OrderService.changeSeatsToOrder: Authentication error during changing seats for order {}: {}", orderId, e.getMessage());
			rollbackSeatEditIfNeeded(venue, venueChanged, segmentId, seatsToAdd, seatsToRemove, eventID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (IllegalStateException e) {
			logger.error("OrderService.changeSeatsToOrder: {}", orderId, e.getMessage());
			rollbackSeatEditIfNeeded(venue, venueChanged, segmentId, seatsToAdd, seatsToRemove, eventID);
			return Result.makeFail(e.getMessage());
		}
		catch (IllegalArgumentException e) {
            logger.error("OrderService.changeSeatsToOrder: Failed to change seats for order {}: {}", orderId, e.getMessage());
            rollbackSeatEditIfNeeded(venue, venueChanged, segmentId, seatsToAdd, seatsToRemove, eventID);
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
			logger.error("OrderService.changeSeatsToOrder: Unexpected error during changing seats", e);            rollbackSeatEditIfNeeded(venue, venueChanged, segmentId, seatsToAdd, seatsToRemove, eventID);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
	
    public Result<Integer> changeNumOfSeatsInFieldOrder(String orderId, String sTocken, int newSeatsNum){
        Venue venue = null;
        boolean venueChanged = false;
		String segmentId = null;
		int oldNumOfTickets = 0;
		int newNumOfTickets = newSeatsNum;
        int eventID = 0;
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

			// policy validation
			eventID = order.getEventId();
            Event event = eventRepo.findByID(String.valueOf(eventID));
			venue = venueRepo.findByID(event.getEventVenueID());

			segmentId = order.getSegmentId();
            oldNumOfTickets = order.getNumOfTickets();
			
            if (oldNumOfTickets == newSeatsNum){
				logger.info("OrderService.changeNumOfSeatsInFieldOrder: New number of seats is the same as the old number of seats for order {}. No changes needed.", orderId);
                return Result.makeOk(newSeatsNum);
            }

			validatePurchasePolicy(event.getEventID(), newSeatsNum, sTocken);
			
			double pricePerSeat = venue.getPriceForSegment(order.getSegmentId(), eventID);
			double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, newSeatsNum);
			
			
            if (oldNumOfTickets < newSeatsNum) {
                // reserve new seatsToAdd
                int seatsToAdd = newSeatsNum - oldNumOfTickets;
                logger.info("OrderService.changeNumOfSeatsInFieldOrder: Reserving {} new seats for order {}.", seatsToAdd, orderId);
				venue.reserveSeats(ReservationRequest.forField(eventID, seatsToAdd, segmentId));
				venueChanged = true;
            } else {
                // free seatsToRemove
                int seatsToRemove = oldNumOfTickets - newSeatsNum;
                logger.info("OrderService.changeNumOfSeatsInFieldOrder: Freeing {} old seats for order {}.", seatsToRemove, orderId);
				venue.freeSeats(ReservationRequest.forField(eventID, seatsToRemove, segmentId));
				venueChanged = true;
            }
			
            logger.info("OrderService.changeNumOfSeatsInFieldOrder: Updating order {} with new seats {}.", orderId, newSeatsNum);
			
			
			order.updateNumOfTickets(newSeatsNum, priceAfterDiscountPolicy);
			orderRepo.save(order);
			venueRepo.save(venue);
            
			return Result.makeOk(newSeatsNum);

        } catch (IllegalArgumentException e) {
            logger.error("OrderService.changeNumOfSeatsInFieldOrder: Failed to change seats for order {}: {}", orderId, e.getMessage());
            rollbackFieldEditIfNeeded(venue, venueChanged, segmentId, oldNumOfTickets, newNumOfTickets, eventID);
			return Result.makeFail(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("OrderService.changeNumOfSeatsInFieldOrder: Failed to change seats for order {}: {}", orderId, e.getMessage());
            rollbackFieldEditIfNeeded(venue, venueChanged, segmentId, oldNumOfTickets, newNumOfTickets, eventID);
			return Result.makeFail(e.getMessage());
        } catch (AuthException e) {
			logger.error("OrderService.changeNumOfSeatsInFieldOrder: Authentication error during changing number of seats for order {}: {}", orderId, e.getMessage());
			rollbackFieldEditIfNeeded(venue, venueChanged, segmentId, oldNumOfTickets, newNumOfTickets, eventID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (Exception e) {
            logger.error("OrderService.changeNumOfSeatsInFieldOrder: Unexpected error during changing seats: {}", e.getMessage());
            rollbackFieldEditIfNeeded(venue, venueChanged, segmentId, oldNumOfTickets, newNumOfTickets, eventID);
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
        Set<DiscountPolicy> companyDiscountPolicy = productionCompanyRepo
                .findByID(String.valueOf(event.getEventProductionCompanyID())).getDiscountPolicy();
        Set<DiscountPolicy> allPolicies = new HashSet<>();

        if (discountPolicy != null) {
            allPolicies.addAll(discountPolicy);
        } else {
            logger.error("No discount policy found for event {}", eventID);
        }
        if (companyDiscountPolicy != null) {
            allPolicies.addAll(companyDiscountPolicy);
        } else {
            logger.error("No discount policy found for production company {}", event.getEventProductionCompanyID());
        }

        double priceAfterDiscountPolicy = pricePerSeat * amount;

        for (DiscountPolicy dp : allPolicies) {
            priceAfterDiscountPolicy = dp.calculateDiscount(priceAfterDiscountPolicy);
        }
        return priceAfterDiscountPolicy;
    }

    private void validatePurchasePolicy(int eventID, int ticketsAmount, String sessionToken) {

        int userAge = 0;
        if (authenticationService.isUserToken(sessionToken)) {
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            User user = userRepo.findByID(userID);
            // userAge = user.getAge(); TODO: add age to user and uncomment?
        }

        Set<PurchasePolicy> allPolicies = new HashSet<>();

        Event event = eventRepo.findByID(String.valueOf(eventID));
        Set<PurchasePolicy> purchasePolicy = event.getEventPurchasePolicy();
        Set<PurchasePolicy> companyPurchasePolicy = productionCompanyRepo
                .findByID(String.valueOf(event.getEventProductionCompanyID())).getPurchasePolicy();
        if (purchasePolicy != null) {
            allPolicies.addAll(purchasePolicy);
        } else {
            logger.warn("No purchase policy found for event {}", eventID);
        }

        if (companyPurchasePolicy != null) {
            allPolicies.addAll(companyPurchasePolicy);
        } else {
            logger.warn("No purchase policy found for production company {}", event.getEventProductionCompanyID());
        }

        for (PurchasePolicy pp : allPolicies) {
            try {
                if (pp instanceof LotteryPolicy) {
                    continue;
                }
                pp.validatePurchase(new PurchaseContext(userAge, ticketsAmount));
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

	public Result<Double> getOrderPrice(String orderId, String sTocken) {
		try {
			logger.info("OrderService.getOrderPrice: Attempting to get price for order {}.", orderId);

			logger.info("OrderService.getOrderPrice: Verifying session token for getting order price.");
			String subjectID = validateAssureNotAdminGetSubjectID(sTocken);
			logger.info("OrderService.getOrderPrice: Session token verified successfully.");

			Order order = orderRepo.findByID(orderId);

			logger.info("OrderService.getOrderPrice: verifying that order {} belongs to the user with the provided token for getting order price.", orderId);
			order.verifyBelongsToSubject(subjectID);

			double price = order.getTotalOrderprice();

			return Result.makeOk(price);
		} catch (AuthException e) {
			logger.error("OrderService.getOrderPrice: Authentication error during getting price for order {}: {}", orderId, e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("OrderService.getOrderPrice: Failed to get price for order {}: {}", orderId, e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("OrderService.getOrderPrice: Failed to get price for order {}: {}", orderId, e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (Exception e) {	
			logger.error("OrderService.getOrderPrice: Unexpected error during getting price for order {}: {}", orderId, e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
	public Result<Long> getActiveOrderTimeStamp(String orderId, String sessionToken) {
		try {
			logger.info("OrderService.getActiveOrderTimeStamp: Attempting to get timestamp for order {}.", orderId);

			logger.info("OrderService.getActiveOrderTimeStamp: Verifying session token for getting order timestamp.");
			String subjectID = validateAssureNotAdminGetSubjectID(sessionToken);
			logger.info("OrderService.getActiveOrderTimeStamp: Session token verified successfully.");

			Order order = orderRepo.findByID(orderId);

			logger.info("OrderService.getActiveOrderTimeStamp: verifying that order {} belongs to the user with the provided token for getting order timestamp.", orderId);
			order.verifyBelongsToSubject(subjectID);

			long timestamp = order.getOrderStartTime();
			
			return Result.makeOk(timestamp);
		} catch (AuthException e) {
			logger.error("OrderService.getActiveOrderTimeStamp: Authentication error during getting timestamp for order {}: {}", orderId, e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("OrderService.getActiveOrderTimeStamp: Failed to get timestamp for order {}: {}", orderId, e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("OrderService.getActiveOrderTimeStamp: Failed to get timestamp for order {}: {}", orderId, e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (Exception e) {	
			logger.error("OrderService.getActiveOrderTimeStamp: Unexpected error during getting timestamp for order {}: {}", orderId, e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
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

	private void safeTicketRevoke(String ticket)
	{
		if(ticket==null)
			return;
		try{
			ticketGateway.revokeTicket(ticket);
		}catch(Exception e)
		{
			logger.error("Ticket revoke failed for ticket {}",ticket,e);
		}
	}

	private void safeExternalRollback(Integer transactionId, String ticket)
	{
		safeRefund(transactionId);
		safeTicketRevoke(ticket);
	}
	
	
	private void rollbackSeatEditIfNeeded(
			Venue venue,
			boolean venueChanged,
			String segmentId,
			List<String> seatsToAdd,
			List<String> seatsToRemove,
			int eventID
	) {
		if (venue == null || !venueChanged) {
			return;
		}

		try {
			if (seatsToAdd != null && !seatsToAdd.isEmpty()) {
				venue.cancelSeatReservation(segmentId, seatsToAdd, eventID);
			}

			if (seatsToRemove != null && !seatsToRemove.isEmpty()) {
				venue.reserveSeats(ReservationRequest.forSeats(eventID, seatsToRemove, segmentId));
			}

			venueRepo.save(venue);
		} catch (Exception rollbackException) {
			logger.error(
					"OrderService.rollbackSeatEditIfNeeded: Failed to rollback seat edit for event {} segment {} added {} removed {}: {}",
					eventID,
					segmentId,
					seatsToAdd,
					seatsToRemove,
					rollbackException.getMessage()
			);
		}
	}

   private void rollbackFieldEditIfNeeded(
			Venue venue,
			boolean venueChanged,
			String segmentId,
			int oldNumOfTickets,
			int newNumOfTickets,
			int eventID
	) {
		if (venue == null || !venueChanged) {
			return;
		}

		try {
			if (newNumOfTickets > oldNumOfTickets) {
				int added = newNumOfTickets - oldNumOfTickets;
				venue.cancelFieldReservation(segmentId, added, eventID);
			} else if (newNumOfTickets < oldNumOfTickets) {
				int removed = oldNumOfTickets - newNumOfTickets;
				venue.reserveSeats(ReservationRequest.forField(eventID, removed, segmentId));
			}

			venueRepo.save(venue);
		} catch (Exception rollbackException) {
			logger.error(
					"OrderService.rollbackFieldEditIfNeeded: Failed to rollback field edit for event {} segment {} oldAmount {} newAmount {}: {}",
					eventID,
					segmentId,
					oldNumOfTickets,
					newNumOfTickets,
					rollbackException.getMessage()
			);
		}
	}
	
}
