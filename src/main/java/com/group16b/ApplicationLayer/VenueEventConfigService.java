package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.BeanDefinitionDsl.Role;

import com.group16b.ApplicationLayer.DTOs.VenueDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.VenueRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueEventConfigService {

    private static final Logger logger = LoggerFactory.getLogger(VenueEventConfigService.class);

    private final IVenueRepository venueRepository;
    private final IEventRepository eventRepository;
    private final IUserRepository userRepository;
    private final IAuthenticationService authService;
    private final IProductionCompanyRepository productionCompanyRepository;
    private final ILocationService locationService;

    public VenueEventConfigService(IVenueRepository venueRepository, IEventRepository eventRepository, IUserRepository userRepository, IAuthenticationService authService,IProductionCompanyRepository productionCompanyRepository, ILocationService locationService) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.locationService = locationService;
    }

    public Result<String> configureLayoutAndInventory(String sessionToken, int companyID, int eventID, VenueRecord newVenueLayout) {
        
        try {
            logger.info("Attempting to configure venue layout for event {}", eventID);

            if (!authService.validateToken(sessionToken)) {
                logger.warn("Config failed: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            
            if(!authService.isUserToken(sessionToken)){
                logger.warn("Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");    
            }

            int userID = Integer.valueOf(authService.extractSubjectFromToken(sessionToken));

            Event targetEvent = eventRepository.getEventByID(eventID);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));

            User actionUser = userRepository.getUserByID(userID);
            
            company.validateUserPermissions(userID, ManagerPermissions.VENUE_CONFIGURATION);

            Location loc = locationService.search(newVenueLayout.location());

            Venue venue = new Venue(newVenueLayout.name(), loc, newVenueLayout.fieldSeg(), newVenueLayout.seatSeg());
            venue.bookEvent(targetEvent.getEventStartTime(), targetEvent.getEventEndTime(), eventID);

            venueRepository.addVenue(venue.getName(), venue);

            targetEvent.setEventVenue(venue.getName()); 
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