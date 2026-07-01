package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class AmountRangeDiscount implements DiscountPolicy {
    private Integer minTickets;
    private Integer maxTickets;
    private double discountPercentage;

    public AmountRangeDiscount() {
    }

    public AmountRangeDiscount(Integer minTickets, Integer maxTickets, double discountPercentage) {
        validate(minTickets, maxTickets, discountPercentage);
        this.minTickets = minTickets;
        this.maxTickets = maxTickets;
        this.discountPercentage = discountPercentage;
    }

    private void validate(Integer minTickets, Integer maxTickets, double discountPercentage) {
        if (minTickets == null && maxTickets == null) {
            throw new IllegalArgumentException("At least one of minTickets or maxTickets must be defined.");
        }
        if (minTickets != null && minTickets < 1) {
            throw new IllegalArgumentException("Minimum tickets must be at least 1.");
        }
        if (maxTickets != null && maxTickets < 1) {
            throw new IllegalArgumentException("Maximum tickets must be at least 1.");
        }
        if (minTickets != null && maxTickets != null && minTickets > maxTickets) {
            throw new IllegalArgumentException("Minimum tickets cannot exceed maximum.");
        }
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
    }

    public Integer getMinTickets() {
        return minTickets;
    }

    public void setMinTickets(Integer minTickets) {
        this.minTickets = minTickets;
    }

    public Integer getMaxTickets() {
        return maxTickets;
    }

    public void setMaxTickets(Integer maxTickets) {
        this.maxTickets = maxTickets;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public boolean isMet(int ticketCount) {
        return !((minTickets != null && ticketCount < minTickets)
                || (maxTickets != null && ticketCount > maxTickets));
    }

    @Override
    public boolean isMet(DiscountContext context) {
        return context != null && isMet(context.ticketCount());
    }

    public double calculateDiscount(double originalPrice, int ticketCount) {
        return isMet(ticketCount)
                ? originalPrice * (1 - discountPercentage / 100)
                : originalPrice;
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext context) {
        return isMet(context)
                ? originalPrice * (1 - discountPercentage / 100)
                : originalPrice;
    }
}