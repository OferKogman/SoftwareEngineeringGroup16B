package com.group16b.DomainLayer.Policies.PurchasePolicy;

public class PurchasePolicyException extends RuntimeException {
    public PurchasePolicyException(String message) {
        super(message);
    }
}