package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.DiscountPolicyService;
import com.group16b.ApplicationLayer.Records.ApplyCouponCodeRequest;
import com.group16b.ApplicationLayer.Records.DiscountPolicyRecord;

@RestController
public class EventDiscountPolicyController extends BaseController {
    private final DiscountPolicyService discountPolicyService;

    public EventDiscountPolicyController(DiscountPolicyService discountPolicyService) {
        this.discountPolicyService = discountPolicyService;
    }

    @PostMapping("/api/events/{eventId}/discount-policy")
    public ResponseEntity<?> createEventDiscountPolicy(
            @RequestHeader("Authorization") String authToken,
            @RequestBody DiscountPolicyRecord record,
            @PathVariable("eventId") int eventId) {
        return executeWithNoReturnData(
                () -> discountPolicyService.createEventDiscountPolicy(authToken, eventId, record));
    }

    @PutMapping("/api/events/{eventId}/discount-policy")
    public ResponseEntity<?> editEventDiscountPolicy(
            @RequestHeader("Authorization") String authToken,
            @RequestBody DiscountPolicyRecord newRecord,
            @PathVariable("eventId") int eventId) {
        return executeWithNoReturnData(
                () -> discountPolicyService.editEventDiscountPolicy(authToken, eventId, newRecord));
    }

    @GetMapping("/api/events/{eventId}/discount-policy")
    public ResponseEntity<?> getEventDiscountPolicy(
            @RequestHeader("Authorization") String authToken,
            @PathVariable("eventId") int eventId) {
        return executeWithReturnData(() -> discountPolicyService.getEventDiscountPolicy(authToken, eventId));
    }

    @PostMapping("/api/orders/{orderId}/coupon")
    public ResponseEntity<?> applyCoupon(
            @PathVariable("orderId") String orderId,
            @RequestBody ApplyCouponCodeRequest request) {
        return executeWithReturnData(() -> discountPolicyService.applyCoupon(orderId, request.couponCode()));
    }

}
