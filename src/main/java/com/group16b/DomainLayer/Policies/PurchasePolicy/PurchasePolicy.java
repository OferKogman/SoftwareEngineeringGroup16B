package com.group16b.DomainLayer.Policies.PurchasePolicy;

public interface PurchasePolicy {
    void validatePurchase(PurchaseContext context) throws PurchasePolicyException;
}