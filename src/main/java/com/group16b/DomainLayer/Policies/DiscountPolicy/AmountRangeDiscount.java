package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class AmountRangeDiscount implements DiscountPolicy {
    private Integer minTickets;
    private Integer maxTickets;
    private double discountPercentage;

    public AmountRangeDiscount(Integer minTickets, Integer maxTickets, double discountPercentage) {
        if (minTickets == null && maxTickets == null)
            throw new IllegalArgumentException("At least one of minTickets or maxTickets must be defined.");
        if (minTickets != null && minTickets < 1)
            throw new IllegalArgumentException("Minimum tickets must be at least 1.");
        if (maxTickets != null && maxTickets < 1)
            throw new IllegalArgumentException("Maximum tickets must be at least 1.");
        if (minTickets != null && maxTickets != null && minTickets > maxTickets)
            throw new IllegalArgumentException("Minimum tickets cannot exceed maximum.");
        if (discountPercentage < 0 || discountPercentage > 100)
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        this.minTickets = minTickets;
        this.maxTickets = maxTickets;
        this.discountPercentage = discountPercentage;
    }


    public boolean isMet(DiscountContext dc){
        return !((minTickets != null && dc.ticketCount() < minTickets) || (maxTickets != null && dc.ticketCount() > maxTickets));
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext dc) {
        return isMet(dc) ? originalPrice * (1 - discountPercentage / 100) : originalPrice;
    }
}