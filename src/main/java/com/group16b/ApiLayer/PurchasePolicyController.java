package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.PurchasePolicyService;
import com.group16b.ApplicationLayer.DTOs.CreateLotteryPolicyRequestDTO;

@RestController
@RequestMapping("/events/{eventId}/purchase-policies")
public class PurchasePolicyController extends BaseController {
    private final PurchasePolicyService purchasePolicyService;

    public PurchasePolicyController(PurchasePolicyService purchasePolicyService) {
        this.purchasePolicyService = purchasePolicyService;
    }

    @PostMapping("/lottery")
    public ResponseEntity<?> createLotteryPolicy(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventId") int eventId,
        @RequestBody CreateLotteryPolicyRequestDTO request
    ) {
        return executeWithNoReturnData(() -> purchasePolicyService.createLotteryPolicy(
            authToken, 
            eventId, 
            request.getLotteryID(), 
            request.getLotteryName(), 
            request.getWinnerAmount(), 
            request.getLotteryRegistrationDueDate()
        ));
    }

    @PostMapping("/lottery/enrollment")
    public ResponseEntity<?> enrollInLottery(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventId") int eventId
    ) {
        return executeWithNoReturnData(() -> purchasePolicyService.enrollInLottery(authToken, eventId));
    }
    
    
}
