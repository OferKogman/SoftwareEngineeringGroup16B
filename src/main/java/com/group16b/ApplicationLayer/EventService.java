package com.group16b.ApplicationLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocatoinService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.IVirtualQueueRepository;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;

import io.jsonwebtoken.JwtException;

public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
	private final IAuthenticationService authenticationService;
    private final ILocatoinService locationService;
    private final EventFilteringService eventFilteringService;
	private final IUserRepository userRepository = UserRepositoryMapImpl.getInstance();
	private final IVenueRepository venueRepository = VenueRepositoryMapImpl.getInstance();
	private final IEventRepository eventRepository = EventRepositoryMapImpl.getInstance();
	private final IVirtualQueueRepository queueRepository = VirtualQueueRepositoryMapImpl.getInstance();
    private final IRepository<ProductionCompany> productionCompanyRepository;

	public EventService(IAuthenticationService authenticationService, ILocatoinService locationService, EventFilteringService eventFilteringService, IRepository<ProductionCompany> productionCompanyRepo) {
        this.eventFilteringService = eventFilteringService;
		this.authenticationService = authenticationService;
		this.locationService = locationService;
		this.productionCompanyRepository=productionCompanyRepo;
	}

	// need to make event active manually
	public Result<EventDTO> createEvent(EventRecord eventRecord, String sessionToken) {
		try {
			logger.info("Verifying session token for event creation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event creation.");
				return Result.makeFail("Invalid session token.");
			}
			if(!authenticationService.isUserToken(sessionToken)) {
				logger.warn("Only signed-in users are allowed to create events.");
				return Result.makeFail("Only signed-in users are allowed to create events. Please use a production company account.");
			}
			if(!authenticationService.isUserToken(sessionToken)){
				logger.warn("Only USERS are allowed to create events.");
				return Result.makeFail("Only signed-in users are allowed to create events. Please use a user account.");
			}
			User user = userRepository.getUserByID(Integer.parseInt(authenticationService.extractSubjectFromToken(sessionToken)));
			logger.info("Session token verified successfully.");

			if(productionCompanyPolicyRepository.getProductionCompanyByID(eventRecord.pcID()) == null) {
				logger.warn("Invalid production company ID provided for event creation.");
				return Result.makeFail("Invalid production company ID. Please provide a valid production company ID to create an event.");
			}

			logger.info("Validating user permissions for event creation.");
			user.validatePermissions(eventRecord.pcID(), Owner.class);
			logger.info("User permissions validated successfully.");

			logger.info("Attempting to create event: " + eventRecord.name());
			Event event = new Event(eventRecord, user.getUserID());
			logger.info("Creating queue for the new event");
			VirtualQueue q = new VirtualQueue(event.getEventID());
			logger.info("Verifying venue availability.");
			Venue venue = venueRepository.getVenueByID(eventRecord.venueID());
			venue.bookEvent(eventRecord.startTime(), eventRecord.endTime(), event.getEventID());
			eventRepository.addEvent(event);
			queueRepository.addVirtualQueue(q);
			logger.info("Event created successfully with ID: " + event.getEventID());
			return Result.makeOk(new EventDTO(event));

		} catch (IllegalArgumentException e) {
			logger.error("Failed to create event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (JwtException e) {
			logger.error("JWT authentication error during event creation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (Exception e) {
			logger.error("Unexpected error during event creation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> activateEvent(int eventID, String sessionToken) {
		try {
			logger.info("Verifying session token for event activation.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event activation.");
				return Result.makeFail("Invalid session token.");
			}

			if(!authenticationService.isUserToken(sessionToken)) {
				logger.warn("Only signed-in users are allowed to create events.");
				return Result.makeFail("Only signed-in users are allowed to create events. Please use a production company account.");
			}
			User user = userRepository.getUserByID(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
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
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event deactivation.");
				return Result.makeFail("Invalid session token.");
			}

			if(!authenticationService.isUserToken(sessionToken)) {
				logger.warn("Only signed-in users are allowed to create events.");
				return Result.makeFail("Only signed-in users are allowed to create events. Please use a production company account.");
			}
			User user = userRepository.getUserByID(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
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
	
	public Result<EventDTO> editEvent(Map<String, Object> editParams, int eventID, String sessionToken) {
        try {
			logger.info("Verifying session token for event editing.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event editing.");
				return Result.makeFail("Invalid session token.");
			}

			if(!authenticationService.isUserToken(sessionToken)) {
				logger.warn("Only signed-in users are allowed to create events.");
				return Result.makeFail("Only signed-in users are allowed to create events. Please use a production company account.");
			}
			User user = userRepository.getUserByID(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
			logger.info("Session token verified successfully.");

			Event event = eventRepository.getEventByID(eventID);

			logger.info("Validating user permissions for event activation.");
			user.validatePermissions(event.getEventProductionCompanyID(), Owner.class);
			logger.info("User permissions validated successfully.");

            logger.info("Attempting to edit event with ID: " + eventID);
			if (getEditParam(editParams, "name", String.class) != null) {
				event.setEventName(getEditParam(editParams, "name", String.class));
			}
			if (getEditParam(editParams, "artist", String.class) != null) {
				event.setEventArtist(getEditParam(editParams, "artist", String.class));
			}
			if (getEditParam(editParams, "category", String.class) != null) {
				event.setEventCategory(getEditParam(editParams, "category", String.class));
			}
			if (getEditParam(editParams, "startTime", java.time.LocalDateTime.class) != null && getEditParam(editParams, "endTime", java.time.LocalDateTime.class) != null) {
				event.setEventNewTime(getEditParam(editParams, "startTime", java.time.LocalDateTime.class), getEditParam(editParams, "endTime", java.time.LocalDateTime.class));
			} else if (getEditParam(editParams, "startTime", java.time.LocalDateTime.class) != null || getEditParam(editParams, "endTime", java.time.LocalDateTime.class) != null) {
				return Result.makeFail("Must edit both start and end time together to update event time !");
			}
			if (getEditParam(editParams, "venue", String.class) != null) {
				Venue oldVenue = venueRepository.getVenueByID(event.getEventVenueID());
				oldVenue.cancelEvent(event.getEventStartTime(),event.getEventID());
				String newVenueID = getEditParam(editParams, "venue", String.class);
				Venue newVenue = venueRepository.getVenueByID(newVenueID);
				newVenue.bookEvent(event.getEventStartTime(), event.getEventEndTime(), event.getEventID());
				event.setEventString(newVenueID);
			}
			if (getEditParam(editParams, "eventRating", Double.class) != null) {
				event.setEventRating(getEditParam(editParams, "eventRating", Double.class));
			}
			eventRepository.updateEvent(event);
			logger.info("Event edited successfully with ID: " + eventID);
            return Result.makeOk(new EventDTO(event));
        }
        catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return Result.makeFail(e.getMessage());
        }
		catch (Exception e) {
            logger.error("Unexpected error during event search: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

	public Result<String> editStockInSegmentsForEvent(Map<String, Integer> segmentsAndNewStock,  int eventID, String sessionToken){
		try{
			logger.info("Verifying session token for event editing.");
			if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event editing.");
				return Result.makeFail("Invalid session token.");
			}

			if(!authenticationService.isUserToken(sessionToken)) {
				logger.warn("Only signed-in users are allowed to create events.");
				return Result.makeFail("Only signed-in users are allowed to create events. Please use a production company account.");
			}
			User user = userRepository.getUserByID(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
			logger.info("Session token verified successfully.");

			Event event = eventRepository.getEventByID(eventID);

			logger.info("Validating user permissions for event activation.");
			user.validatePermissions(event.getEventProductionCompanyID(), Owner.class);
			logger.info("User permissions validated successfully.");

			Venue venue = venueRepository.getVenueByID(event.getEventVenueID());

			logger.info("Attempting to edit event with ID: " + eventID);	
			
			for(Map.Entry<String, Integer> entry : segmentsAndNewStock.entrySet()){
				Segment currSeg = venue.getSegmentByID(entry.getKey());
				currSeg.setStockForEvent(eventID, entry.getValue());
			}

			return Result.makeOk("Changed stocks for eventID: " + eventID);
		}	catch(Exception e){
			logger.error("Unexpected error during event search: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}	
	}

    public Result<List<EventDTO>> searchEvents(Map<String, List<Object>> searchParams) {
        try {
            logger.info("Attempting to search events with parameters: " + searchParams);
            List<String> locationNames = getParam(searchParams, "location", String.class);
            List<Location> locations = null;
            if (locationNames != null) {
                locations = new ArrayList<>();
                for (String locationName : locationNames) {
                    locations.add(locationService.search(locationName));
                }
            }
            List<String> pcName = getParam(searchParams, "productionCompany", String.class);
			List<Integer> pcID = null;
			if(pcName != null) {
				pcID = pcName.stream().map(name -> productionCompanyPolicyRepository.getProductionCompanyByName(name).getProductionCompanyID()).toList();
			}
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
        catch (IOException e) {
			logger.error("Failed contacting Photon API: " + e.getMessage());
            return Result.makeFail("Failed contacting Photon API: " + e.getMessage());
		} catch (InterruptedException e) {
			logger.error("Request interrupted: " + e.getMessage());
            return Result.makeFail("Request interrupted: " + e.getMessage());
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

	@SuppressWarnings("unchecked")
    private <T> T getEditParam(Map<String, Object> params, String key, Class<T> type) {
        if (!params.containsKey(key)) {
            return null;
        }
        if (!type.isInstance(params.get(key))) {
            throw new IllegalArgumentException("Invalid type for parameter '" + key + "'. Expected: " + type.getSimpleName());
        }
        return (T) params.get(key);
    }
}
