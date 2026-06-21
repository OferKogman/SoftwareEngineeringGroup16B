package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.LotteryPolicyService;
import com.group16b.ApplicationLayer.DTOs.CreateLotteryPolicyRequestDTO;

@RestController
@RequestMapping("api/events/{eventId}/lottery")
public class LotteryPolicyController extends BaseController{
    private final LotteryPolicyService lotteryPolicyService;

    public LotteryPolicyController(LotteryPolicyService lotteryPolicyService)
    {
        this.lotteryPolicyService=lotteryPolicyService;
    }

    @PostMapping("/policy")
    public ResponseEntity<?> createLotteryPolicy(
        @PathVariable("eventId") int eventId,
        @RequestBody CreateLotteryPolicyRequestDTO request
    )
    {
        return executeWithNoReturnData(()-> lotteryPolicyService.createLotteryPolicy(eventId, request.lotteryID(), request.lotteryName(), request.winnerAmount(), request.lotteryRegistrationDueDate()));
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollInLottery(
        @PathVariable("eventId") int eventId)
    {
        return executeWithNoReturnData(()-> lotteryPolicyService.enrollInLottery(eventId));
    }

    @PostMapping("/results")
    public ResponseEntity<?> handleLotteryResults(
            @PathVariable("eventId") int eventId) 
    {
        return executeWithNoReturnData(() -> lotteryPolicyService.handleLotteryResults(eventId));
    }


    
}
