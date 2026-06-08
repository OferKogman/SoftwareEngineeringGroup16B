package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

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
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.ApplicationLayer.Records.PurchasePolicyRecord;
import com.group16b.DomainLayer.Policies.PurchasePolicy.AgePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MinTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MaxTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.TicketAmountPolicy;
import java.util.Set;

@Service
public class PurchasePolicyService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final IAuthenticationService authenticationService;
    private final IEventRepository eventRepo;
    private final IRepository<User> userRepository;
    private final IProductionCompanyRepository productionCompanyRepository;

    public PurchasePolicyService(IAuthenticationService authenticationService,
            IProductionCompanyRepository productionCompanyRepository, IEventRepository eventRepo,
            IRepository<User> userRepository) {
        this.authenticationService = authenticationService;
        this.productionCompanyRepository = productionCompanyRepository;
        this.eventRepo = eventRepo;
        this.userRepository = userRepository;
    }

    public Result<Boolean> createLotteryPolicy(String sessionToken, int eventID, int lotteryID, String lotteryName,
            int winnerAmount, LocalDateTime lotteryRegistrationDueDate) {
        try {
            logger.info(
                    "PurchasePolicyService.createLotteryPolicy: Received request to create lottery policy for event ID: {}",
                    eventID);
            if (!authenticationService.validateToken(sessionToken)) {
                logger.warn("PurchasePolicyService.createLotteryPolicy: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            if (!authenticationService.isUserToken(sessionToken)) {
                logger.warn("PurchasePolicyService.createLotteryPolicy: Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            String userID = authenticationService.extractSubjectFromToken(sessionToken);

            logger.info("PurchasePolicyService.createLotteryPolicy: verifying user exists for id {}", userID);
            User user = userRepository.findByID(userID);

            logger.info(
                    "PurchasePolicyService.createLotteryPolicy: retrieving production company for creating a lottery policy");
            ProductionCompany company = productionCompanyRepository.findByID(
                    String.valueOf(eventRepo.findByID(String.valueOf(eventID)).getEventProductionCompanyID()));

            logger.info("PurchasePolicyService.createLotteryPolicy: Checking user permissions for userID: {}",
                    user.getEmail());
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            logger.info(
                    "PurchasePolicyService.createLotteryPolicy: Creating lottery policy with ID: {}, Name: {}, Winner Amount: {}, Registration Due Date: {}",
                    lotteryID, lotteryName, winnerAmount, lotteryRegistrationDueDate);
            LotteryPolicy lotteryPolicy = new LotteryPolicy(lotteryID, lotteryName, winnerAmount,
                    lotteryRegistrationDueDate);

            while (true) {
                logger.info("PurchasePolicyService.createLotteryPolicy: verifying event exists for id {}", eventID);
                Event e = eventRepo.findByID(String.valueOf(eventID));

                logger.info("PurchasePolicyService.createLotteryPolicy: Adding lottery policy to event with ID: {}",
                        eventID);
                e.setLotteryPolicy(lotteryPolicy);

                logger.info("PurchasePolicyService.createLotteryPolicy: saving changes to repository");
                try {
                    eventRepo.save(e);
                    break;
                } catch (OptimisticLockingFailureException err) {
                    logger.warn("Event got edit retrying");
                }
            }

            logger.info("PurchasePolicyService.createLotteryPolicy: Lottery policy added to event successfully");
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("PurchasePolicyService.createLotteryPolicy: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("PurchasePolicyService.createLotteryPolicy: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error(
                    "PurchasePolicyService.createLotteryPolicy: An unexpected error occurred while creating a lottery"
                            + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Boolean> enrollInLottery(String sessionToken, int eventID) {
        try {
            logger.info("PurchasePolicyService.enrollInLottery: Received request to enroll in lottery for event ID: {}",
                    eventID);
            if (!authenticationService.validateToken(sessionToken)) {
                logger.warn("PurchasePolicyService.enrollInLottery: Invalid or expired session token.");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            if (!authenticationService.isUserToken(sessionToken)) {
                logger.warn("PurchasePolicyService.enrollInLottery: Expected a user session token");
                return Result.makeFail("Authentication failed. Please log in again.");
            }
            String userID = authenticationService.extractSubjectFromToken(sessionToken);

            logger.info("PurchasePolicyService.enrollInLottery: verifying user exists for id {}", userID);
            User user = userRepository.findByID(userID);

            logger.info(
                    "PurchasePolicyService.enrollInLottery: Checking if user with id {} passed purchase policy checks",
                    userID);
            // TODO: implement purchase policy checks for lottery enrollment

            while (true) {
                logger.info("PurchasePolicyService.createLotteryPolicy: verifying event exists for id {}", eventID);
                Event e = eventRepo.findByID(String.valueOf(eventID));

                logger.info("PurchasePolicyService.createLotteryPolicy: Enrolling in lottery");
                e.enrollInLottery(user.getEmail());

                logger.info("PurchasePolicyService.createLotteryPolicy: Saving changes to repository");
                try {
                    eventRepo.save(e);
                    break;
                } catch (OptimisticLockingFailureException err) {
                    logger.warn("Event got edit retrying");
                }
            }

            logger.info("User with ID: {} enrolled in lottery for event ID: {} successfully", user.getEmail(), eventID);
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("PurchasePolicyService.enrollInLottery: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("PurchasePolicyService.enrollInLottery: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while enrolling in lottery for event ID: {}: {}", eventID,
                    e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Boolean> createCompanyPurchasePolicy(String sessionToken, int companyID, PurchasePolicyRecord record) {
        try {
            logger.info("PurchasePolicyService.createCompanyPurchasePolicy: Received request for company ID: {}", companyID);
            if (!authenticationService.validateToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            if (!authenticationService.isUserToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            userRepository.findByID(userID);

            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(companyID));
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);
            company.addPurchasePolicy(buildPolicy(record));
            productionCompanyRepository.save(company);

            logger.info("PurchasePolicyService.createCompanyPurchasePolicy: Policy added to company {} successfully", companyID);
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("PurchasePolicyService.createCompanyPurchasePolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("PurchasePolicyService.createCompanyPurchasePolicy: Unexpected error: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    private PurchasePolicy buildPolicy(PurchasePolicyRecord record) {
        return switch (record.type()) {
            case "AGE" -> new AgePolicy(record.minAge(), record.maxAge());
            case "MIN_TICKETS" -> new MinTicketsPolicy(record.minTickets());
            case "MAX_TICKETS" -> new MaxTicketsPolicy(record.maxTickets());
            case "TICKET_AMOUNT" -> new TicketAmountPolicy(record.minTickets(), record.maxTickets());
            default -> throw new IllegalArgumentException("Unknown policy type: " + record.type());
        };
    }

    public Result<Boolean> createEventPurchasePolicy(String sessionToken, int eventID, PurchasePolicyRecord record) {
        try {
            logger.info("PurchasePolicyService.createEventPurchasePolicy: Received request for event ID: {}", eventID);
            if (!authenticationService.validateToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            if (!authenticationService.isUserToken(sessionToken))
                return Result.makeFail("Authentication failed. Please log in again.");
            String userID = authenticationService.extractSubjectFromToken(sessionToken);
            userRepository.findByID(userID);

            Event event = eventRepo.findByID(String.valueOf(eventID));
            ProductionCompany company = productionCompanyRepository.findByID(String.valueOf(event.getEventProductionCompanyID()));
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);
            event.addEventPurchasePolicy(buildPolicy(record));
            eventRepo.save(event);

            logger.info("PurchasePolicyService.createEventPurchasePolicy: Policy added to event {} successfully", eventID);
            return Result.makeOk(true);
        } catch (IllegalArgumentException e) {
            logger.error("PurchasePolicyService.createEventPurchasePolicy: {}", e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("PurchasePolicyService.createEventPurchasePolicy: Unexpected error: {}", e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

}
