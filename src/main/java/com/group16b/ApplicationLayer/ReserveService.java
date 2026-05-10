package com.group16b.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Event.IEventRepositoryMapImpl;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.Order.OrderRepository;
import com.group16b.DomainLayer.Venue.IVenueRepositoryImp;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueueImp;

public class ReserveService {
    private static final Logger logger = LoggerFactory.getLogger(ReserveService.class);

    private final IVenueRepositoryImp veuneRepo = IVenueRepositoryImp.getInstance();
    private final OrderRepository orderRepo = OrderRepository.getInstance();
    private final VirtualQueueImp queueImp = VirtualQueueImp.getInstance();
    private final IEventRepository eventRepo = IEventRepositoryMapImpl.getInstance();

    public Result<String> reserveSeats(int userId, String segmentId, List<String> seatIds, int eventID, String venueId, String sTocken) {
        // 0. log everything

        logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for user {}", userId);
        try {
            //1. System - Checks user passed the queue.
            if(!queueImp.isUserPassedQueue(userId, eventID)){
                logger.error("ApplicationLayer.ReserveService.reserveSeats: user did not pass the queue");
                return Result.makeFail("user did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveSeats: User {} is passed the queue", userId);
            //2. System - validates the event does NOT have a lottery policy.

            logger.info("ApplicationLayer.ReserveService.reserveSeats: Validating lottery for user {}", userId);
            if (eventRepo.getEventByID(eventID).HasLotteryPolicy()) {
                logger.error("ApplicationLayer.ReserveService.reserveSeats: User {} did not provide lottery keypass", userId);
                return Result.makeFail("User did not provide lottery keypass to reserve seats for this event");
            }

            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            veuneRepo.reserveTickets(venueId, segmentId, seatIds, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Seats reserved seccessfully for user {}", userId);

            // 5.5 calculate price of the order
            Venue venue = veuneRepo.getVenueByID(venueId);
            Segment segment = venue.getSegmentByID(segmentId);
            double pricePerSeat = segment.getPrice(eventID);
            // @TODO: Implement price calculation logic
            // @TODO check purchase policy //already checked lottery policy
            double priceAfterPurchasePolicy = pricePerSeat; // @TODO: Implement purchase policy logic

            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, seatIds, sTocken, priceAfterPurchasePolicy, eventID, userId);
            orderRepo.addOrder(order);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Active order {} created successfully for user {}", order.getOrderId(), userId);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: Failed to reserve seats for user {}: {}", userId, e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: An unexpected error occurred while reserving seats for user {}: {}", userId, e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    public Result<String> reserveFieldSeats(int userId, String segmentId, int amount, int eventID, String venueId, String sTocken) {
           // 0. log everything

        logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Attempting to reserve seats for user {}", userId);
        try {
            //1. System - Checks user passed the queue.
            if(!queueImp.isUserPassedQueue(userId, eventID)){
                throw new IllegalArgumentException("ApplicationLayer.ReserveService.reserveFieldSeats: user did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: User {} is passed the queue", userId);

            //2. System - validates the event does NOT have a lottery policy.
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Validating lottery for user {}", userId);
            if (eventRepo.getEventByID(eventID).HasLotteryPolicy()) {
                logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: User {} did not provide lottery keypass", userId);
                return Result.makeFail("User did not provide lottery keypass to reserve seats for this event");
            }

            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            veuneRepo.reserveTickets(venueId, segmentId, amount, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Seats reserved successfully for user {}", userId);

            // 5.5 calculate price of the order
             // 5.5 calculate price of the order
            Venue venue = veuneRepo.getVenueByID(venueId);
            Segment segment = venue.getSegmentByID(segmentId);
            double pricePerSeat = segment.getPrice(eventID);
            // @TODO: Implement price calculation logic
            // @TODO check purchase policy
            double priceAfterPurchasePolicy = pricePerSeat; // @TODO: Implement purchase policy logic

            //6. System - creates an active order for the user with the selected tickets.
            Order order = new Order(segmentId, amount, sTocken, priceAfterPurchasePolicy, eventID, userId);
            orderRepo.addOrder(order);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Active order {} created successfully for user {}", order.getOrderId(), userId);
            return Result.makeOk("new OrderId: " + order.getOrderId());
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: Failed to reserve seats for user {}: {}", userId, e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: An unexpected error occurred while reserving seats for user {}: {}", userId, e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }


    private void cancelOrder(String orderId) { // to call when order is expired
        // Logic to cancel the order, e.g., release reserved seats
        orderRepo.cancelOrder(orderId);
    }
    
}