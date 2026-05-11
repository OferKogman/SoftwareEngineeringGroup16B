package com.group16b.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.IVirtualQueueRepository;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImp;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;

import io.jsonwebtoken.JwtException;

public class ReserveService {
    private static final Logger logger = LoggerFactory.getLogger(ReserveService.class);

    private final VenueRepositoryMapImp veuneRepo = VenueRepositoryMapImp.getInstance();
    private final OrderRepositoryMapImpl orderRepo = OrderRepositoryMapImpl.getInstance();
    private final IVirtualQueueRepository queueImp = VirtualQueueRepositoryMapImpl.getInstance();
    private final IUserRepository userRepository = UserRepositoryMapImpl.getInstance();
    private final IEventRepository eventRepository = EventRepositoryMapImpl.getInstance();
    private final IAuthenticationService authenticationService;

    public ReserveService(IAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public Result<String> reserveSeats(String segmentId, List<String> seatIds, int eventID, String venueId, String sessionToken) {
        // 0. log everything

        try {
            logger.info("Verifying session token for event creation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event creation.");
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for user {}", user.getUserID());
            
            logger.info("Checking event is active");
            Event event = eventRepository.getEventByID(eventID);
            if (!event.getEventStatus()) {
                logger.error("Event is inactive");
                return Result.makeFail("Event is inactive");
            }
            // @TODO check purchase policy
            logger.info("Moving queue forward");
            VirtualQueue q = queueImp.findVirtualQueueById(eventID);
            q.addToQueue(sessionToken);
            q.popFirstIn();
            queueImp.saveVirtualQueue(q);
            logger.info("check if user passed queue");
            if(!q.isUserPassedQueue(sessionToken)){
                logger.error("User did not pass the queue");
                return Result.makeFail("User did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveSeats: User {} is passed the queue", user.getUserID());
            //2. System - validates the event does NOT have a lottery policy.

            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for user {}", user.getUserID());
            if (eventRepository.getEventByID(eventID).getLotteryPolicy() != null) {
                logger.error("ApplicationLayer.ReserveService.reserveSeats: User {} did not provide lottery keypass", user.getUserID());
                return Result.makeFail("User did not provide lottery keypass to reserve seats for this event");
            }
            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            veuneRepo.reserveTickets(venueId, segmentId, seatIds, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Seats reserved seccessfully for user {}", user.getUserID());

            // 5.5 calculate price of the order
            Venue venue = veuneRepo.getVenueByID(venueId);
            Segment segment = venue.getSegmentByID(segmentId);
            double pricePerSeat = segment.getPrice(eventID);
            // @TODO: Implement price calculation logic
            // @TODO check purchase policy //already checked lottery policy
            double priceAfterPurchasePolicy = pricePerSeat; // @TODO: Implement purchase policy logic

            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, seatIds, sessionToken, priceAfterPurchasePolicy, eventID, user.getUserID());
            orderRepo.addOrder(order);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Active order {} created successfully for user {}", order.getOrderId(), user.getUserID());
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: Failed to reserve seats for user: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (JwtException e) {
			logger.error("JWT authentication error during event creation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
        catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    public Result<String> reserveFieldSeats(String segmentId, int amount, int eventID, String venueId, String sessionToken) {
           // 0. log everything

           try {
            logger.info("Verifying session token for event creation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event creation.");
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
			logger.info("Session token verified successfully.");
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Attempting to reserve seats for user {}", user.getUserID());
            logger.info("Checking event is active");
            Event event = eventRepository.getEventByID(eventID);
            if (!event.getEventStatus()) {
                logger.error("Event is inactive");
                return Result.makeFail("Event is inactive");
            }
            // @TODO check purchase policy
            //1. System - Checks user passed the queue.
            logger.info("Moving queue forward");
            VirtualQueue q = queueImp.findVirtualQueueById(eventID);
            q.addToQueue(sessionToken);
            q.popFirstIn();
            queueImp.saveVirtualQueue(q);
            logger.info("check if user passed queue");
            if(!q.isUserPassedQueue(sessionToken)){
                logger.error("User did not pass the queue");
                return Result.makeFail("User did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: User {} is passed the queue", user.getUserID());

            //2. System - validates the event does NOT have a lottery policy.
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Validating lottery for user {}", user.getUserID());
            if (eventRepository.getEventByID(eventID).getLotteryPolicy() != null) {
                logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: User {} did not provide lottery keypass", user.getUserID());
                return Result.makeFail("User did not provide lottery keypass to reserve seats for this event");
            }

            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            veuneRepo.reserveTickets(venueId, segmentId, amount, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Seats reserved successfully for user {}", user.getUserID());

            // 5.5 calculate price of the order
             // 5.5 calculate price of the order
            Venue venue = veuneRepo.getVenueByID(venueId);
            Segment segment = venue.getSegmentByID(segmentId);
            double pricePerSeat = segment.getPrice(eventID);
            // @TODO: Implement price calculation logic
            // purchase policy //already checked lottery policy
            double priceAfterPurchasePolicy = pricePerSeat; // @TODO: Implement purchase policy logic

            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, amount, sessionToken, priceAfterPurchasePolicy, eventID, user.getUserID());
            orderRepo.addOrder(order);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Active order {} created successfully for user {}", order.getOrderId(), user.getUserID());
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: Failed to reserve seats for user: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (JwtException e) {
			logger.error("JWT authentication error during event creation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
        catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: An unexpected error occurred while reserving seats for user: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }


    private void cancelOrder(String orderId) { // to call when order is expired
        // Logic to cancel the order, e.g., release reserved seats
        orderRepo.cancelOrder(orderId);
    }
    
}