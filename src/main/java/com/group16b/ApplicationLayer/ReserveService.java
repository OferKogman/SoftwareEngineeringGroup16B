package com.group16b.ApplicationLayer;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.IVirtualQueueRepository;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;

import io.jsonwebtoken.JwtException;

public class ReserveService {
    private static final Logger logger = LoggerFactory.getLogger(ReserveService.class);

    private final IVenueRepository venueRepo = VenueRepositoryMapImpl.getInstance();
    private final IOrderRepository orderRepo = OrderRepositoryMapImpl.getInstance();
    private final IVirtualQueueRepository queueImp = VirtualQueueRepositoryMapImpl.getInstance();
    private final IEventRepository eventRepository = EventRepositoryMapImpl.getInstance();
    private final IProductionCompanyRepository productionCompanyRepo;
    private final IAuthenticationService authenticationService;

    public ReserveService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepo) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepo=productionCompanyRepo;
    }

    public Result<String> reserveSeats(String segmentId, List<String> seatIds, int eventID, String venueId, String sessionToken) {
        VirtualQueue q = null;
        String subjectID = null;
        try {
            logger.info("Verifying session token for reservation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for reservation.");
                queueRemovePassed(q, subjectID);
				return Result.makeFail("Invalid session token.");
			}
            if (authenticationService.isAdminToken(sessionToken)) {
				logger.warn("Invalid session token provided for reservation.");
                queueRemovePassed(q, subjectID);
				return Result.makeFail("Invalid session token.");
			}
			subjectID = authenticationService.extractSubjectFromToken(sessionToken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);
            
            logger.info("Checking event is active");
            Event event = eventRepository.getEventByID(eventID);
            // TODO check event exists
            if (!event.getEventStatus()) {
                logger.error("Event is inactive");
                queueRemovePassed(q, subjectID);
                return Result.makeFail("Event is inactive");
            }

            //2. System - validates the event does NOT have a lottery policy.

            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for {}", subjectID);
            if (eventRepository.getEventByID(eventID).getLotteryPolicy() != null) {
                logger.error("ApplicationLayer.ReserveService.reserveSeats: {} did not provide lottery keypass");
                queueRemovePassed(q, subjectID);
                return Result.makeFail("User did not provide lottery keypass to reserve seats for this event");
            }
            logger.info("Moving queue forward");
            q = queueImp.findVirtualQueueById(eventID);
            q.addToQueue(subjectID);
            queueImp.saveVirtualQueue(q);
            logger.info("check if user passed queue");
            if(!q.isUserPassedQueue(subjectID)){
                logger.error("User did not pass the queue");
                queueRemovePassed(q, subjectID);
                return Result.makeFail("User did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveSeats: {} is passed the queue");
            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            venueRepo.reserveTickets(venueId, segmentId, seatIds, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Seats reserved seccessfully for {}", subjectID);

            // 5.5 calculate price of the order
            Venue venue = venueRepo.getVenueByID(venueId);
            if (venue == null) {
                logger.error("Venue with ID {} not found", venueId);
                queueRemovePassed(q, subjectID);
                return Result.makeFail("Venue not found");
            }
            Segment segment = venue.getSegmentByID(segmentId);
            if (segment == null) {
                logger.error("Segment with ID {} not found in venue {}", segmentId, venueId);
                queueRemovePassed(q, subjectID);
                return Result.makeFail("Segment not found");
            }

            double pricePerSeat = segment.getPrice(eventID);
            

            if (!validatePurchasePolicy(eventID)) {
                logger.error("Purchase policy validation failed for event {}", eventID);
                queueRemovePassed(q, subjectID);
                return Result.makeFail("Purchase policy validation failed for this event");
            }

            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, seatIds.size());


            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, seatIds, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.addOrder(order);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Active order {} created successfully for user {}", order.getOrderId(), subjectID);
            q.removePassed(subjectID);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: Failed to reserve seats for user: {}", e.getMessage());
            if (q != null && subjectID != null) {
                q.removePassed(subjectID);
            }
            return Result.makeFail(e.getMessage());
        }
        catch (JwtException e) {
			logger.error("JWT authentication error during event creation: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
        catch (NoSuchAlgorithmException e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: Cryptographic algorithm not found: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            return Result.makeFail("Cryptographic error occurred: " + e.getMessage());
        }catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    public Result<String> reserveFieldSeats(String segmentId, int amount, int eventID, String venueId, String sessionToken) {
        String subjectID = null;
        VirtualQueue q = null;
        try {
            logger.info("Verifying session token for reservation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for reservation.");
				return Result.makeFail("Invalid session token.");
			}
            if (authenticationService.isAdminToken(sessionToken)) {
				logger.warn("Invalid session token provided for reservation.");
				return Result.makeFail("Invalid session token.");
			}

            if (authenticationService.isAdminToken(sessionToken)){
                return Result.makeFail("Admin can't reserve Tickets");
            }
			subjectID = authenticationService.extractSubjectFromToken(sessionToken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);
            
            logger.info("Checking event is active");
            Event event = eventRepository.getEventByID(eventID);
            if (!event.getEventStatus()) {
                logger.error("Event is inactive");
                return Result.makeFail("Event is inactive");
            }

            //2. System - validates the event does NOT have a lottery policy.
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Validating lottery for {}", subjectID);
            if (eventRepository.getEventByID(eventID).getLotteryPolicy() != null) {
                logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: {} did not provide lottery keypass", subjectID);
                return Result.makeFail("User did not provide lottery keypass to reserve seats for this event");
            }
            //1. System - Checks user passed the queue.
            logger.info("Moving queue forward");
            q = queueImp.findVirtualQueueById(eventID);
            q.addToQueue(subjectID);
            queueImp.saveVirtualQueue(q);
            logger.info("check if user passed queue");
            if(!q.isUserPassedQueue(subjectID)){
                logger.error("User did not pass the queue");
                queueRemovePassed(q, subjectID);
                return Result.makeFail("User did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: {} is passed the queue", subjectID);


            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            venueRepo.reserveTickets(venueId, segmentId, amount, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Seats reserved successfully for {}", subjectID);

            // 5.5 calculate price of the order
            Venue venue = venueRepo.getVenueByID(venueId);
            if (venue == null) {
                logger.error("Venue with ID {} not found", venueId);
                queueRemovePassed(q, subjectID);
                return Result.makeFail("Venue not found");
            }
            Segment segment = venue.getSegmentByID(segmentId);
            if (segment == null) {
                logger.error("Segment with ID {} not found in venue {}", segmentId, venueId);
                queueRemovePassed(q, subjectID);
                return Result.makeFail("Segment not found");
            }
            double pricePerSeat = segment.getPrice(eventID);
            Set<DiscountPolicy> discountPolicy = event.getEventDiscountPolicy();
            if (discountPolicy == null) {
                logger.error("No discount policy found for event {}", eventID);
                queueRemovePassed(q, subjectID);
                return Result.makeFail("No discount policy found for this event");
            }

            if (!validatePurchasePolicy(eventID)) {
                logger.error("Purchase policy validation failed for event {}", eventID);
                queueRemovePassed(q, subjectID);
                return Result.makeFail("Purchase policy validation failed for this event");
            }

            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, amount);


            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, amount, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.addOrder(order);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Active order {} created successfully for {}", order.getOrderId(), subjectID);
            q.removePassed(subjectID);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: Failed to reserve seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            return Result.makeFail(e.getMessage());
        }
        catch (JwtException e) {
			logger.error("JWT authentication error during event creation: " + e.getMessage());
			queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
        catch (NoSuchAlgorithmException e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: Cryptographic algorithm not found: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            return Result.makeFail("Cryptographic error occurred: " + e.getMessage());
        }catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    public Result<String> reserveSeatsWithLottery(String segmentId, List<String> seatIds, int eventID, String venueId, String lotteryCode, String sessionToken) {
        // 0. log everything
        String subjectID = null;
        VirtualQueue q = null;
        LotteryPolicy lotteryPolicy = null;
        try {
            logger.info("Verifying session token for reservation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for reservation.");
				return Result.makeFail("Invalid session token.");
			}
            if (authenticationService.isAdminToken(sessionToken)) {
				logger.warn("Invalid session token provided for reservation.");
				return Result.makeFail("Invalid session token.");
			}
			subjectID = authenticationService.extractSubjectFromToken(sessionToken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);

            
            Event event = eventRepository.getEventByID(eventID);
            if (event == null) {
                logger.error("Event with ID {} not found", eventID);
                return Result.makeFail("Event not found");
            }
            logger.info("Checking event is active");

            if (!event.getEventStatus()) {
                logger.error("Event is inactive");
                return Result.makeFail("Event is inactive");
            }
            
            
            //2. System - validates the event does NOT have a lottery policy.
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for {}", subjectID);
            lotteryPolicy = eventRepository.getEventByID(eventID).getLotteryPolicy();
            if (lotteryPolicy == null) {
                logger.error("ApplicationLayer.ReserveService.reserveSeats: no keypass required for {}", eventID);
                return Result.makeFail("User provided lottery keypass to reserve seats for event that does not have lottery policy.");
            }
            lotteryPolicy.validateLotteryCode(lotteryCode);

            logger.info("Moving queue forward");
            q = queueImp.findVirtualQueueById(eventID);
            q.addToQueue(subjectID);
            queueImp.saveVirtualQueue(q);
            logger.info("check if user passed queue");
            if(!q.isUserPassedQueue(subjectID)){
                logger.error("User did not pass the queue");
                queueRemovePassed(q, subjectID);
                return Result.makeFail("User did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveSeats: {} is passed the queue");


            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            venueRepo.reserveTickets(venueId, segmentId, seatIds, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Seats reserved seccessfully for {}", subjectID);

            // 5.5 calculate price of the order
            Venue venue = venueRepo.getVenueByID(venueId);
            Segment segment = venue.getSegmentByID(segmentId);
            double pricePerSeat = segment.getPrice(eventID);

            if (!validatePurchasePolicy(eventID)) {
                queueRemovePassed(q, subjectID);
                logger.error("Purchase policy validation failed for event {}", eventID);
                return Result.makeFail("Purchase policy validation failed for this event");
            }

            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, seatIds.size());


            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, seatIds, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.addOrder(order);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Active order {} created successfully for user {}", order.getOrderId(), subjectID);
            q.removePassed(subjectID);
            lotteryPolicy.useCode(lotteryCode);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: Failed to reserve seats for user: {}", e.getMessage());
            if(lotteryPolicy != null) {
                lotteryPolicy.renewLotteryCode(lotteryCode);
            }
            queueRemovePassed(q, subjectID);
            return Result.makeFail(e.getMessage());
        }
        catch (JwtException e) {
			logger.error("JWT authentication error during event creation: " + e.getMessage());
			queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
        catch (NoSuchAlgorithmException e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: Cryptographic algorithm not found: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            if(lotteryPolicy != null) {
                lotteryPolicy.renewLotteryCode(lotteryCode);
            }
            return Result.makeFail("Cryptographic error occurred: " + e.getMessage());
        }catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            if(lotteryPolicy != null) {
                lotteryPolicy.renewLotteryCode(lotteryCode);
            }
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<String> reserveFieldSeatsWithLottery(String segmentId, int amount, int eventID, String venueId, String lotteryCode, String sessionToken) {
           // 0. log everything
            LotteryPolicy lotteryPolicy = null;
            String subjectID = null;
            VirtualQueue q = null;
        try {
            logger.info("Verifying session token for reservation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for reservation.");
				return Result.makeFail("Invalid session token.");
			}
            if (authenticationService.isAdminToken(sessionToken)) {
				logger.warn("Invalid session token provided for reservation.");
				return Result.makeFail("Invalid session token.");
			}

            if (authenticationService.isAdminToken(sessionToken)){
                return Result.makeFail("Admin can't reserve Tickets");
            }
			subjectID = authenticationService.extractSubjectFromToken(sessionToken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);
            
            logger.info("Checking event is active");
            Event event = eventRepository.getEventByID(eventID);
            if (event == null) {
                logger.error("Event with ID {} not found", eventID);
                return Result.makeFail("Event not found");
            }
            if (!event.getEventStatus()) {
                logger.error("Event is inactive");
                return Result.makeFail("Event is inactive");
            }
            Segment segment = venueRepo.getVenueByID(venueId).getSegmentByID(segmentId);
            if (segment == null) {
                logger.error("Segment with ID {} not found in venue {}", segmentId, venueId);
                return Result.makeFail("Segment not found");
            }            

            //2. System - validates the event does NOT have a lottery policy.
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for {}", subjectID);
            lotteryPolicy = eventRepository.getEventByID(eventID).getLotteryPolicy();
            if (lotteryPolicy == null) {
                logger.error("ApplicationLayer.ReserveService.reserveSeats: no keypass required for {}", eventID);
                return Result.makeFail("User provided lottery keypass to reserve seats for event that does not have lottery policy.");
            }
            lotteryPolicy.validateLotteryCode(lotteryCode);

            //1. System - Checks user passed the queue.
            logger.info("Moving queue forward");
            q = queueImp.findVirtualQueueById(eventID);
            q.addToQueue(subjectID);
            queueImp.saveVirtualQueue(q);
            logger.info("check if user passed queue");
            if(!q.isUserPassedQueue(subjectID)){
                logger.error("User did not pass the queue");
                queueRemovePassed(q, subjectID);
                return Result.makeFail("User did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: {} is passed the queue", subjectID);

            venueRepo.reserveTickets(venueId, segmentId, amount, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Seats reserved successfully for {}", subjectID);

            double pricePerSeat = segment.getPrice(eventID);

            if (!validatePurchasePolicy(eventID)) {
                logger.error("Purchase policy validation failed for event {}", eventID);
                queueRemovePassed(q, subjectID);
                return Result.makeFail("Purchase policy validation failed for this event");
            }

            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, amount);

            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, amount, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.addOrder(order);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Active order {} created successfully for {}", order.getOrderId(), subjectID);
            q.removePassed(subjectID);
            lotteryPolicy.useCode(lotteryCode);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: Failed to reserve seats for user: {}", e.getMessage());
            if(lotteryPolicy != null) {
                lotteryPolicy.renewLotteryCode(lotteryCode);
            }
            queueRemovePassed(q, subjectID);
            return Result.makeFail(e.getMessage());
        }
        catch (JwtException e) {
			logger.error("JWT authentication error during event creation: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
        catch (NoSuchAlgorithmException e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: Cryptographic algorithm not found: {}", e.getMessage());
            if(lotteryPolicy != null) {
                lotteryPolicy.renewLotteryCode(lotteryCode);
            }
            queueRemovePassed(q, subjectID);
            return Result.makeFail("Cryptographic error occurred: " + e.getMessage());
        }catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            if(lotteryPolicy != null) {
                lotteryPolicy.renewLotteryCode(lotteryCode);
            }
            queueRemovePassed(q, subjectID);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }


    private double calculateDiscountPolicies(int eventID, double pricePerSeat, int amount) {
        Event event = eventRepository.getEventByID(eventID);
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
    private boolean validatePurchasePolicy(int eventID) {
        Event event = eventRepository.getEventByID(eventID);
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
                    return false;
                }
            }
            return true;
    }

    private void queueRemovePassed(VirtualQueue q, String subjectID) {
        if (q != null && subjectID != null) {
            q.removePassed(subjectID);
        }
    }
    
}