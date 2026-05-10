package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Event.IEventRepositoryMapImpl;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;

public class PurchasePolicyService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final IEventRepository eventRepo = IEventRepositoryMapImpl.getInstance();

    

    public Result<LotteryPolicy> createLotteryPolicy(int eventID, int lotteryID, String lotteryName, int winnerAmount, LocalDateTime lotteryRegistrationDueDate) {
        logger.info("Creating lottery policy with ID: {}, Name: {}, Winner Amount: {}, Registration Due Date: {}", lotteryID, lotteryName, winnerAmount, lotteryRegistrationDueDate);
        LotteryPolicy lotteryPolicy = new LotteryPolicy(lotteryID, lotteryName, winnerAmount, lotteryRegistrationDueDate);
        logger.info("Lottery policy created successfully: {}", lotteryPolicy);
        
        logger.info("Adding lottery policy to event with ID: {}", eventID);
        eventRepo.getEventByID(eventID).addEventPurchasePolicy(lotteryPolicy);
        logger.info("Lottery policy added to event successfully");
        return Result.makeOk(lotteryPolicy);
    }
}
