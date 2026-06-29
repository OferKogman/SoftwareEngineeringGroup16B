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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;

@Embeddable
public class LotteryPolicy implements PurchasePolicy {
    private String lotteryName;
    private int winnerAmount;
    private LocalDateTime lotteryRegistrationDueDate;
    
    @ElementCollection
    @CollectionTable(
        name = "lottery_participants",
        joinColumns = @JoinColumn(name = "event_id")
    )
    @Column(name = "user_id",nullable = false)
    private Set<String> participants=new HashSet<>();

    @ElementCollection
    @CollectionTable(
        name = "lottery_winners",
        joinColumns = @JoinColumn(name = "event_id")
    )
    @MapKeyColumn(name = "code")
    @Column(name = "user_id",nullable = false)
    private Map<String, String> winnersAndCodes=new HashMap<>();;

    @ElementCollection
    @CollectionTable(
        name = "lottery_used_codes",
        joinColumns = @JoinColumn(name = "event_id")
    )
    @MapKeyColumn(name = "code")
    @Column(name = "user_id",nullable = false)
    private Map<String, String> usedCodes=new HashMap<>();

    public LotteryPolicy(int redundendButTests, String lotteryName, int winnerAmount,LocalDateTime lotteryRegistrationDueDate) {
        this.lotteryName = lotteryName;
        this.winnerAmount = winnerAmount;
        validateDate(lotteryRegistrationDueDate);
        this.lotteryRegistrationDueDate = lotteryRegistrationDueDate;
    }

    public LotteryPolicy(LotteryPolicy other) {
        this.lotteryName = other.lotteryName;
        this.winnerAmount = other.winnerAmount;
        this.lotteryRegistrationDueDate = other.lotteryRegistrationDueDate;

        this.participants = new HashSet<>(other.participants);

        this.winnersAndCodes = new HashMap<>(other.winnersAndCodes);
        this.usedCodes = new HashMap<>(other.usedCodes);
    }

    public LotteryPolicy(){}

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
        if(lotteryRegistrationDueDate.isAfter(LocalDateTime.now()))
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

    //FOR TESTS
    public List<String> getWinners() {
        return new ArrayList<>(winnersAndCodes.values());
    }
}
