package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;

import org.springframework.stereotype.Service;

@Service
public class PurchasePolicyService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    
    private final IAuthenticationService authenticationService;
    private final IEventRepository eventRepo;
	private final IRepository<User> userRepository;
    private final IProductionCompanyRepository productionCompanyRepository;

    public PurchasePolicyService(IAuthenticationService authenticationService, IProductionCompanyRepository productionCompanyRepository, IEventRepository eventRepo, IRepository<User> userRepository) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepository=productionCompanyRepository;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
    }
    

    public Result<Boolean> createLotteryPolicy(String sessionToken, int eventID, int lotteryID, String lotteryName, int winnerAmount, LocalDateTime lotteryRegistrationDueDate) {
        try {
            logger.info("PurchasePolicyService.createLotteryPolicy: Received request to create lottery policy for event ID: {}", eventID); 
            if (!authenticationService.validateToken(sessionToken)) {
                    logger.warn("PurchasePolicyService.createLotteryPolicy: Invalid or expired session token.");
                    return Result.makeFail("Authentication failed. Please log in again.");
                }
                if(!authenticationService.isUserToken(sessionToken)){
                    logger.warn("PurchasePolicyService.createLotteryPolicy: Expected a user session token");
                    return Result.makeFail("Authentication failed. Please log in again.");    
                }
            String userID = authenticationService.extractSubjectFromToken(sessionToken);

            logger.info("PurchasePolicyService.createLotteryPolicy: verifying user exists for id {}", userID);
            User user = userRepository.findByID(userID);

            logger.info("PurchasePolicyService.createLotteryPolicy: retrieving production company for creating a lottery policy");
            ProductionCompany company=productionCompanyRepository.findByID(String.valueOf(eventRepo.findByID(String.valueOf(eventID)).getEventProductionCompanyID()));

            logger.info("PurchasePolicyService.createLotteryPolicy: Checking user permissions for userID: {}", user.getEmail());
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            logger.info("PurchasePolicyService.createLotteryPolicy: Creating lottery policy with ID: {}, Name: {}, Winner Amount: {}, Registration Due Date: {}", lotteryID, lotteryName, winnerAmount, lotteryRegistrationDueDate);
            LotteryPolicy lotteryPolicy = new LotteryPolicy(lotteryID, lotteryName, winnerAmount, lotteryRegistrationDueDate);
            
            logger.info("PurchasePolicyService.createLotteryPolicy: verifying event exists for id {}", eventID);
            Event e = eventRepo.findByID(String.valueOf(eventID));
            
            logger.info("PurchasePolicyService.createLotteryPolicy: Adding lottery policy to event with ID: {}", eventID);
            e.addEventPurchasePolicy(lotteryPolicy);

            logger.info("PurchasePolicyService.createLotteryPolicy: saving changes to repository");
            eventRepo.save(e);

            logger.info("PurchasePolicyService.createLotteryPolicy: Lottery policy added to event successfully");
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("PurchasePolicyService.createLotteryPolicy: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
            logger.error("PurchasePolicyService.createLotteryPolicy: An unexpected error occurred while creating a lottery" + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Boolean> enrollInLottery(String sessionToken, int eventID) {
        try{
            logger.info("PurchasePolicyService.enrollInLottery: Received request to enroll in lottery for event ID: {}", eventID); 
            if (!authenticationService.validateToken(sessionToken)) {
                logger.warn("PurchasePolicyService.enrollInLottery: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            if(!authenticationService.isUserToken(sessionToken)){
                logger.warn("PurchasePolicyService.enrollInLottery: Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");    
            }
            String userID = authenticationService.extractSubjectFromToken(sessionToken);

            logger.info("PurchasePolicyService.enrollInLottery: verifying user exists for id {}", userID);
            User user = userRepository.findByID(userID);

            logger.info("PurchasePolicyService.enrollInLottery: Checking if user with id {} passed purchase policy checks", userID);
            //TODO: implement purchase policy checks for lottery enrollment

            logger.info("PurchasePolicyService.createLotteryPolicy: verifying event exists for id {}", eventID);
            Event e = eventRepo.findByID(String.valueOf(eventID));

            logger.info("PurchasePolicyService.createLotteryPolicy: Enrolling in lottery");
            e.enrollInLottery(user.getEmail());

            logger.info("PurchasePolicyService.createLotteryPolicy: Saving changes to repository");
            eventRepo.save(e);

            logger.info("User with ID: {} enrolled in lottery for event ID: {} successfully", user.getEmail(), eventID);
            return Result.makeOk(true);
        }
        catch (IllegalArgumentException e) {
            logger.error("PurchasePolicyService.enrollInLottery: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (IllegalStateException e) {
            logger.error("PurchasePolicyService.enrollInLottery: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        }
        catch (Exception e) {
            logger.error("An unexpected error occurred while enrolling in lottery for event ID: {}: {}", eventID, e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }
}