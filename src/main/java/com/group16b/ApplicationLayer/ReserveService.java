package com.group16b.ApplicationLayer;

import java.util.List;
import java.util.Set;

import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchaseContext;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;

import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;

@Service
public class ReserveService {
    private static final Logger logger = LoggerFactory.getLogger(ReserveService.class);

    private final IRepository<Venue> venueRepo;
    private final IRepository<Order> orderRepo;
    private final IRepository<VirtualQueue> queueImp;
    private final IEventRepository eventRepository;
    private final IProductionCompanyRepository productionCompanyRepo;
    private final IAuthenticationService authenticationService;

    public ReserveService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepo, IRepository<VirtualQueue> queueImp, IRepository<Venue> venueRepo, IEventRepository eventRepository, IRepository<Order> orderRepo) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepo=productionCompanyRepo;
        this.venueRepo = venueRepo;
        this.queueImp = queueImp;
        this.eventRepository = eventRepository;
        this.orderRepo = orderRepo;
    }

    public Result<String> reserveSeats(String segmentId, List<String> seatIds, int eventID, String venueId, String sessionToken) {
        VirtualQueue q = null;
        String subjectID = null;
        try {
            logger.info("ReserveService.reserveSeats: Verifying session token for reservation.");
			subjectID = validateAssureNotAdminGetSubjectID(sessionToken);

			logger.info("ReserveService.reserveSeats: Session token verified successfully.");
            logger.info("ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);
            
            logger.info("Checking event is active");
            Event event = eventRepository.findByID(String.valueOf(eventID));
            event.validateEventIsActive();

            //2. System - validates the event does NOT have a lottery policy.

            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for {}", subjectID);
            event.verifyDoesNotHaveLotteryPolicy();


            logger.info("ReserveService.reserveSeats: Moving queue forward");
            q = queueImp.findByID(Integer.toString(eventID));
            q.addToQueue(subjectID);
            queueImp.save(q);
            logger.info("ReserveService.reserveSeats: Checking if user passed queue");

            q.validateUserPassedQueue(subjectID);

            logger.info("ReserveService.reserveSeats: {} is passed the queue");
            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            Venue venue = venueRepo.findByID(venueId);
            venue.reserveTickets(segmentId, seatIds, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Seats reserved seccessfully for {}", subjectID);

            Segment segment = venue.getSegmentByID(segmentId);
            double pricePerSeat = segment.getPrice(eventID);
            

            validatePurchasePolicy(eventID);

            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, seatIds.size());

            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, seatIds, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.save(order);
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
			logger.error("ReserveService.reserveSeats: JWT authentication error during event creation: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (AuthException e) {
			logger.error("ReserveService.reserveSeats: Authentication error during retrieving orders: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (IllegalStateException e) { 
			logger.error("ReserveService.reserveSeats: Illegal state encountered for order {} for user {}: {}", eventID, subjectID, e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail(e.getMessage());
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
            logger.info("ReserveService.reserveFieldSeats: Verifying session token for reservation.");
			
			subjectID = validateAssureNotAdminGetSubjectID(sessionToken);
			logger.info("ReserveService.reserveFieldSeats: Session token verified successfully.");

            logger.info("ReserveService.reserveFieldSeats: Attempting to reserve seats for {}", subjectID);
            
            logger.info("ReserveService.reserveFieldSeats: Checking event is active");
            Event event = eventRepository.findByID(String.valueOf(eventID));
            event.validateEventIsActive();

            //2. System - validates the event does NOT have a lottery policy.
            logger.info("ReserveService.reserveFieldSeats: Validating lottery for {}", subjectID);
            event.verifyDoesNotHaveLotteryPolicy();
            //1. System - Checks user passed the queue.
            logger.info("ReserveService.reserveFieldSeats: Moving queue forward");
            q = queueImp.findByID(Integer.toString(eventID));
            q.addToQueue(subjectID);
            queueImp.save(q);
            logger.info("ReserveService.reserveFieldSeats: Checking if user passed queue");
            q.validateUserPassedQueue(subjectID);
            logger.info("ReserveService.reserveFieldSeats: {} is passed the queue", subjectID);

            
            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            Venue venue = venueRepo.findByID(venueId);
            venue.reserveTickets(segmentId, amount, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Seats reserved successfully for {}", subjectID);

            // 5.5 calculate price of the order
            
            validatePurchasePolicy(eventID);

            Segment segment = venue.getSegmentByID(segmentId);
            double pricePerSeat = segment.getPrice(eventID);
            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, amount);


            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, amount, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.save(order);
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
			logger.error("ReserveService.reserveSeats: JWT authentication error during event creation: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (AuthException e) {
			logger.error("ReserveService.reserveSeats: Authentication error during retrieving orders: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (IllegalStateException e) { 
			logger.error("ReserveService.reserveSeats: Illegal state encountered for order {} for user {}: {}", eventID, subjectID, e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail(e.getMessage());
		}catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
    }
    
    public Result<String> reserveSeatsWithLottery(String segmentId, List<String> seatIds, int eventID, String venueId, String lotteryCode, String sessionToken) {
        // 0. log everything
        String subjectID = null;
        VirtualQueue q = null;
        try {
            logger.info("Verifying session token for reservation.");
			
			subjectID = validateAssureNotAdminGetSubjectID(sessionToken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);

            
            Event event = eventRepository.findByID(String.valueOf(eventID));
            logger.info("Checking event is active");

            event.validateEventIsActive();
            
            
            //2. System - validates the event does NOT have a lottery policy.
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for {}", subjectID);
            event.validateLotteryCode(lotteryCode);
            


            logger.info("Moving queue forward");
            q = queueImp.findByID(Integer.toString(eventID));
            q.addToQueue(subjectID);
            queueImp.save(q);
            logger.info("check if user passed queue");
            q.validateUserPassedQueue(subjectID);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: {} is passed the queue");


            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            Venue venue = venueRepo.findByID(venueId);
            venue.reserveTickets(segmentId, seatIds, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Seats reserved seccessfully for {}", subjectID);

            // 5.5 calculate price of the order
            Segment segment = venue.getSegmentByID(segmentId);
            double pricePerSeat = segment.getPrice(eventID);

            validatePurchasePolicy(eventID);

            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, seatIds.size());


            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, seatIds, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.save(order);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Active order {} created successfully for user {}", order.getOrderId(), subjectID);
            q.removePassed(subjectID);
            event.lotteryUseCode(lotteryCode);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (JwtException e) {
			logger.error("ReserveService.reserveSeats: JWT authentication error during event creation: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (AuthException e) {
			logger.error("ReserveService.reserveSeats: Authentication error during retrieving orders: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (IllegalStateException e) { 
			logger.error("ReserveService.reserveSeats: Illegal state encountered for order {} for user {}: {}", eventID, subjectID, e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail(e.getMessage());
		}catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            Event event = eventRepository.findByID(String.valueOf(eventID));

            if(event.hasLotteryPolicy()) {
                event.renewLotteryCode(lotteryCode);
            }
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<String> reserveFieldSeatsWithLottery(String segmentId, int amount, int eventID, String venueId, String lotteryCode, String sessionToken) {
           // 0. log everything
            String subjectID = null;
            VirtualQueue q = null;
        try {
            logger.info("Verifying session token for reservation.");
			
			subjectID = validateAssureNotAdminGetSubjectID(sessionToken);
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for {}", subjectID);
            
            logger.info("Checking event is active");
            Event event = eventRepository.findByID(String.valueOf(eventID));
            if (event == null) {
                logger.error("Event with ID {} not found", eventID);
                return Result.makeFail("Event not found");
            }
            event.validateEventIsActive();

            Segment segment = venueRepo.findByID(venueId).getSegmentByID(segmentId);      

            //2. System - validates the event does NOT have a lottery policy.
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for {}", subjectID);
            event.validateLotteryCode(lotteryCode);

            //1. System - Checks user passed the queue.
            logger.info("Moving queue forward");
            q = queueImp.findByID(Integer.toString(eventID));
            q.addToQueue(subjectID);
            queueImp.save(q);
            logger.info("check if user passed queue");
            q.validateUserPassedQueue(subjectID);

            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: {} is passed the queue", subjectID);
            Venue venue = venueRepo.findByID(venueId);
            venue.reserveTickets(segmentId, amount, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Seats reserved successfully for {}", subjectID);

            double pricePerSeat = segment.getPrice(eventID);

            validatePurchasePolicy(eventID);

            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, amount);

            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, amount, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.save(order);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Active order {} created successfully for {}", order.getOrderId(), subjectID);
            q.removePassed(subjectID);
            event.lotteryUseCode(lotteryCode);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (JwtException e) {
			logger.error("ReserveService.reserveSeats: JWT authentication error during event creation: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (AuthException e) {
			logger.error("ReserveService.reserveSeats: Authentication error during retrieving orders: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (IllegalStateException e) { 
			logger.error("ReserveService.reserveSeats: Illegal state encountered for order {} for user {}: {}", eventID, subjectID, e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail(e.getMessage());
		}catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            Event event = eventRepository.findByID(String.valueOf(eventID));
            if(event.hasLotteryPolicy()) {
                event.renewLotteryCode(lotteryCode);
            }
            queueRemovePassed(q, subjectID);
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }


    private double calculateDiscountPolicies(int eventID, double pricePerSeat, int amount) {
        Event event = eventRepository.findByID(String.valueOf(eventID));
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
        Event event = eventRepository.findByID(String.valueOf(eventID));
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
                try {
                    pp.validatePurchase(new PurchaseContext(0, 0));
                } catch (PurchasePolicyException e) {
                    logger.error("User did not meet purchase policy requirements");
                    throw new IllegalArgumentException("User did not meet purchase policy requirements");
                }
            }
    }

    private void queueRemovePassed(VirtualQueue q, String subjectID) {
        if (q != null && subjectID != null) {
            q.removePassed(subjectID);
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
    
}