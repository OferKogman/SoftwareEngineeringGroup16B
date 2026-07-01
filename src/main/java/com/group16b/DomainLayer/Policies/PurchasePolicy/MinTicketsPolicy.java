package com.group16b.DomainLayer.Policies.PurchasePolicy;

public class MinTicketsPolicy implements PurchasePolicy {
    private int minTicketsPerTransaction;

    public MinTicketsPolicy() {
    }

    public MinTicketsPolicy(int minTicketsPerTransaction) {
        if (minTicketsPerTransaction < 1) {
            throw new IllegalArgumentException("Customer must buy at least 1 ticket.");
        }
        this.minTicketsPerTransaction = minTicketsPerTransaction;
    }

    public int getMinTicketsPerTransaction() {
        return this.minTicketsPerTransaction;
    }

    public void setMinTicketsPerTransaction(int minTicketsPerTransaction) {
        if (minTicketsPerTransaction < 1) {
            throw new IllegalArgumentException("Customer must buy at least 1 ticket.");
        }
        this.minTicketsPerTransaction = minTicketsPerTransaction;
    }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException {
        if (context.ticketCount() < minTicketsPerTransaction) {
            throw new PurchasePolicyException("Must purchase at least "
                    + minTicketsPerTransaction + " ticket(s).");
        }
    }
}