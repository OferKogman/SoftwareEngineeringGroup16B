package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LotteryPolicy implements PurchasePolicy {
    
    private int lotteryID;
    private String lotteryName;
    private int winnerAmount;
    private LocalDateTime lotteryRegistrationDueDate;
    private Set<Integer> participants;
    private Map<String, Integer> winnersAndCodes;

    public LotteryPolicy(int lotteryID, String lotteryName, int winnerAmount, LocalDateTime lotteryRegistrationDueDate) {
        this.lotteryID = lotteryID;
        this.lotteryName = lotteryName;
        this.winnerAmount = winnerAmount;
        validateDate(lotteryRegistrationDueDate);
        this.lotteryRegistrationDueDate = lotteryRegistrationDueDate;
        this.participants = ConcurrentHashMap.newKeySet();
        this.winnersAndCodes = new ConcurrentHashMap<>();
    }

    public int getLotteryID() {
        return lotteryID;
    }   

    public String getLotteryName() {
        return lotteryName;
    }

    public void setLotteryName(String lotteryName) {
        this.lotteryName = lotteryName;
    }

    public int getWinnerAmount() {
        return winnerAmount;
    }

    public void setWinnerAmount(int winnerAmount) {
        this.winnerAmount = winnerAmount;
    }

    public LocalDateTime getLotteryRegistrationDueDate() {
        return lotteryRegistrationDueDate;
    }

    public void setLotteryRegistrationDueDate(LocalDateTime lotteryRegistrationDueDate) {
        validateDate(lotteryRegistrationDueDate);
        this.lotteryRegistrationDueDate = lotteryRegistrationDueDate;
    }

    public Set<Integer> getParticipants() {
        return participants;
    }

    public synchronized void enrollInLottery(int eventID, int userID) {
        if(LocalDateTime.now().isAfter(lotteryRegistrationDueDate)) {
            throw new IllegalStateException("Lottery enrollment is closed since " + lotteryRegistrationDueDate + ".");
        }
        if(participants.contains(userID)) {
            throw new IllegalStateException("User is already enrolled in the lottery.");
        }
        participants.add(userID);
    }

    public synchronized void handleLotteryResults() {
        List<Integer> winners = new ArrayList<>(participants);

        Collections.shuffle(winners);

        winners = winners.subList(0, Math.min(winnerAmount, winners.size()));

        for(Integer winnerID : winners) {
            String uniqueCode = UUID.randomUUID().toString();
            winnersAndCodes.put(uniqueCode, winnerID);
        }

        //TODO: Notify winners with their unique codes
    }

    private void validateDate(LocalDateTime lotteryRegistrationDueDate) {
        if(lotteryRegistrationDueDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lottery registration due date cannot be in the past.");
        }
    }

}
