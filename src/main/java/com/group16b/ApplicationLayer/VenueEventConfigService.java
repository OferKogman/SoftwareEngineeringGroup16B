package com.group16b.ApplicationLayer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.VenueRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueEventConfigService {

    private static final Logger logger = LoggerFactory.getLogger(VenueEventConfigService.class);

    private final IVenueRepository venueRepository;
    private final IEventRepository eventRepository;
    private final IRepository<User> userRepository;
    private final IAuthenticationService authService;
    private final IProductionCompanyRepository productionCompanyRepository;
    private final ILocationService locationService;

    public VenueEventConfigService(IVenueRepository venueRepository, IEventRepository eventRepository, IRepository<User> userRepository, IAuthenticationService authService,IProductionCompanyRepository productionCompanyRepository, ILocationService locationService) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.locationService = locationService;
    }

    public Result<String> configureNewLayoutAndInventory(String sessionToken, int companyID, int eventID, VenueRecord newVenueLayout) {
        
        try {
            logger.info("VenueEventConfigService.configureLayoutAndInventory: Attempting to configure venue layout for event {}", eventID);

            if (!authService.validateToken(sessionToken)) {
                logger.warn("VenueEventConfigService.configureLayoutAndInventory: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            
            if(!authService.isUserToken(sessionToken)){
                logger.warn("VenueEventConfigService.configureLayoutAndInventory: Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");    
            }

            String userID = authService.extractSubjectFromToken(sessionToken);
            
            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying event exists for id {}", eventID);
            Event targetEvent = eventRepository.findByID(String.valueOf(eventID));
            
            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying user exists for id {}", userID);
            userRepository.findByID(userID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying company exists for id {}", companyID);
            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            
            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying user has permissions for company");
            company.validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

            Location loc = locationService.search(newVenueLayout.location());

            logger.info("VenueEventConfigService.configureLayoutAndInventory: creating a new venue");
            Venue venue = new Venue(newVenueLayout.name(), loc, newVenueLayout.fieldSeg(), newVenueLayout.seatSeg(), "venueID");

            logger.info("VenueEventConfigService.configureLayoutAndInventory: booking the event in the venue");
            venue.bookEvent(targetEvent.getEventStartTime(), targetEvent.getEventEndTime(), eventID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: saving venue changes to repository");
            venueRepository.addVenue(venue.getName(), venue);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: mark the event with correct venue");
            targetEvent.setEventVenue(venue.getName()); 

            logger.info("VenueEventConfigService.configureLayoutAndInventory: saving changes to event");
            eventRepository.save(targetEvent);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Successfully configured venue and initialized inventory for event {}", eventID);
            return Result.makeOk("Venue layout configured and saved successfully.");

        } catch (OptimisticLockingFailureException e) {
            logger.warn("VenueEventConfigService.configureLayoutAndInventory: Failed to save changes to repository");
            return Result.makeOk("Failed to save changes to repository.");
        } catch (IllegalArgumentException e) {
            logger.warn("VenueEventConfigService.configureLayoutAndInventory: Domain logic error during configuration: {}", e.getMessage());
            return Result.makeFail("Configuration failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("VenueEventConfigService.configureLayoutAndInventory: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while saving the layout.");
        }
    }

    public Result<String> configureLayoutAndInventory(String sessionToken, int companyID, int eventID, String venueID) {
        
        try {
            logger.info("VenueEventConfigService.configureLayoutAndInventory: Attempting to configure venue layout for event {}", eventID);

            if (!authService.validateToken(sessionToken)) {
                logger.warn("VenueEventConfigService.configureLayoutAndInventory: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            
            if(!authService.isUserToken(sessionToken)){
                logger.warn("VenueEventConfigService.configureLayoutAndInventory: Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");    
            }

            String userID = authService.extractSubjectFromToken(sessionToken);
            
            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying event exists for id {}", eventID);
            Event targetEvent = eventRepository.findByID(String.valueOf(eventID));
            
            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying user exists for id {}", userID);
            userRepository.findByID(userID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying company exists for id {}", companyID);
            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            
            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying user has permissions for company");
            company.validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Verifying venue exists for id {}", venueID);
            Venue venue = venueRepository.findByID(venueID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: booking the event in the venue");
            venue.bookEvent(targetEvent.getEventStartTime(), targetEvent.getEventEndTime(), eventID);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: saving venue changes to repository");
            venueRepository.addVenue(venue.getName(), venue);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: mark the event with correct venue");
            targetEvent.setEventVenue(venue.getName()); 
            
            logger.info("VenueEventConfigService.configureLayoutAndInventory: saving changes to event");
            eventRepository.save(targetEvent);

            logger.info("VenueEventConfigService.configureLayoutAndInventory: Successfully configured venue and initialized inventory for event {}", eventID);
            return Result.makeOk("Venue layout configured and saved successfully.");

        } catch (OptimisticLockingFailureException e) {
            logger.warn("VenueEventConfigService.configureLayoutAndInventory: Failed to save changes to repository");
            return Result.makeOk("Failed to save changes to repository.");
        } catch (IllegalArgumentException e) {
            logger.warn("VenueEventConfigService.configureLayoutAndInventory: Domain logic error during configuration: {}", e.getMessage());
            return Result.makeFail("Configuration failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("VenueEventConfigService.configureLayoutAndInventory: System error: {}", e.getMessage(), e);
            return Result.makeFail("An unexpected system error occurred while saving the layout.");
        }
    }
}