package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
        this.participants = new HashSet<>();
        this.winnersAndCodes = new HashMap<>();
        this.usedCodes = new HashMap<>();
    }

    public LotteryPolicy(LotteryPolicy other) {
        this.lotteryName = other.lotteryName;
        this.winnerAmount = other.winnerAmount;
        this.lotteryRegistrationDueDate = other.lotteryRegistrationDueDate;

        this.participants = other.participants == null ? new HashSet<>() : new HashSet<>(other.participants);

        this.winnersAndCodes = other.winnersAndCodes == null ? new HashMap<>() : new HashMap<>(other.winnersAndCodes);
        this.usedCodes = other.usedCodes == null ? new HashMap<>() : new HashMap<>(other.usedCodes);
    }

    public LotteryPolicy() {
        this.participants = new HashSet<>();
        this.winnersAndCodes = new HashMap<>();
        this.usedCodes = new HashMap<>();
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
        return new HashSet<>(participants);
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants == null ? new HashSet<>() : new HashSet<>(participants);
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
        if (LocalDateTime.now().isBefore(lotteryRegistrationDueDate)) {
            throw new IllegalStateException("Cannot handle lottery results before the registration due time passed.");
        }

        if (!winnersAndCodes.isEmpty()) {
            throw new IllegalStateException("Lottery results were already handled.");
        }

        List<String> winners = new ArrayList<>(participants);

        Collections.shuffle(winners);

        winners = winners.subList(0, Math.min(winnerAmount, winners.size()));

        for (String winnerID : winners) {
            String uniqueCode = UUID.randomUUID().toString();
            winnersAndCodes.put(uniqueCode, winnerID);
        }
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
        if (lotteryRegistrationDueDate == null) {
            throw new IllegalArgumentException("Lottery registration due date is required.");
        }
        if (lotteryRegistrationDueDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lottery registration due date cannot be in the past.");
        }
    }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException {
    }

    // FOR TESTS
    @JsonIgnore
    public List<String> getWinners() {
        return new ArrayList<>(winnersAndCodes.values());
    }

    public Map<String, String> getWinnersAndCodes() {
        return new HashMap<>(winnersAndCodes);
    }

    public void setWinnersAndCodes(Map<String, String> winnersAndCodes) {
        this.winnersAndCodes = winnersAndCodes == null ? new HashMap<>() : new HashMap<>(winnersAndCodes);
    }

    public Map<String, String> getUsedCodes() {
        return new HashMap<>(usedCodes);
    }

    public void setUsedCodes(Map<String, String> usedCodes) {
        this.usedCodes = usedCodes == null ? new HashMap<>() : new HashMap<>(usedCodes);
    }

    @JsonIgnore
    public Set<String> getLosers() {
        Set<String> losers = new HashSet<>(participants);
        losers.removeAll(winnersAndCodes.values());
        return losers;
    }
}
