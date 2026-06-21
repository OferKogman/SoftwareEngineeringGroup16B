package com.group16b.ApplicationLayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.DTOs.VenueDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.ChosenSeatingSegRecord;
import com.group16b.ApplicationLayer.Records.FieldSegRecord;
import com.group16b.ApplicationLayer.Records.VenueRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Venue;

import io.jsonwebtoken.JwtException;

@Service
public class VenueEventConfigService {

    private static final Logger logger = LoggerFactory.getLogger(VenueEventConfigService.class);

    private final IRepository<Venue> venueRepository;
    private final IEventRepository eventRepository;
    private final IRepository<User> userRepository;
    private final IAuthenticationService authService;
    private final IProductionCompanyRepository productionCompanyRepository;
    private final ILocationService locationService;
    private final IOrderRepository orderRepository;
    private final IPaymentGateway paymentService;
    private final ITicketGateway ticketService;

    public VenueEventConfigService(IRepository<Venue> venueRepository, IEventRepository eventRepository,
            IRepository<User> userRepository, IAuthenticationService authService,
            IProductionCompanyRepository productionCompanyRepository, ILocationService locationService,
            IOrderRepository orderRepository, IPaymentGateway paymentService, ITicketGateway ticketService) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.locationService = locationService;
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.ticketService = ticketService;
    }

    public Result<String> configureNewLayoutAndInventory(String sessionToken, int companyID,
            VenueRecord newVenueLayout) {

        try {
            logger.info(
                    "VenueEventConfigService.configureLayoutAndInventory: Attempting to configure new venue layout");

            if (!authService.validateToken(sessionToken)) {
                logger.warn("VenueEventConfigService.configureLayoutAndInventory: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }

            if (!authService.isUserToken(sessionToken)) {
                logger.warn("VenueEventConfigService.configureLayoutAndInventory: Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");
            }

            String userID = authService.extractSubjectFromToken(sessionToken);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying user exists for id {}", userID);
            userRepository.findByID(userID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying company exists for id {}",
                    companyID);
            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

            logger.info(
                    "VenueEventConfigService.configureLayoutAndInventory: Verifying user has permissions for company");
            company.validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

            Location loc = locationService.search(newVenueLayout.location());

            logger.info("VenueEventConfigService.configureLayoutAndInventory: creating a new venue");
            Venue venue = new Venue(newVenueLayout.name(), loc, newVenueLayout.fieldSeg(), newVenueLayout.seatSeg(),
                    newVenueLayout.name(), newVenueLayout.grid(), newVenueLayout.stages(),
                    newVenueLayout.entrances(), companyID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: saving venue changes to repository");
            venueRepository.save(venue);

            return Result.makeOk("Venue layout configured and saved successfully.");

        } catch (OptimisticLockingFailureException e) {
            logger.warn("VenueEventConfigService.configureLayoutAndInventory: Failed to save changes to repository");
            return Result.makeFail("Failed to save changes to repository."); 
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "VenueEventConfigService.configureLayoutAndInventory: Domain logic error during configuration: {}",
                    e.getMessage());
            return Result.makeFail("Configuration failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("VenueEventConfigService.configureLayoutAndInventory: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while saving the layout.");
        }
    }

    public Result<String> configureLayoutAndInventory(String sessionToken, int companyID, int eventID, String venueID) {

        try {
            logger.info(
                    "VenueEventConfigService.configureLayoutAndInventory: Attempting to configure venue layout for event {}",
                    eventID);

            if (!authService.validateToken(sessionToken)) {
                logger.warn("VenueEventConfigService.configureLayoutAndInventory: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }

            if (!authService.isUserToken(sessionToken)) {
                logger.warn("VenueEventConfigService.configureLayoutAndInventory: Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");
            }

            String userID = authService.extractSubjectFromToken(sessionToken);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying event exists for id {}",
                    eventID);
            Event targetEvent = eventRepository.findByID(String.valueOf(eventID));

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying user exists for id {}", userID);
            userRepository.findByID(userID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying company exists for id {}",
                    companyID);
            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

            logger.info(
                    "VenueEventConfigService.configureLayoutAndInventory: Verifying user has permissions for company");
            company.validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying venue exists for id {}",
                    venueID);
            Venue venue = venueRepository.findByID(venueID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: booking the event in the venue");
            venue.bookEvent(targetEvent.getEventStartTime(), targetEvent.getEventEndTime(), eventID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: saving venue changes to repository");
            venueRepository.save(venue);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: mark the event with correct venue");
            targetEvent.setEventVenue(venue.getName());

            logger.info("VenueEventConfigService.configureLayoutAndInventory: saving changes to event");
            eventRepository.save(targetEvent);

            logger.info(
                    "VenueEventConfigService.configureLayoutAndInventory: Successfully configured venue and initialized inventory for event {}",
                    eventID);
            return Result.makeOk("Venue layout configured and saved successfully.");

        } catch (OptimisticLockingFailureException e) {
            logger.warn("VenueEventConfigService.configureLayoutAndInventory: Failed to save changes to repository");
            return Result.makeFail("Failed to save changes to repository.");
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "VenueEventConfigService.configureLayoutAndInventory: Domain logic error during configuration: {}",
                    e.getMessage());
            return Result.makeFail("Configuration failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("VenueEventConfigService.configureLayoutAndInventory: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while saving the layout.");
        }
    }

    public Result<VenueDTO> getVenue(String sessionToken, String venueID) {
        try {
            logger.info(
                    "VenueEventConfigService.getVenue: Attempting to get venue with id: {}", venueID);

            if (!authService.validateToken(sessionToken)) {
                logger.warn("VenueEventConfigService.getVenue: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }

            logger.info("VenueEventConfigService.getVenue: Verifying venue exists for id {}",
                    venueID);
            Venue venue = venueRepository.findByID(venueID);

            logger.info(
                    "VenueEventConfigService.getVenue: Successfully found venue for ID {}",
                    venueID);

            return Result.makeOk(new VenueDTO(venue));
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "VenueEventConfigService.getVenue: Domain logic error during configuration: {}",
                    e.getMessage());
            return Result.makeFail("Configuration failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("VenueEventConfigService.getVenue: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred");
        }
    }

	public Result<Boolean> editVenueSegments(int companyID, String venueID, Map<String, Integer> fieldSegmentsToEdit, Map<String, List<String>> seatsSegmentsToEdit,List<FieldSegRecord> newFieldSegments, List<ChosenSeatingSegRecord> newSeatSegments,String sessionToken) {
		try {
            if (fieldSegmentsToEdit == null || seatsSegmentsToEdit == null || newFieldSegments == null || newSeatSegments == null) {       
                logger.error("VenueEventConfigService.editVenueSegments: Invalid input parameters.");
                return Result.makeFail("Invalid input parameters.");
            }
			String userID = validateAndGetUserID(sessionToken);
            // validate user has permission to edit venue segments
			logger.info("VenueEventConfigService.editVenueSegments: Session token verified successfully.");
			Venue venue = venueRepository.findByID(venueID);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

            logger.info(
                    "VenueEventConfigService.editVenueSegments: Verifying user has permissions for company");
            company.validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);
            venue.validateCompanyID(companyID);

			// future events:
            logger.info("VenueEventConfigService.editVenueSegments: Checking for future events to refund.");
			List<Integer> futureEventsToRefund = new ArrayList<>();
			for (Event event : eventRepository.findAllByVenueID(venueID)) {
				if (event.getEventStartTime().isAfter(java.time.LocalDateTime.now())) {
					futureEventsToRefund.add(event.getEventID());
				}
			}
			// eventID -> segID -> amount of ticketsto refund
			HashMap<Integer, HashMap<String, Integer>> fieldSegmentsToRefund = new HashMap<>();
			//.     eventid -> segID -> list of seatIDs to refund
			HashMap<Integer, HashMap<String, List<String>>> seatSegmentsToRefund = new HashMap<>();
			// for each event collect tickets to refund
            logger.info("VenueEventConfigService.editVenueSegments: Collecting tickets to refund for future events.");
			for (Integer eventID : futureEventsToRefund) {
				
				// edit field
				// if reserved stock for this event is bigger then new stock, we need to refund the difference to customers
				fieldSegmentsToRefund.put(eventID, new HashMap<>());
				for (Map.Entry<String, Integer> entry : fieldSegmentsToEdit.entrySet()) {
					// the reserved stock for this event in this segment
					int currntlyReserved = venue.getReservedStockBySegmentEventField(eventID, entry.getKey());
					if (currntlyReserved > entry.getValue()) {
						fieldSegmentsToRefund.get(eventID).put(entry.getKey(), currntlyReserved - entry.getValue());
					}
				}

				// edit seat
				seatSegmentsToRefund.put(eventID, new HashMap<>());
				for (Map.Entry<String, List<String>> entry : seatsSegmentsToEdit.entrySet()) {
					List<String> reservedSeatsToRefund = venue.getStockRefundForEvent(eventID, entry.getKey(), entry.getValue());
					if (!reservedSeatsToRefund.isEmpty()) {
                        seatSegmentsToRefund.get(eventID).put(entry.getKey(), reservedSeatsToRefund);
                    }
				}
			}

            // after we collect all refunds, we can edit the segments stock:
            // adding the new segments with the new stock.
            // adding new field segments
            logger.info("VenueEventConfigService.editVenueSegments: Adding new segments and editing existing segments.");
            for (FieldSegRecord record : newFieldSegments) {
                venue.addFieldSegment(record);
            }
            // adding new seating segments
            logger.info("VenueEventConfigService.editVenueSegments: Adding new seating segments.");
            for (ChosenSeatingSegRecord record : newSeatSegments) {
                venue.addChosenSeatingSegment(record);
            }
            // editing the segments with the new stock
            logger.info("VenueEventConfigService.editVenueSegments: Editing existing segments with new stock.");
            for (Map.Entry<String, Integer> entry : fieldSegmentsToEdit.entrySet()) {
                venue.setNewFieldStock(entry.getKey(), entry.getValue());
            }
            logger.info("VenueEventConfigService.editVenueSegments: Editing existing seating segments with new stock.");
            for (Map.Entry<String, List<String>> entry : seatsSegmentsToEdit.entrySet()) {
                venue.setNewSeatingStock(entry.getKey(), entry.getValue());
            }
            
            venueRepository.save(venue);
			// __refunding tickets__
            // for each ticket we need to refund. we have the event and the segment. 
            // start with seat tickets we have the seatID. so we can call ordersercive to find the transactionId that is connected to this ticket. then refund it
            logger.info("VenueEventConfigService.editVenueSegments: Refunding tickets for future events.");
            for (Map.Entry<Integer, HashMap<String, List<String>>> entry : seatSegmentsToRefund.entrySet()) {
                int eventID = entry.getKey();
                for (Map.Entry<String, List<String>> segmentEntry : entry.getValue().entrySet()) {
                    String segmentID = segmentEntry.getKey();
                    List<String> seatIDsToRefund = segmentEntry.getValue();
                    // get all orders that are completed and have this eventID, segmentID and seatIDs
                    List<Order> ordersToRefund = orderRepository.getCompletedByEventIdSeatIds(eventID, segmentID, seatIDsToRefund);
                    // for each order we need to refund the transaction
                    for (Order order : ordersToRefund) {
                        paymentService.cancelPayment(order.getTransactionId());
                        // how do i cancel the ticket? like if only half of the seats are refunded, i need to cancel only those tickets. but i need to call ticketService.revokeTicket cancels all tickets of order.
                        ticketService.revokeTicket(order.getExternalTicket());
                        order.CancelOrder();
                        orderRepository.save(order);
                    }
                } 
            }
            // for field segments
            logger.info("VenueEventConfigService.editVenueSegments: Refunding field segment tickets for future events.");
            for (Map.Entry<Integer, HashMap<String, Integer>> entry : fieldSegmentsToRefund.entrySet()) {
                int eventID = entry.getKey();
                for (Map.Entry<String, Integer> segmentEntry : entry.getValue().entrySet()) {
                    String segmentID = segmentEntry.getKey();
                    int amountToRefund = segmentEntry.getValue();
                    // get all orders that are completed and have this eventID, segmentID and orderType FIELD
                    List<Order> ordersToRefund = orderRepository.getCompletedByEventIdField(eventID, segmentID);
                    // sort the orders by the number of seats in acending order, so we refund the smallest orders first
                    ordersToRefund.sort(Comparator.comparing(Order::getNumOfTickets));
                    // for each order we need to refund the transaction
                    for (Order order : ordersToRefund) {
                        paymentService.cancelPayment(order.getTransactionId());
                        ticketService.revokeTicket(order.getExternalTicket());
                        order.CancelOrder();
                        orderRepository.save(order);
                        amountToRefund -= order.getNumOfTickets();
                        if (amountToRefund <= 0) {
                            break;
                        }
                    }
                } 
            }
            
			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("VenueEventConfigService.editVenueSegments: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (AuthException e) {
			logger.error("VenueEventConfigService.editVenueSegments: Invalid session token. " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (JwtException e) {
			logger.error("VenueEventConfigService.editVenueSegments: JWT authentication error during stock edition: "
					+ e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("VenueEventConfigService.editVenueSegments: Unexpected error occurred: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}
    private String validateAndGetUserID(String sessionToken) {
		if (!authService.validateToken(sessionToken)) {
			throw new AuthException("Invalid session token.");
		}
		if (!authService.isUserToken(sessionToken)) {
			throw new AuthException("Only users are allowed to perform operation");
		}
		String userID = (authService.extractSubjectFromToken(sessionToken));
		userRepository.findByID(userID);
		return userID;
	}
}