package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.util.List;

public class AndPolicy implements PurchasePolicy {
    private List<PurchasePolicy> policies;

    public AndPolicy(List<PurchasePolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("AndPolicy must have at least one policy.");
        }
        this.policies = policies;
    }

    public List<PurchasePolicy> getPolicies() { return policies; }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException {
        for (PurchasePolicy policy : policies)
            policy.validatePurchase(context);
    }}