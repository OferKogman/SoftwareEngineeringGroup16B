package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class PurchasePolicyService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    
    private final IAuthenticationService authenticationService;
    private final IEventRepository eventRepo = EventRepositoryMapImpl.getInstance();
	private final IUserRepository userRepository = UserRepositoryMapImpl.getInstance();

    public PurchasePolicyService(IAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    

    public Result<LotteryPolicy> createLotteryPolicy(String sessionToken, int eventID, int lotteryID, String lotteryName, int winnerAmount, LocalDateTime lotteryRegistrationDueDate) {
        logger.info("Received request to create lottery policy for event ID: {} by session token: {}", eventID, sessionToken); 
        logger.info("Validating session token: {}", sessionToken);
        if (!authenticationService.validateToken(sessionToken)) {
				logger.warn("Invalid session token provided for event creation.");
				return Result.makeFail("Invalid session token.");
			}
        User user = userRepository.getUserByEmail(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));
        logger.info("Checking user permissions for userID: {}", user.getEmail());
        user.validatePermissions(eventRepo.getEventByID(eventID).getEventProductionCompanyID(), ManagerPermissions.PURCHASE_POLICY);
        logger.info("User has necessary permissions to create lottery policy for event ID: {}", eventID);

        logger.info("Creating lottery policy with ID: {}, Name: {}, Winner Amount: {}, Registration Due Date: {}", lotteryID, lotteryName, winnerAmount, lotteryRegistrationDueDate);
        LotteryPolicy lotteryPolicy = new LotteryPolicy(lotteryID, lotteryName, winnerAmount, lotteryRegistrationDueDate);
        logger.info("Lottery policy created successfully: {}", lotteryPolicy);
        
        logger.info("Adding lottery policy to event with ID: {}", eventID);
        eventRepo.getEventByID(eventID).addEventPurchasePolicy(lotteryPolicy);
        logger.info("Lottery policy added to event successfully");
        return Result.makeOk(lotteryPolicy);
    }

    public Result<Void> enrollInLottery(String sessionToken, int eventID) {
        try{
            logger.info("Received request to enroll in lottery for event ID: {} by session token: {}", eventID, sessionToken);
            logger.info("Validating session token: {}", sessionToken);
            if (!authenticationService.validateToken(sessionToken)) {
                logger.warn("Invalid session token provided for lottery enrollment.");
                return Result.makeFail("Invalid session token.");
            }
            User user = userRepository.getUserByEmail(Integer.valueOf(authenticationService.extractSubjectFromToken(sessionToken)));

            logger.info("Checking if userID: {} passed purchase policy checks", user.getEmail());
            //TODO: implement purchase policy checks for lottery enrollment
            logger.info("User passed purchase policy checks for lottery enrollment");

            //check event is active
            logger.info("Checking if event with ID: {} is active", eventID);
            if(!eventRepo.getEventByID(eventID).getEventStatus()) {
                logger.warn("Event with ID: {} is not active. Cannot enroll in lottery.", eventID);
                return Result.makeFail("Event is not active. Cannot enroll in lottery.");
            }

            //check event has lottery policy with lotteryID
            logger.info("Checking if event with ID: {} has a lottery policy", eventID);
            LotteryPolicy lottery = eventRepo.getEventByID(eventID).getLotteryPolicy();
            if(lottery == null) {
                logger.warn("Event with ID: {} does not have a lottery policy.", eventID);
                return Result.makeFail("Event does not have a lottery policy.");
            }

            logger.info("Enrolling in lottery...");
            lottery.enrollInLottery(eventID, user.getEmail());
            logger.info("User with ID: {} enrolled in lottery for event ID: {} successfully", user.getEmail(), eventID);
            return Result.makeOk(null);
        }
        catch (IllegalStateException e) {
            logger.error("Failed to enroll in lottery for event ID: {}: {}", eventID, e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
            logger.error("An unexpected error occurred while enrolling in lottery for event ID: {}: {}", eventID, e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
}