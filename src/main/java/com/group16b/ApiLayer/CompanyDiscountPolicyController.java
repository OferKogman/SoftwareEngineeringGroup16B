package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.DiscountPolicyService;
import com.group16b.ApplicationLayer.Records.DiscountPolicyRecord;

@RestController
@RequestMapping("/api/production-companies/{companyId}/discount-policy")
public class CompanyDiscountPolicyController extends BaseController{
    private final DiscountPolicyService discountPolicyService;

    public CompanyDiscountPolicyController(DiscountPolicyService discountPolicyService)
    {
        this.discountPolicyService=discountPolicyService;
    }

    @PostMapping
    public ResponseEntity<?> createCompanyDiscountPolicy(
        @RequestHeader("Authorization") String authToken,
        @RequestBody DiscountPolicyRecord record,
        @PathVariable("companyId") int companyId
    )
    {
        return executeWithNoReturnData(()-> discountPolicyService.createCompanyDiscountPolicy(authToken, companyId, record));
    }

    @GetMapping
    public ResponseEntity<?> getCompanyDiscountPolicy(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("companyId") int companyId
    )
    {
        return executeWithReturnData(()-> discountPolicyService.getCompanyDiscountPolicy(authToken, companyId));
    }

}
