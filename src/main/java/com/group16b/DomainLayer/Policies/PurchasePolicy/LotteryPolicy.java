package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LotteryPolicy implements PurchasePolicy {
    
    private int lotteryID;
    private String lotteryName;
    private int winnerAmount;
    private LocalDateTime lotteryRegistrationDueDate;
    private List<Integer> participants;

    public LotteryPolicy(int lotteryID, String lotteryName, int winnerAmount, LocalDateTime lotteryRegistrationDueDate) {
        this.lotteryID = lotteryID;
        this.lotteryName = lotteryName;
        this.winnerAmount = winnerAmount;
        this.lotteryRegistrationDueDate = lotteryRegistrationDueDate;
        this.participants = new ArrayList<>();
    }

    public void enrollInLottery(int eventID, int userID) {
        // Implementation for enrolling in the lottery
    }

    public void handleLotteryResults() {
        // Implementation for handling lottery results
    }

}
