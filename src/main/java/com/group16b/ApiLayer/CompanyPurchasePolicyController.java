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
@RequestMapping("/api/production-companies/{companyId}/purchase-policy")
public class CompanyPurchasePolicyController extends BaseController{
    private final PurchasePolicyService purchasePolicyService;

    public CompanyPurchasePolicyController(PurchasePolicyService purchasePolicyService)
    {
        this.purchasePolicyService=purchasePolicyService;
    }

    @PostMapping
    public ResponseEntity<?> createCompanyPurchasePolicy(
        @RequestHeader("Authorization") String authToken,
        @RequestBody PurchasePolicyRecord record,
        @PathVariable("companyId") int companyId
    )
    {
        return executeWithNoReturnData(()-> purchasePolicyService.createCompanyPurchasePolicy(authToken, companyId, record));
    }
    
    @PutMapping
    public ResponseEntity<?> editCompanyPurchasePolicy(
        @RequestHeader("Authorization") String authToken,
        @RequestBody PurchasePolicyRecord newRecord,
        @PathVariable("companyId") int companyId
    )
    {
        return executeWithNoReturnData(()->purchasePolicyService.editCompanyPurchasePolicy(authToken, companyId, newRecord));
    }

    @GetMapping
    public ResponseEntity<?> getCompanyPurchasePolicy(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("companyId") int companyId
    )
    {
        return executeWithReturnData(()-> purchasePolicyService.getCompanyPurchasePolicy(authToken, companyId));
    }


}
