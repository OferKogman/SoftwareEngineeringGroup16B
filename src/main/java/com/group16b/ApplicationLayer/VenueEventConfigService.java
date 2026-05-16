package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.DTOs.VenueDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueEventConfigService {

    private static final Logger logger = LoggerFactory.getLogger(VenueEventConfigService.class);

    private final IVenueRepository venueRepository;
    private final IEventRepository eventRepository;
    private final IUserRepository userRepository;
    private final IAuthenticationService authService;

    public VenueEventConfigService(IVenueRepository venueRepository, IEventRepository eventRepository, IUserRepository userRepository, IAuthenticationService authService) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public Result<String> configureLayoutAndInventory(String sessionToken, int companyID, int eventID, VenueDTO newVenueLayoutDTO, LocalDateTime startTime, LocalDateTime endTime) {
        
        logger.info("Attempting to configure venue layout for event {}", eventID);

        try {

            if (!authService.validateToken(sessionToken)) {
                logger.warn("Config failed: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            
            if(!authService.isUserToken(sessionToken)){
                logger.warn("Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");    
            }

            int userID = Integer.valueOf(authService.extractSubjectFromToken(sessionToken));

            if (!eventRepository.EventExists(eventID)) {
                return Result.makeFail("Event not found.");
            }

            Event targetEvent = eventRepository.getEventByID(eventID);
            if (targetEvent.isActiveEvent()) {
                return Result.makeFail("Event is already active and is not in creation process.");
            }

            User actionUser = userRepository.getUserByEmail(userID);
            if (actionUser == null || !(actionUser.isOwnerOfCompany(companyID) || actionUser.managerInCompany(companyID))) {
                logger.warn("Config failed: User {} is not an owner or manager for company {}.", userID, companyID);
                return Result.makeFail("Permission denied. You must be an owner or manager of this company.");
            }

            Venue newVenueLayout = new Venue(newVenueLayoutDTO);
            newVenueLayout.bookEvent(startTime, endTime, eventID);

            venueRepository.addVenue(newVenueLayout.getName(), newVenueLayout);

            targetEvent.setEventString(newVenueLayout.getName()); 
            eventRepository.updateEvent(targetEvent);

            logger.info("Successfully configured venue and initialized inventory for event {}", eventID);
            return Result.makeOk("Venue layout configured and saved successfully.");

        } catch (IllegalArgumentException e) {
            logger.warn("Domain logic error during configuration: {}", e.getMessage());
            return Result.makeFail("Configuration failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while saving the layout.");
        }
    }
}