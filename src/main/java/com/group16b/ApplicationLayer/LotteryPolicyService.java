package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.group16b.ApplicationLayer.Exceptions.AuthException;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Interfaces.IBusinessNotificationService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;


@Service
@Transactional
public class LotteryPolicyService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final IEventRepository eventRepository;
    private final IRepository<User> userRepository;
    private final IProductionCompanyRepository productionCompanyRepository;
    private final IAuthenticationService authenticationService;
    private IBusinessNotificationService businessNotifications;

    public LotteryPolicyService(IEventRepository eventRepository, IRepository<User> userRepository,
            IProductionCompanyRepository productionCompanyRepository, IAuthenticationService authenticationService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.productionCompanyRepository = productionCompanyRepository;
        this.authenticationService = authenticationService;
    }

    public Result<Void> createLotteryPolicy(int eventID, int lotteryID, String lotteryName, int winnerAmount,
            LocalDateTime lotteryRegistrationDueDate, String sessionToken) {
        try {
            logger.info(
                    "LotteryPolicyService.createLotteryPolicy: Received request to create lottery policy for event ID: {}",
                    eventID);

            String userID = validateRoleAndGetUserId(sessionToken);

            logger.info("LotteryPolicyService.createLotteryPolicy: verifying user exists for id {}", userID);
            User user = userRepository.findByID(userID);

            logger.info(
                    "LotteryPolicyService.createLotteryPolicy: retrieving production company for creating a lottery policy");
            ProductionCompany company = productionCompanyRepository.findByID(
                    String.valueOf(eventRepository.findByID(String.valueOf(eventID)).getEventProductionCompanyID()));

            logger.info("LotteryPolicyService.createLotteryPolicy: Checking user permissions for userID: {}",
                    user.getEmail());
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            logger.info(
                    "LotteryPolicyService.createLotteryPolicy: Creating lottery policy with ID: {}, Name: {}, Winner Amount: {}, Registration Due Date: {}",
                    lotteryID, lotteryName, winnerAmount, lotteryRegistrationDueDate);
            LotteryPolicy lotteryPolicy = new LotteryPolicy(lotteryID, lotteryName, winnerAmount,
                    lotteryRegistrationDueDate);

            while (true) {
                logger.info("LotteryPolicyService.createLotteryPolicy: verifying event exists for id {}", eventID);
                Event e = eventRepository.findByID(String.valueOf(eventID));

                logger.info("LotteryPolicyService.createLotteryPolicy: Adding lottery policy to event with ID: {}",
                        eventID);
                e.setLotteryPolicy(lotteryPolicy);

                logger.info("LotteryPolicyService.createLotteryPolicy: saving changes to repository");
                try {
                    eventRepository.save(e);
                    break;
                } catch (OptimisticLockingFailureException err) {
                    logger.warn("LotteryPolicyService.createLotteryPolicy: Event got edit retrying");
                }
            }

            notifyGeneric(userID, "Lottery policy created for event " + eventID + ".");

            logger.info("LotteryPolicyService.createLotteryPolicy: Lottery policy added to event successfully");
            return Result.makeOk(null);
        } catch (AuthException e) {
            logger.warn("LotteryPolicyService.createLotteryPolicy: AuthException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("LotteryPolicyService.createLotteryPolicy: IllegalArgumentException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("LotteryPolicyService.createLotteryPolicy: IllegalStateException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error(
                    "LotteryPolicyService.createLotteryPolicy: An unexpected error occurred while creating a lottery"
                            + e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Void> enrollInLottery(int eventID, String sessionToken) {
        try {
            logger.info("LotteryPolicyService.enrollInLottery: Received request to enroll in lottery for event ID: {}",
                    eventID);
            String userID = validateRoleAndGetUserId(sessionToken);

            logger.info("LotteryPolicyService.enrollInLottery: verifying user exists for id {}", userID);
            User user = userRepository.findByID(userID);

            logger.info(
                    "LotteryPolicyService.enrollInLottery: Checking if user with id {} passed purchase policy checks",
                    userID);
            // TODO: implement purchase policy checks for lottery enrollment
            // so it is not implemented yet?

            while (true) {
                logger.info("LotteryPolicyService.createLotteryPolicy: verifying event exists for id {}", eventID);
                Event e = eventRepository.findByID(String.valueOf(eventID));

                logger.info("LotteryPolicyService.createLotteryPolicy: Enrolling in lottery");
                e.enrollInLottery(user.getEmail());// hmm what? just how old is this function? gonna leave it here as a
                                                   // remenent of better times

                logger.info("LotteryPolicyService.createLotteryPolicy: Saving changes to repository");
                try {
                    eventRepository.save(e);
                    break;
                } catch (OptimisticLockingFailureException err) {
                    logger.warn("Event got edit retrying");
                }
            }

            notifyLotteryEnrolled(userID, eventID);

            logger.info("User with ID: {} enrolled in lottery for event ID: {} successfully", user.getEmail(), eventID);
            return Result.makeOk(null);
        } catch (AuthException e) {
            logger.warn("LotteryPolicyService.enrollInLottery: AuthException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("LotteryPolicyService.enrollInLottery: IllegalArgumentException:" + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("LotteryPolicyService.enrollInLottery: IllegalStateException:" + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while enrolling in lottery for event ID: {}: {}", eventID,
                    e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    public Result<Void> handleLotteryResults(int eventID, String sessionToken) {
        try {
            logger.info(
                    "LotteryPolicyService.handleLotteryResults: Received request to handle the lottery results for event ID: {}",
                    eventID);
            String userID = validateRoleAndGetUserId(sessionToken);

            logger.info("LotteryPolicyService.handleLotteryResults: verifying user exists for id {}", userID);
            User user = userRepository.findByID(userID);

            logger.info(
                    "LotteryPolicyService.handleLotteryResults: Checking if user with id {} is permitted to finalize lottery",
                    userID);
            ProductionCompany company = productionCompanyRepository.findByID(
                    String.valueOf(eventRepository.findByID(String.valueOf(eventID)).getEventProductionCompanyID()));
            company.validateUserPermissions(userID, ManagerPermissions.PURCHASE_POLICY);

            Map<String, String> winnerCodes = new HashMap<>();

            while (true) {
                logger.info("LotteryPolicyService.handleLotteryResults: verifying event exists for id {}", eventID);
                Event e = eventRepository.findByID(String.valueOf(eventID));

                logger.info("LotteryPolicyService.handleLotteryResults: settling lottery");
                e.handleLotteryResults();
                winnerCodes = e.getLotteryPolicy().copyWinnersAndCodesForNotification();

                logger.info("LotteryPolicyService.handleLotteryResults: Saving changes to repository");
                try {
                    eventRepository.save(e);
                    break;
                } catch (OptimisticLockingFailureException err) {
                    logger.warn("LotteryPolicyService.handleLotteryResults: Event got edit retrying");
                }
            }

            notifyLotteryResultsHandled(userID, eventID);
            notifyLotteryWinners(eventID, winnerCodes);

            logger.info("User with ID: {} finalized the lottery for event ID: {} successfully", user.getEmail(),
                    eventID);
            return Result.makeOk(null);
        } catch (AuthException e) {
            logger.warn("LotteryPolicyService.handleLotteryResults: AuthException: " + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("LotteryPolicyService.handleLotteryResults: IllegalArgumentException:" + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("LotteryPolicyService.handleLotteryResults: IllegalStateException:" + e.getMessage());
            return Result.makeFail(e.getMessage());
        } catch (Exception e) {
            logger.error(
                    "LotteryPolicyService.handleLotteryResults: An unexpected error occurred while handling lottery results for event ID: {}: {}",
                    eventID,
                    e.getMessage());
            return Result.makeFail("An unexpected error occurred: " + e.getMessage());
        }
    }

    private String validateRoleAndGetUserId(String sessionToken) {
        if (!authenticationService.validateToken(sessionToken)) {
            throw new AuthException("Invalid session token.");
        }
        if (!authenticationService.isUserToken(sessionToken)) {
            throw new AuthException("Only users are allowed to perform operation");
        }
        String userID = (authenticationService.extractSubjectFromToken(sessionToken));
        userRepository.findByID(userID);
        return userID;
    }

    @Autowired(required = false)
    public void setBusinessNotifications(IBusinessNotificationService businessNotifications) {
        this.businessNotifications = businessNotifications;
    }

    private void notifyGeneric(String userID, String message) {
        if (businessNotifications != null) {
            businessNotifications.generic(userID, message);
        }
    }

    private void notifyLotteryEnrolled(String userID, int eventID) {
        if (businessNotifications != null) {
            businessNotifications.lotteryEnrolled(userID, eventID);
        }
    }

    private void notifyLotteryResultsHandled(String userID, int eventID) {
        if (businessNotifications != null) {
            businessNotifications.lotteryResultsHandled(userID, eventID);
        }
    }

    private void notifyLotteryWinners(int eventID, Map<String, String> winnerCodes) {
        if (businessNotifications == null || winnerCodes == null || winnerCodes.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : winnerCodes.entrySet()) {
            String lotteryCode = entry.getKey();
            String winnerID = entry.getValue();

            businessNotifications.generic(
                    winnerID,
                    "You won the lottery for event " + eventID + ". Your lottery code is: " + lotteryCode);
        }
    }

}
