package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LotteryPolicy implements PurchasePolicy {

    private String lotteryName;
    private int winnerAmount;
    private LocalDateTime lotteryRegistrationDueDate;
    private Set<String> participants;
    private Map<String, String> winnersAndCodes;
    private Map<String, String> usedCodes;

    public LotteryPolicy(int lotteryID, String lotteryName, int winnerAmount,
            LocalDateTime lotteryRegistrationDueDate) {
        this.lotteryName = lotteryName;
        this.winnerAmount = winnerAmount;
        validateDate(lotteryRegistrationDueDate);
        this.lotteryRegistrationDueDate = lotteryRegistrationDueDate;
        this.participants = ConcurrentHashMap.newKeySet();
        this.winnersAndCodes = new ConcurrentHashMap<>();
        this.usedCodes = new ConcurrentHashMap<>();
    }

    public LotteryPolicy(LotteryPolicy other) {
        this.lotteryName = other.lotteryName;
        this.winnerAmount = other.winnerAmount;
        this.lotteryRegistrationDueDate = other.lotteryRegistrationDueDate;

        this.participants = new HashSet<>(other.participants);

        this.winnersAndCodes = new ConcurrentHashMap<>(other.winnersAndCodes);
        this.usedCodes = new ConcurrentHashMap<>(other.usedCodes);
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

    public Set<String> getParticipants() {
        return participants;
    }

    public synchronized void enrollInLottery(int eventID, String userID) {
        if (LocalDateTime.now().isAfter(lotteryRegistrationDueDate)) {
            throw new IllegalStateException("Lottery enrollment is closed since " + lotteryRegistrationDueDate + ".");
        }
        if (participants.contains(userID)) {
            throw new IllegalStateException("User is already enrolled in the lottery.");
        }
        participants.add(userID);
    }

    public synchronized void handleLotteryResults() {
        if(lotteryRegistrationDueDate.isBefore(LocalDateTime.now()))
            throw new IllegalStateException("Cannot handle lottery results before the registration due time passed.");
        List<String> winners = new ArrayList<>(participants);

        Collections.shuffle(winners);

        winners = winners.subList(0, Math.min(winnerAmount, winners.size()));

        for (String winnerID : winners) {
            String uniqueCode = UUID.randomUUID().toString();
            winnersAndCodes.put(uniqueCode, winnerID);
        }

        // TODO: Notify winners with their unique codes
    }

    public synchronized void validateLotteryCode(String code) {
        if (usedCodes.containsKey(code)) {
            throw new IllegalArgumentException("Lottery code has already been used.");
        }
        if (!winnersAndCodes.containsKey(code)) {
            throw new IllegalArgumentException("Invalid lottery code.");
        }
        String winnerID = winnersAndCodes.get(code);
        usedCodes.put(code, winnerID);
    }

    public synchronized void useCode(String code) {
        winnersAndCodes.remove(code);
    }

    public synchronized void renewLotteryCode(String code) {
        if (winnersAndCodes.containsKey(code) && usedCodes.containsKey(code)) {
            String userID = winnersAndCodes.get(code);
            winnersAndCodes.remove(code);
            winnersAndCodes.put(code, userID);
            usedCodes.remove(code);
        }
    }

    private void validateDate(LocalDateTime lotteryRegistrationDueDate) {
        if (lotteryRegistrationDueDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lottery registration due date cannot be in the past.");
        }
    }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException {
    }

}
