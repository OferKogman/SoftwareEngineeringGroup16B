package com.group16b.ApplicationLayer;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocatoinService;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.ProductionCompanyPolicy.IProductionCompanyPolicyRepository;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Venue;

import io.jsonwebtoken.JwtException;

public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
	private final IAuthenticationService authenticationService;
    private final ILocatoinService locationService;
	private final IUserRepository userRepository;
	private final IVenueRepository venueRepository;
	private final IEventRepository eventRepository;
    private final IProductionCompanyPolicyRepository productionCompanyPolicyRepository;
    private final EventFilteringService eventFilteringService;

	public EventService(IAuthenticationService authenticationService, ILocatoinService locationService, IUserRepository userRepository,
			IVenueRepository venueRepository, IEventRepository eventRepository, IProductionCompanyPolicyRepository productionCompanyPolicyRepository, EventFilteringService eventFilteringService) {
        this.eventFilteringService = eventFilteringService;
		this.authenticationService = authenticationService;
		this.locationService = locationService;
		this.userRepository = userRepository;
		this.venueRepository = venueRepository;
		this.eventRepository = eventRepository;
		this.productionCompanyPolicyRepository = productionCompanyPolicyRepository;
	}

	// need to make event active manually
	public Result<EventDTO> createEvent(EventRecord eventRecord, String sessionToken) {
		try {
			logger.info("Verifying session token for event creation.");
			if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for event creation.");
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
			logger.info("Session token verified successfully.");

			logger.info("Validating user permissions for event creation.");
			user.validatePermissions(eventRecord.pcID(), Owner.class);
			logger.info("User permissions validated successfully.");

			logger.info("Attempting to create event: " + eventRecord.name());
			Event event = new Event(eventRecord);
			logger.info("Verifying venue availability.");
			Venue venue = venueRepository.getVenueByID(eventRecord.venueID());
			venue.bookEvent(eventRecord.startTime(), eventRecord.endTime(), event.getEventID());
			eventRepository.addEvent(event);
			logger.info("Event created successfully with ID: " + event.getEventID());
			return Result.makeOk(new EventDTO(event));

		} catch (IllegalArgumentException e) {
			logger.error("Failed to create event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event creation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event creation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> activateEvent(int eventID, String sessionToken) {
		try {
			logger.info("Verifying session token for event activation.");
			if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for event activation.");
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
			logger.info("Session token verified successfully.");

			Event event = eventRepository.getEventByID(eventID);

			logger.info("Validating user permissions for event activation.");
			user.validatePermissions(event.getEventProductionCompanyID(), Owner.class);
			logger.info("User permissions validated successfully.");

			logger.info("Attempting to activate event: " + event.getEventName());
			event.activateEvent();
			logger.info("Event activated successfully with ID: " + event.getEventID());

			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to activate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event activation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event activation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> deactivateEvent(int eventID, String sessionToken) {
		try {
			logger.info("Verifying session token for event deactivation.");
			if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for event deactivation.");
				return Result.makeFail("Invalid session token.");
			}
			User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
			logger.info("Session token verified successfully.");

			Event event = eventRepository.getEventByID(eventID);

			logger.info("Validating user permissions for event deactivation.");
			user.validatePermissions(event.getEventProductionCompanyID(), Owner.class);
			logger.info("User permissions validated successfully.");

			logger.info("Attempting to deactivate event: " + event.getEventName());
			event.deactivateEvent();
			logger.info("Event deactivated successfully with ID: " + event.getEventID());

			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("Failed to deactivate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

    public Result<EventDTO> viewEvent(int eventID) {
        try {
            logger.info("Attempting to retrieve event with ID: " + eventID);
            Event event = eventRepository.getEventByID(eventID);
            logger.info("Event retrieved successfully with ID: " + eventID);
            return Result.makeOk(new EventDTO(event));
        }
        catch (IllegalArgumentException e) {
            logger.error("Failed to find event: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during event retrieval: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<List<EventDTO>> searchEvents(Map<String, List<Object>> searchParams) {
        try {
            logger.info("Attempting to search events with parameters: " + searchParams);
            List<Location> locations = getParam(searchParams, "location", String.class).stream().map(locationService::search).toList();
            List<Integer> pcID = getParam(searchParams, "productionCompany", String.class).stream().map(name -> productionCompanyPolicyRepository.getProductionCompanyByName(name).getProductionCompanyID()).toList();
            List<EventDTO> results = eventFilteringService.searchEvents(
                getParam(searchParams, "name", String.class),
                getParam(searchParams, "artist", String.class),
                getParam(searchParams, "category", String.class),
                getParam(searchParams, "keyword", String.class),
                getParam(searchParams, "minPrice", Double.class),
                getParam(searchParams, "maxPrice", Double.class),
                getParam(searchParams, "startTime", java.time.LocalDateTime.class),
                getParam(searchParams, "endTime", java.time.LocalDateTime.class),
                getParam(searchParams, "eventRating", Double.class),
                pcID,
                locations,
                getParam(searchParams, "productionCompanyRating", Double.class)
            ).stream().map(EventDTO::new).toList();

            logger.info("Event search completed successfully. Found " + results.size() + " events.");
            return Result.makeOk(results);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid search parameters: " + e.getMessage());
            return Result.makeFail("Invalid search parameters: " + e.getMessage());
        }
        catch (Exception e) {
            logger.error("Unexpected error during event search: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }


    @SuppressWarnings("unchecked")
    private <T> List<T> getParam(Map<String, List<Object>> params, String key, Class<T> type) {
        if (!params.containsKey(key) || params.get(key).isEmpty()) {
            return null;
        }
        return params.get(key).stream().map(val -> {
            if (!type.isInstance(val)) {
                throw new IllegalArgumentException("Invalid type for parameter '" + key + "'. Expected: " + type.getSimpleName());
            }
            return (T) val;
        }).toList();
    }
}
