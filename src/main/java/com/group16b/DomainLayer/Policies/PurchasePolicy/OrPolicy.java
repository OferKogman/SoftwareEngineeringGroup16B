package com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.util.List;

public class OrPolicy implements PurchasePolicy {
    private List<PurchasePolicy> policies;

    public OrPolicy(List<PurchasePolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("OrPolicy must have at least one policy.");
        }
        this.policies = policies;
    }

    public List<PurchasePolicy> getPolicies() { return policies; }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException { }
}