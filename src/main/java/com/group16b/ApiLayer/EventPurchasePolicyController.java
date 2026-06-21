package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.PurchasePolicyService;
import com.group16b.ApplicationLayer.Records.PurchasePolicyRecord;

@RestController
@RequestMapping("/api/events/{eventId}/purchase-policy")
public class EventPurchasePolicyController extends BaseController{
    private final PurchasePolicyService purchasePolicyService;

    public EventPurchasePolicyController(PurchasePolicyService purchasePolicyService)
    {
        this.purchasePolicyService=purchasePolicyService;
    }

    @PostMapping
    public ResponseEntity<?> createEventPurchasePolicy(
        @RequestHeader("Authorization") String authToken,
        @RequestBody PurchasePolicyRecord record,
        @PathVariable("eventId") int eventId
    )
    {
        return executeWithNoReturnData(()-> purchasePolicyService.createEventPurchasePolicy(authToken, eventId, record));
    }

    @PutMapping
    public ResponseEntity<?> editEventPurchasePolicy(
        @RequestHeader("Authorization") String authToken,
        @RequestBody PurchasePolicyRecord newRecord,
        @PathVariable("eventId") int eventId
    )
    {
        return executeWithNoReturnData(()->purchasePolicyService.editEventPurchasePolicy(authToken, eventId, newRecord));
    }

    @GetMapping
    public ResponseEntity<?> getEventPurchasePolicy(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventId") int eventId
    )
    {
        return executeWithReturnData(()-> purchasePolicyService.getEventPurchasePolicy(authToken, eventId));
    }


}
