package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.DomainServices.AuthenticationService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.SessionToken;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Venue;

public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final AuthenticationService authenticationService;
    private final IVenueRepository venueRepository;
    private final IEventRepository eventRepository;  

    public EventService(AuthenticationService authenticationService, IVenueRepository venueRepository, IEventRepository eventRepository) {
        this.authenticationService = authenticationService;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
    }

    //need to make event active manually
    public void createEvent(EventRecord eventRecord, SessionToken sessionToken) {
        try{
            logger.info("Verifying session token for event creation.");
            User user = authenticationService.authenticate(sessionToken);
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

        } catch (IllegalArgumentException e) {
            logger.error("Failed to create event: " + e.getMessage());
            throw e;
        }
    }

    public void activateEvent(int eventID) {
        logger.info("Activating event: " + eventID);

        logger.info("Event activated: " + eventID);
    }
}
