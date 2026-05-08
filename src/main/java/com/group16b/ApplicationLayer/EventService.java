package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Venue;

import io.jsonwebtoken.JwtException;

public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final IAuthenticationService authenticationService;
    private final IUserRepository userRepository;
    private final IVenueRepository venueRepository;
    private final IEventRepository eventRepository;  

    public EventService(IAuthenticationService authenticationService, IUserRepository userRepository, IVenueRepository venueRepository, IEventRepository eventRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
    }

    //need to make event active manually
    public Result<EventDTO> createEvent(EventRecord eventRecord, String sessionToken) {
        try{
            logger.info("Verifying session token for event creation.");
            if(!authenticationService.authenticate(sessionToken)) {
                logger.warn("Invalid session token provided for event creation.");
                throw new IllegalArgumentException("Invalid session token.");
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
        }
        catch (JwtException e) {
            logger.error("JWT authentication error during event creation: " + e.getMessage());
            return Result.makeFail("Authentication failed: " + e.getMessage());
        }
        catch (Exception e) {
            logger.error("Unexpected error during event creation: " + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public void activateEvent(int eventID, String sessionToken) {
        try{
            logger.info("Verifying session token for event activation.");
            if(!authenticationService.authenticate(sessionToken)) {
                logger.warn("Invalid session token provided for event activation.");
                throw new IllegalArgumentException("Invalid session token.");
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

        } catch (IllegalArgumentException e) {
            logger.error("Failed to find event: " + e.getMessage());
            throw e;
        }
        catch(IllegalStateException e){
            logger.error("Failed to activate event: " + e.getMessage());
            throw e;
        }
        catch (JwtException e) {
            logger.error("JWT authentication error during event activation: " + e.getMessage());
            throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
        }
        catch (Exception e) {
            logger.error("Unexpected error during event activation: " + e.getMessage());
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }

    public void deactivateEvent(int eventID, String sessionToken) {
        try{
            logger.info("Verifying session token for event deactivation.");
            if(!authenticationService.authenticate(sessionToken)) {
                logger.warn("Invalid session token provided for event deactivation.");
                throw new IllegalArgumentException("Invalid session token.");
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

        } catch (IllegalArgumentException e) {
            logger.error("Failed to find event: " + e.getMessage());
            throw e;
        }
        catch(IllegalStateException e){
            logger.error("Failed to deactivate event: " + e.getMessage());
            throw e;
        }
        catch (JwtException e) {
            logger.error("JWT authentication error during event deactivation: " + e.getMessage());
            throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
        }
        catch (Exception e) {
            logger.error("Unexpected error during event deactivation: " + e.getMessage());
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }
}
