package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDateTime;

import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;

public class LotteryDTO {
    private String lotteryName;
    private int winnerAmount;
    private LocalDateTime lotteryRegistrationDueDate;
    
    public LotteryDTO(LotteryPolicy lp)
    {
        if(lp==null)
            throw new IllegalArgumentException("cant create Lottery dto when lottery is null");
        this.lotteryName=lp.getLotteryName();
        this.winnerAmount=lp.getWinnerAmount();
        this.lotteryRegistrationDueDate=lp.getLotteryRegistrationDueDate();
    }

    public String getLotteryName()
    {
        return lotteryName;
    }
    public int getWinnerAmount()
    {
        return winnerAmount;
    }
    public LocalDateTime getLotteryRegistrationDueDate()
    {
        return lotteryRegistrationDueDate;
    }
    
}
