package com.group16b.ApplicationLayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchaseContext;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicyException;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;

import io.jsonwebtoken.JwtException;

@Service
public class ReserveService {
    private static final Logger logger = LoggerFactory.getLogger(ReserveService.class);

    private final IRepository<Venue> venueRepo;
    private final IRepository<Order> orderRepo;
    private final IRepository<VirtualQueue> queueImp;
    private final IEventRepository eventRepository;
    private final IProductionCompanyRepository productionCompanyRepo;
    private final IAuthenticationService authenticationService;
    private final IRepository<User> userRepo;

    public ReserveService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepo, IRepository<VirtualQueue> queueImp, IRepository<Venue> venueRepo, IEventRepository eventRepository, IRepository<Order> orderRepo, IRepository<User> userRepo) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepo=productionCompanyRepo;
        this.venueRepo = venueRepo;
        this.queueImp = queueImp;
        this.eventRepository = eventRepository;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
    }
    public Result<String> reserveSeats(String segmentId, List<String> seatIds, int eventID, String venueId, String sessionToken) {
        return this.reserveSeatsWithLottery(segmentId, seatIds, eventID, venueId, "", sessionToken);
    }
    public Result<String> reserveFieldSeats(String segmentId, int amount, int eventID, String venueId, String sessionToken) {
        return this.reserveFieldSeatsWithLottery(segmentId, amount, eventID, venueId, "", sessionToken);
    }

    public Result<String> reserveSeatsWithLottery(String segmentId, List<String> seatIds, int eventID, String venueId, String lotteryCode, String sessionToken) {
        VirtualQueue q = null;
        String subjectID = null;
        Venue venue = null;
        boolean seatsReserved = false;
        Event event = null;
        boolean lotteryCodeUsed = false;
        try {
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Starting seat reservation process for eventID {} and user session {}", eventID, sessionToken);
            if (seatIds == null || seatIds.isEmpty() || segmentId == null || segmentId.isEmpty() || venueId == null || venueId.isEmpty()) {
                logger.warn("ApplicationLayer.ReserveService.reserveSeats: Invalid input parameters for seat reservation. segmentId: {}, seatIds: {}, venueId: {}", segmentId, seatIds, venueId);
                return Result.makeFail("Invalid input parameters for seat reservation");
            }
            
            logger.info("ReserveService.reserveSeats: Verifying session token for reservation.");
			subjectID = validateAssureNotAdminGetSubjectID(sessionToken);

            
            logger.info("Checking event is active");
            event = eventRepository.findByID(String.valueOf(eventID));
            event.validateEventIsActive();

            //2. System - validates the event does NOT have a lottery policy.

            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for {}", subjectID);
            validateLottery(event, lotteryCode);


            logger.info("ReserveService.reserveSeats: Moving queue forward");
            q = queueImp.findByID(Integer.toString(eventID));
            q.addToQueue(subjectID);
            queueImp.save(q);
            logger.info("ReserveService.reserveSeats: Checking if user passed queue");

            q.validateUserPassedQueue(subjectID);

            logger.info("ReserveService.reserveSeats: {} is passed the queue", subjectID);
            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            venue = venueRepo.findByID(venueId);
            
            validatePurchasePolicy(eventID, seatIds.size(), sessionToken);
            
            double pricePerSeat = venue.getPriceForSegment(segmentId, eventID); // validate segment exists and is valid for the event before reserving seats
            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, seatIds.size());
            
            logger.info("ReserveService.reserveSeats: Attempting to reserve seats {} in segment {} for event {} for user {}", seatIds, segmentId, eventID, subjectID);
            venue.reserveTickets(segmentId, seatIds, eventID);
            seatsReserved = true;
            venueRepo.save(venue);

            //6. System - creates an active order for the user with the selected tickets.
            useLotteryCodeIfNeeded(event, lotteryCode);
            lotteryCodeUsed = hasLotteryCode(lotteryCode);
            Order order = new Order(segmentId, seatIds, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.save(order);

            logger.info("ApplicationLayer.ReserveService.reserveSeats: Active order {} created successfully for user {}", order.getOrderId(), subjectID);
            q.removePassed(subjectID);
            queueImp.save(q);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: Failed to reserve seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            rollbackSeatReservationIfNeeded(venue, seatsReserved, segmentId, seatIds, eventID);
            if (lotteryCodeUsed) {renewLotteryCodeIfNeeded(event, lotteryCode);}
            return Result.makeFail("Illegal argument: " + e.getMessage());
        }
        catch (JwtException e) {
			logger.error("ReserveService.reserveSeats: JWT authentication error during event creation: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (AuthException e) {
			logger.error("ReserveService.reserveSeats: Authentication error during retrieving orders: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (OptimisticLockingFailureException e){
            logger.error("ReserveService.reserveSeats: Concurrent modification detected for order {} for user {}: {}", eventID, subjectID, e.getMessage());
            rollbackSeatReservationIfNeeded(venue, seatsReserved, segmentId, seatIds, eventID);
            queueRemovePassed(q, subjectID);
            if (lotteryCodeUsed) {renewLotteryCodeIfNeeded(event, lotteryCode);}
            return Result.makeFail("Optimistic locking failure: " + e.getMessage());
		} catch (IllegalStateException e) { 
			logger.error("ReserveService.reserveSeats: Illegal state encountered for order {} for user {}: {}", eventID, subjectID, e.getMessage());
            rollbackSeatReservationIfNeeded(venue, seatsReserved, segmentId, seatIds, eventID);
            queueRemovePassed(q, subjectID);
            if (lotteryCodeUsed) {renewLotteryCodeIfNeeded(event, lotteryCode);}
			return Result.makeFail("Illegal state encountered: " + e.getMessage());
        }catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            rollbackSeatReservationIfNeeded(venue, seatsReserved, segmentId, seatIds, eventID);
            if (lotteryCodeUsed) {renewLotteryCodeIfNeeded(event, lotteryCode);}
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
    }
    
    public Result<String> reserveFieldSeatsWithLottery(String segmentId, int amount, int eventID, String venueId, String lotteryCode, String sessionToken) {
        String subjectID = null;
        VirtualQueue q = null;
        Venue venue = null;
        boolean seatsReserved = false;
        Event event = null;
        boolean lotteryCodeUsed = false;
        try {
            logger.info("ReserveService.reserveFieldSeats: Starting field seat reservation process for eventID {} and user session {}", eventID, sessionToken);
            if (amount <= 0 || segmentId == null || segmentId.isEmpty() || venueId == null || venueId.isEmpty()) {
                logger.warn("ReserveService.reserveFieldSeats: Invalid input parameters for field seat reservation. segmentId: {}, amount: {}, venueId: {}", segmentId, amount, venueId);
                return Result.makeFail("Invalid input parameters for field reservation");
            }
            logger.info("ReserveService.reserveFieldSeats: Verifying session token for reservation.");
			subjectID = validateAssureNotAdminGetSubjectID(sessionToken);
            
            logger.info("ReserveService.reserveFieldSeats: Checking event is active");
            event = eventRepository.findByID(String.valueOf(eventID));
            event.validateEventIsActive();

            logger.info("ReserveService.reserveFieldSeats: Validating lottery for {}", subjectID);
            validateLottery(event, lotteryCode);

            logger.info("ReserveService.reserveFieldSeats: Moving queue forward");
            q = queueImp.findByID(Integer.toString(eventID));
            q.addToQueue(subjectID);
            queueImp.save(q);
            logger.info("ReserveService.reserveFieldSeats: Checking if user passed queue");
            q.validateUserPassedQueue(subjectID);
            logger.info("ReserveService.reserveFieldSeats: {} is passed the queue", subjectID);

            
            validatePurchasePolicy(eventID, amount, sessionToken);
            
            venue = venueRepo.findByID(venueId);
            
            double pricePerSeat = venue.getPriceForSegment(segmentId, eventID);
            double priceAfterDiscountPolicy = calculateDiscountPolicies(eventID, pricePerSeat, amount);
            
            
            venue.reserveTickets(segmentId, amount, eventID);
            seatsReserved = true;
            venueRepo.save(venue);
            logger.info("ReserveService.reserveFieldSeats: Seats reserved successfully for {}", subjectID);

            //6. System - creates an active order for the user with the selected tickets.
            useLotteryCodeIfNeeded(event, lotteryCode);
            lotteryCodeUsed = hasLotteryCode(lotteryCode);
            
            Order order = new Order(segmentId, amount, priceAfterDiscountPolicy, eventID, subjectID);
            orderRepo.save(order);
            logger.info("ReserveService.reserveFieldSeats: Active order {} created successfully for {}", order.getOrderId(), subjectID);
            

            
            q.removePassed(subjectID);
            queueImp.save(q);


            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ReserveService.reserveFieldSeats: Failed to reserve seats for user: {}", e.getMessage());
            queueRemovePassed(q, subjectID);
            rollbackFieldReservationIfNeeded(venue, seatsReserved, segmentId, amount, eventID);
            if (lotteryCodeUsed) {renewLotteryCodeIfNeeded(event, lotteryCode);}
            return Result.makeFail("Illegal argument: " + e.getMessage());
        }
        catch (JwtException e) {
			logger.error("ReserveService.reserveFieldSeats: JWT authentication error during event creation: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
        } catch (AuthException e) {
			logger.error("ReserveService.reserveFieldSeats: Authentication error during retrieving orders: " + e.getMessage());
            queueRemovePassed(q, subjectID);
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (OptimisticLockingFailureException e) {
            logger.error("ReserveService.reserveFieldSeats: Optimistic locking failure occurred while reserving seats for user: {}", e.getMessage());
            rollbackFieldReservationIfNeeded(venue, seatsReserved, segmentId, amount, eventID);
            queueRemovePassed(q, subjectID);
            if (lotteryCodeUsed) {renewLotteryCodeIfNeeded(event, lotteryCode);}
            return Result.makeFail("Optimistic locking failure: " + e.getMessage());
		} catch (IllegalStateException e) { 
			logger.error("ReserveService.reserveFieldSeats: Illegal state encountered for order {} for user {}: {}", eventID, subjectID, e.getMessage());
            rollbackFieldReservationIfNeeded(venue, seatsReserved, segmentId, amount, eventID);
            queueRemovePassed(q, subjectID);
            if (lotteryCodeUsed) {renewLotteryCodeIfNeeded(event, lotteryCode);}
			return Result.makeFail("Illegal state encountered: " + e.getMessage());
        }catch (Exception e) {
            logger.error("ReserveService.reserveFieldSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            rollbackFieldReservationIfNeeded(venue, seatsReserved, segmentId, amount, eventID);
            queueRemovePassed(q, subjectID);
            if (lotteryCodeUsed) {renewLotteryCodeIfNeeded(event, lotteryCode);}
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
    }
    


    private double calculateDiscountPolicies(int eventID, double pricePerSeat, int amount) {
        Event event = eventRepository.findByID(String.valueOf(eventID));
        Set<DiscountPolicy> discountPolicy = event.getEventDiscountPolicy();
        Set<DiscountPolicy> companyDiscountPolicy = productionCompanyRepo.findByID(String.valueOf(event.getEventProductionCompanyID())).getDiscountPolicy();
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
            //userAge = user.getAge(); TODO: add age to user and uncomment?
        }

        Set<PurchasePolicy> allPolicies = new HashSet<>();

        Event event = eventRepository.findByID(String.valueOf(eventID));
        Set<PurchasePolicy> purchasePolicy = event.getEventPurchasePolicy();
        Set<PurchasePolicy> companyPurchasePolicy = productionCompanyRepo.findByID(String.valueOf(event.getEventProductionCompanyID())).getPurchasePolicy();
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

    private void queueRemovePassed(VirtualQueue q, String subjectID) {
        if (q != null && subjectID != null) {
            q.removePassed(subjectID);
            queueImp.save(q);
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
    private void rollbackSeatReservationIfNeeded(Venue venue, boolean seatsReserved, String segmentId, List<String> seatIds, int eventID) {
            if (venue != null && seatsReserved) {
                    try {
                            venue.cancelSeatReservation(segmentId, seatIds, eventID);
                            venueRepo.save(venue);
                    } catch (Exception rollbackException) {
                            logger.error("ReserveService.rollbackSeatReservationIfNeeded: Failed to rollback seat reservation for event {} segment {} seats {}: {}", eventID, segmentId, seatIds, rollbackException.getMessage());
                    }
            }
    }
    private void rollbackFieldReservationIfNeeded(Venue venue, boolean seatsReserved, String segmentId, int amount, int eventID) {
        if (venue != null && seatsReserved) {
            try{
                venue.cancelFieldReservation(segmentId, amount, eventID);
                venueRepo.save(venue);
            } catch (Exception rollbackException) {
                logger.error("ReserveService.rollbackFieldReservationIfNeeded: Failed to rollback field reservation for event {} segment {} amount {}: {}", eventID, segmentId, amount, rollbackException.getMessage());
            }
        }
    }
    private boolean hasLotteryCode(String lotteryCode) {
    return lotteryCode != null && !lotteryCode.isEmpty();
}

    private void validateLottery(Event event, String lotteryCode) {
        if (hasLotteryCode(lotteryCode)) {
            event.validateLotteryCode(lotteryCode);
        } else {
            event.verifyDoesNotHaveLotteryPolicy();
        }
    }

    private void useLotteryCodeIfNeeded(Event event, String lotteryCode) {
        if (hasLotteryCode(lotteryCode)) {
            event.lotteryUseCode(lotteryCode);
            eventRepository.save(event);
        }
    }

    private void renewLotteryCodeIfNeeded(Event event, String lotteryCode) {
        if (event != null && hasLotteryCode(lotteryCode)) {
            event.renewLotteryCode(lotteryCode);
            eventRepository.save(event);
        }
    }
}