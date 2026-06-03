package com.group16b.DomainLayer.Policies.PurchasePolicy;

public class TicketAmountPolicy implements PurchasePolicy {
    private Integer minTickets; // null = no minimum
    private Integer maxTickets; // null = no maximum

    public TicketAmountPolicy(Integer minTickets, Integer maxTickets) {
        if (minTickets == null && maxTickets == null)
            throw new IllegalArgumentException("At least one of minTickets or maxTickets must be defined.");
        if (minTickets != null && minTickets < 1)
            throw new IllegalArgumentException("Minimum tickets cannot be less than 1.");
        if (maxTickets != null && maxTickets < 1)
            throw new IllegalArgumentException("Maximum tickets cannot be less than 1.");
        if (minTickets != null && maxTickets != null && minTickets > maxTickets)
            throw new IllegalArgumentException("Minimum tickets cannot exceed maximum tickets.");
        this.minTickets = minTickets;
        this.maxTickets = maxTickets;
    }

    public Integer getMinTickets() { return minTickets; }
    public Integer getMaxTickets() { return maxTickets; }

    @Override
    public void validatePurchase(PurchaseContext context) throws PurchasePolicyException {
        if (minTickets != null && context.ticketCount() < minTickets)
            throw new PurchasePolicyException("Must purchase at least " + minTickets + " ticket(s).");
        if (maxTickets != null && context.ticketCount() > maxTickets)
            throw new PurchasePolicyException("Cannot purchase more than " + maxTickets + " ticket(s).");
    }
}