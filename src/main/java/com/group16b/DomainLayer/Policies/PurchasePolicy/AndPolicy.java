package com.group16b.DomainLayer.Policies.PurchasePolicy;


import java.util.ArrayList;
import java.util.List;

public class AndPolicy implements PurchasePolicy {
    private List<PurchasePolicy> policies = new ArrayList<>();

    public AndPolicy() {
        this.policies = new ArrayList<>();
    }

    public AndPolicy(List<PurchasePolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("AndPolicy must have at least one policy.");
        }
        this.policies = policies;
    }

    public List<PurchasePolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<PurchasePolicy> policies) {
        this.policies = policies;
    }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException {
        if (policies == null || policies.isEmpty()) {
            throw new PurchasePolicyException("AndPolicy must have at least one policy.");
        }

        for (PurchasePolicy policy : policies) {
            policy.validatePurchase(context);
        }
    }}