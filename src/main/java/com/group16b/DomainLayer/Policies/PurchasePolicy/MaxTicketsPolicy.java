package com.group16b.DomainLayer.Policies.PurchasePolicy;

public class MaxTicketsPolicy implements PurchasePolicy {
    private int maxTicketsPerTransaction;

    public MaxTicketsPolicy(int maxTicketsPerTransaction) {
        if (maxTicketsPerTransaction <= 0) {
            throw new IllegalArgumentException("Maximum ticket limit must be greater than 0.");
        }
        this.maxTicketsPerTransaction = maxTicketsPerTransaction;
    }

    public int getMaxTicketsPerTransaction() {
        return this.maxTicketsPerTransaction;
    }

    public void setMaxTicketsPerTransaction(int maxTicketsPerTransaction) {
        if (maxTicketsPerTransaction <= 0) {
            throw new IllegalArgumentException("Maximum ticket limit must be greater than 0.");
        }
        this.maxTicketsPerTransaction = maxTicketsPerTransaction;
    }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException { }
}