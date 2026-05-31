package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDateTime;

public class CreateLotteryPolicyRequestDTO {
    private int lotteryID;
    private String lotteryName;
    private int winnerAmount;
    private LocalDateTime lotteryRegistrationDueDate;

    public CreateLotteryPolicyRequestDTO() {}

    public int getLotteryID() {
        return lotteryID;
    }

    public String getLotteryName() {
        return lotteryName;
    }

    public int getWinnerAmount() {
        return winnerAmount;
    }

    public LocalDateTime getLotteryRegistrationDueDate() {
        return lotteryRegistrationDueDate;
    }

    public void setLotteryID(int lotteryID) {
        this.lotteryID = lotteryID;
    }
    public void setLotteryName(String lotteryName) {
        this.lotteryName = lotteryName;
    }
    public void setWinnerAmount(int winnerAmount) {
        this.winnerAmount = winnerAmount;
    }
    public void setLotteryRegistrationDueDate(LocalDateTime lotteryRegistrationDueDate) {
        this.lotteryRegistrationDueDate = lotteryRegistrationDueDate;
    }

}
