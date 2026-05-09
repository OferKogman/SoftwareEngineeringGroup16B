package com.group16b.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.DomainLayer.Order.OrderRepository;
import com.group16b.DomainLayer.Venue.IVenueRepositoryImp;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueueImp;

public class ReserveService {
    private static final Logger logger = LoggerFactory.getLogger(ReserveService.class);

    private final IVenueRepositoryImp veuneRepo = IVenueRepositoryImp.getInstance();
    private final OrderRepository orderRepo = OrderRepository.getInstance();
    private final VirtualQueueImp queueImp = VirtualQueueImp.getInstance();
    // search criteria, selected event, selected tickts

    /*
        1. System - Checks user passed the queue.
        2. System - validates the user won the lottery if required.
        3. System - validates selected seats exist.
        4. System - validates selected seats are available.
        5. System - removes selected seats from stock.
        6. System - creates an active order for the user with the selected tickets.

    */
    public Result<String> reserveSeats(int userId, String segmentId, List<String> seatIds, int eventID, String venueId) {
        // 0. log everything

        logger.info("ApplicationLayer.ReserveService.reserveSeats: Attempting to reserve seats for user {}", userId);
        try {
            //1. System - Checks user passed the queue.
            if(!queueImp.isUserPassedQueue(userId)){
                throw new IllegalArgumentException("ApplicationLayer.ReserveService.reserveSeats: user did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveSeats: User {} is passed the queue", userId);
            //2. System - validates the user won the lottery if required.
            // TODO: Implement lottery validation logic

            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            veuneRepo.reserveTickets(venueId, segmentId, seatIds, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Seats reserved seccessfully for user {}", userId);

            //6. System - creates an active order for the user with the selected tickets.
            String orderId = orderRepo.createSeatingActiveOrder(seatIds, segmentId, eventID, userId);
            logger.info("ApplicationLayer.ReserveService.reserveSeats: Active order {} created successfully for user {}", orderId, userId);
            return Result.makeOk("new OrderId: " + orderId);
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
    // needed or only one?
    public Result<String> reserveFieldSeats(int userId, String segmentId, int amount, int eventID, String venueId) {
           // 0. log everything

        logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Attempting to reserve seats for user {}", userId);
        try {
            //1. System - Checks user passed the queue.
            if(!queueImp.isUserPassedQueue(userId)){
                throw new IllegalArgumentException("ApplicationLayer.ReserveService.reserveFieldSeats: user did not pass the queue");
            }
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: User {} is passed the queue", userId);

            //2. System - validates the user won the lottery if required.
            // TODO: Implement lottery validation logic

            //3. System - validates selected seats exist.
            //4. System - validates selected seats are available.
            //5. System - removes selected seats from stock.
            veuneRepo.reserveTickets(venueId, segmentId, amount, eventID);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Seats reserved successfully for user {}", userId);

            //6. System - creates an active order for the user with the selected tickets.
            String orderId = orderRepo.createFieldActiveOrder(amount, segmentId, eventID, userId);
            logger.info("ApplicationLayer.ReserveService.reserveFieldSeats: Active order {} created successfully for user {}", orderId, userId);
            return Result.makeOk("new OrderId: " + orderId);
        }
        catch (IllegalArgumentException e) {
            logger.error("ApplicationLayer.ReserveService.reserveFieldSeats: Failed to reserve seats for user {}: {}", userId, e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
            logger.error("ApplicationLayer.ReserveService.reserveSeats: An unexpected error occurred while reserving seats for user {}: {}", userId, e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }


    private void cancelOrder(String orderId) { // to call when order is expired
        // Logic to cancel the order, e.g., release reserved seats
        orderRepo.removeOrder(orderId);
    }
    
}