package com.group16b.ApplicationLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.BeanDefinitionDsl.Role;

import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import java.time.LocalDateTime;
import org.springframework.dao.OptimisticLockingFailureException;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;

import io.jsonwebtoken.JwtException;

public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
	private final IAuthenticationService authenticationService;
    private final ILocationService locationService;
    private final EventFilteringService eventFilteringService;
	private final IUserRepository userRepository = UserRepositoryMapImpl.getInstance();
	private final IRepository<Venue> venueRepository;
	private final IEventRepository eventRepository = new EventRepositoryMapImpl();
	private final IRepository<VirtualQueue> queueRepository;
    private final IProductionCompanyRepository productionCompanyRepository;

	public EventService(IAuthenticationService authenticationService, ILocationService locationService, EventFilteringService eventFilteringService, IProductionCompanyRepository productionCompanyRepo, IRepository<VirtualQueue> queueRepository, IRepository<Venue> venueRepository) {
        this.eventFilteringService = eventFilteringService;
		this.authenticationService = authenticationService;
		this.locationService = locationService;
		this.productionCompanyRepository = productionCompanyRepo;
		this.queueRepository = queueRepository;
		this.venueRepository = venueRepository;
	}

	// need to make event active manually
	public Result<EventDTO> createEvent(EventRecord eventRecord, String sessionToken) {
		try {
			int userID = validateAndGetUserID(sessionToken);
			logger.info("EventService.createEvent: Session token verified successfully.");

			User user = userRepository.getUserByID(userID);
	
			logger.info("EventService.createEvent: retrieving production company for event creation");
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(eventRecord.pcID()));

			logger.info("EventService.createEvent: Validating user permissions for event creation.");
			company.validateUserPermissions(userID, RoleType.OWNER);
			logger.info("EventService.createEvent: User permissions validated successfully.");

			logger.info("EventService.createEvent: Attempting to create event: " + eventRecord.name());
			Event event = new Event(eventRecord, user.getUserID());

			logger.info("EventService.createEvent: Creating queue for the new event");
			VirtualQueue q = new VirtualQueue(event.getEventID());

			logger.info("EventService.createEvent: Verifying venue availability.");
			Venue venue = venueRepository.findByID(eventRecord.venueID());
			venue.bookEvent(eventRecord.startTime(), eventRecord.endTime(), event.getEventID());

			eventRepository.save(event);
			queueRepository.save(q);
			venueRepository.save(venue);

			logger.info("EventService.createEvent: Event created successfully with ID: " + event.getEventID());
			return Result.makeOk(new EventDTO(event));

		} catch (IllegalArgumentException e) {
			logger.error("EventService.createEvent: Failed to create event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		}catch (AuthException e) {
			logger.error("EventService.createEvent: Invalid token. " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (JwtException e) {
			logger.error("EventService.createEvent: JWT authentication error during event creation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (OptimisticLockingFailureException e){
			logger.error("EventService.createEvent: Concurrent modification error during event creation: " + e.getMessage());
			return Result.makeFail("Failed to create event due to concurrent modification. Please try again.");
		}catch (Exception e) {
			logger.error("EventService.createEvent: Unexpected error during event creation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> activateEvent(int eventID, String sessionToken) {
		try {
			int userID = validateAndGetUserID(sessionToken);
			logger.info("EventService.activateEvent: Session token verified successfully.");

			logger.info("EventService.activateEvent: retrieving event for activation");
			Event event = eventRepository.findByID(String.valueOf(eventID));

			logger.info("EventService.activateEvent: retrieving production company for event activation");
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(event.getEventProductionCompanyID()));

			logger.info("EventService.activateEvent: Validating user permissions for event activation.");
			company.validateUserPermissions(userID, RoleType.OWNER);
			logger.info("EventService.activateEvent: User permissions validated successfully.");

			logger.info("EventService.activateEvent: Attempting to activate event: " + event.getEventName());
			event.activateEvent();
			eventRepository.save(event);
			logger.info("EventService.activateEvent: Event activated successfully with ID: " + event.getEventID());

			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("EventService.activateEvent: Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("EventService.activateEvent: Failed to activate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		}catch (AuthException e) {
			logger.error("EventService.activateEvent: Invalid session token. " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (JwtException e) {
			logger.error("EventService.activateEvent: JWT authentication error during event activation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (OptimisticLockingFailureException e){
			logger.error("EventService.activateEvent: Concurrent modification error during event activation: " + e.getMessage());
			return Result.makeFail("Failed to activate event due to concurrent modification. Please try again.");
		}catch (Exception e) {
			logger.error("EventService.activateEvent: Unexpected error during event activation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

	public Result<Boolean> deactivateEvent(int eventID, String sessionToken) {
		try {
			int userID = validateAndGetUserID(sessionToken);
			logger.info("EventService.deactivateEvent: Session token verified successfully.");

			logger.info("EventService.deactivateEvent: retrieving event for deactivation");
			Event event = eventRepository.findByID(String.valueOf(eventID));

			logger.info("EventService.deactivateEvent: retrieving production company for event deactivation");
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(event.getEventProductionCompanyID()));

			logger.info("EventService.deactivateEvent: Validating user permissions for event deactivation.");
			company.validateUserPermissions(userID, RoleType.OWNER);
			logger.info("EventService.deactivateEvent: User permissions validated successfully.");

			logger.info("EventService.deactivateEvent: Attempting to deactivate event: " + event.getEventName());
			event.deactivateEvent();
			eventRepository.save(event);
			logger.info("EventService.deactivateEvent: Event deactivated successfully with ID: " + event.getEventID());

			return Result.makeOk(true);

		} catch (IllegalArgumentException e) {
			logger.error("EventService.deactivateEvent: Failed to find event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		} catch (IllegalStateException e) {
			logger.error("EventService.deactivateEvent: Failed to deactivate event: " + e.getMessage());
			return Result.makeFail(e.getMessage());
		}catch (AuthException e) {
			logger.error("EventService.deactivateEvent: Invalid session token. " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
		} catch (JwtException e) {
			logger.error("EventService.deactivateEvent: JWT authentication error during event deactivation: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch (OptimisticLockingFailureException e){
			logger.error("EventService.deactivateEvent: Concurrent modification error during event deactivation: " + e.getMessage());
			return Result.makeFail("Failed to deactivate event due to concurrent modification. Please try again.");
		} catch (Exception e) {
			logger.error("EventService.deactivateEvent: Unexpected error during event deactivation: " + e.getMessage());
			return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}
	}

    public Result<EventDTO> viewEvent(int eventID) {
        try {
            logger.info("EventService.viewEvent: Attempting to retrieve event with ID: " + eventID);
            Event event = eventRepository.findByID(String.valueOf(eventID));

            logger.info("EventService.viewEvent: Event retrieved successfully with ID: " + eventID);
            return Result.makeOk(new EventDTO(event));
        }
        catch (IllegalArgumentException e) {
            logger.error("EventService.viewEvent: Failed to find event: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("EventService.viewEvent: Unexpected error during event retrieval: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
	
	public Result<EventDTO> editEvent(Map<String, Object> editParams, int eventID, String sessionToken) {
        try {
			int userID = validateAndGetUserID(sessionToken);
			logger.info("EventService.editEvent: Session token verified successfully.");

			logger.info("EventService.editEvent: retrieving event for edition");
			Event event = eventRepository.findByID(String.valueOf(eventID));

			logger.info("EventService.editEvent: retrieving production company for event edition");
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(event.getEventProductionCompanyID()));

			logger.info("EventService.editEvent: Validating user permissions for event edition.");
			company.validateUserPermissions(userID, RoleType.OWNER);
			logger.info("EventService.editEvent: User permissions validated successfully.");

            logger.info("EventService.editEvent: Attempting to edit event with ID: " + eventID);
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
				Venue oldVenue = venueRepository.findByID(event.getEventVenueID());
				oldVenue.cancelEvent(event.getEventStartTime(),event.getEventID());
				String newVenueID = getEditParam(editParams, "venue", String.class);
				Venue newVenue = venueRepository.findByID(newVenueID);
				newVenue.bookEvent(event.getEventStartTime(), event.getEventEndTime(), event.getEventID());
				venueRepository.save(oldVenue);
				venueRepository.save(newVenue);
				event.setEventVenue(newVenueID);
			}
			if (getEditParam(editParams, "eventRating", Double.class) != null) {
				event.setEventRating(getEditParam(editParams, "eventRating", Double.class));
			}
			eventRepository.save(event);
			
			logger.info("EventService.editEvent: Event edited successfully with ID: " + eventID);
            return Result.makeOk(new EventDTO(event));
        }
        catch (IllegalArgumentException e) {
            logger.error("EventService.editEvent: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        }
		catch (OptimisticLockingFailureException e){
			logger.error("EventService.editEvent: Concurrent modification error during event edition: " + e.getMessage());
			return Result.makeFail("Failed to edit event due to concurrent modification. Please try again.");
		}
		catch (AuthException e) {
			logger.error("EventService.editEvent: Invalid session token. " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (JwtException e) {
			logger.error("EventService.editEvent: JWT authentication error during event edition: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (Exception e) {
            logger.error("EventService.editEvent: Unexpected error during event search: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

	public Result<String> editStockInSegmentsForEvent(Map<String, Integer> segmentsAndNewStock,  int eventID, String sessionToken){
		try{
			int userID = validateAndGetUserID(sessionToken);
			logger.info("EventService.editStockInSegmentsForEvent: Session token verified successfully.");

			logger.info("EventService.editStockInSegmentsForEvent: retrieving event for stock edition");
			Event event = eventRepository.findByID(String.valueOf(eventID));

			logger.info("EventService.editStockInSegmentsForEvent: retrieving production company for event stock edition");
			ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(event.getEventProductionCompanyID()));

			logger.info("EventService.editStockInSegmentsForEvent: Validating user permissions for event activation.");
			company.validateUserPermissions(userID, RoleType.OWNER);
			logger.info("EventService.editStockInSegmentsForEvent: User permissions validated successfully.");

			logger.info("EventService.editStockInSegmentsForEvent: Attempting to edit stock in segments for event with ID: " + eventID);
			Venue venue = venueRepository.findByID(event.getEventVenueID());

			
			for(Map.Entry<String, Integer> entry : segmentsAndNewStock.entrySet()){
				Segment currSeg = venue.getSegmentByID(entry.getKey());
				currSeg.setStockForEvent(eventID, entry.getValue());
			}
			venueRepository.save(venue);

			return Result.makeOk("EventService.editStockInSegmentsForEvent: Changed stocks for eventID: " + eventID);
		}
		catch (IllegalArgumentException e) {
            logger.error("EventService.editStockInSegmentsForEvent: " + e.getMessage());
            return Result.makeFail(e.getMessage());
		}
		catch (AuthException e) {
			logger.error("EventService.editStockInSegmentsForEvent: Invalid session token. " + e.getMessage());
				return Result.makeFail("Authentication failed: " + e.getMessage());
		}
		catch (JwtException e) {
			logger.error("EventService.editStockInSegmentsForEvent: JWT authentication error during stock edition: " + e.getMessage());
			return Result.makeFail("Authentication failed: " + e.getMessage());
		}catch(Exception e){
			logger.error("EventService.editStockInSegmentsForEvent: Unexpected error occurred: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
		}	
	}

    public Result<List<EventDTO>> searchEvents(Map<String, List<Object>> searchParams) {
        try {
            logger.info("EventService.searchEvents: Attempting to search events with parameters: " + searchParams);
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
			if (pcName != null) {
					pcID = pcName.stream().map(productionCompanyRepository::getIDByName).toList();
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

            logger.info("EventService.searchEvents: Event search completed successfully. Found " + results.size() + " events.");
            return Result.makeOk(results);
        }
        catch (IllegalArgumentException e) {
            logger.error("EventService.searchEvents: Invalid search parameters: " + e.getMessage());
            return Result.makeFail("Invalid search parameters: " + e.getMessage());
        }
        catch (IOException e) {
			logger.error("EventService.searchEvents: Failed contacting Photon API: " + e.getMessage());
            return Result.makeFail("Failed contacting Photon API: " + e.getMessage());
		} catch (InterruptedException e) {
			logger.error("EventService.searchEvents: Request interrupted: " + e.getMessage());
            return Result.makeFail("Request interrupted: " + e.getMessage());
		}
		catch (Exception e) {
            logger.error("EventService.searchEvents: Unexpected error during event search: " + e.getMessage());
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

	private int validateAndGetUserID(String sessionToken)
    {
        if (!authenticationService.validateToken(sessionToken)  ) {
            throw new AuthException("Invalid session token.");
        }
        if (!authenticationService.isUserToken(sessionToken)) {
            throw new AuthException("Only users are allowed to perform operation");
        }
        int userID = Integer.parseInt(authenticationService.extractSubjectFromToken(sessionToken));
        userRepository.getUserByID(userID);
        return userID;
    }
}
