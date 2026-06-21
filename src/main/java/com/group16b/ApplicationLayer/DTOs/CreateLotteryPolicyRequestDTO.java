package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDateTime;

public record CreateLotteryPolicyRequestDTO (
    int lotteryID,
    String lotteryName, 
    int winnerAmount,
    LocalDateTime lotteryRegistrationDueDate){}
    

