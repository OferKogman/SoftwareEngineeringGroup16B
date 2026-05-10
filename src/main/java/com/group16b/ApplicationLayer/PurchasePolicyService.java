package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Event.IEventRepositoryMapImpl;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.User;

public class PurchasePolicyService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final IEventRepository eventRepo = IEventRepositoryMapImpl.getInstance();
    private final IAuthenticationService authenticationService;
	private final IUserRepository userRepository;

    public PurchasePolicyService(IAuthenticationService authenticationService, IUserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }
    

    public Result<LotteryPolicy> createLotteryPolicy(String sessionToken, int eventID, int lotteryID, String lotteryName, int winnerAmount, LocalDateTime lotteryRegistrationDueDate) {
        logger.info("Received request to create lottery policy for event ID: {} by session token: {}", eventID, sessionToken); 
        logger.info("Validating session token: {}", sessionToken);
        if (!authenticationService.authenticate(sessionToken)) {
				logger.warn("Invalid session token provided for event creation.");
				return Result.makeFail("Invalid session token.");
			}
        User user = userRepository.getUserByID(authenticationService.extractIdFromUserToken(sessionToken));
        logger.info("Checking user permissions for userID: {}", user.getUserID());
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
}
