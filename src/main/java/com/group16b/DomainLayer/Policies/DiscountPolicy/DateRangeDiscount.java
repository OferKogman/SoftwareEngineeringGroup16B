package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public class DateRangeDiscount implements DiscountPolicy {
    private LocalDateTime startDate; // null = no start limit
    private LocalDateTime endDate;   // null = no end limit
    private double discountPercentage;

    public DateRangeDiscount(LocalDateTime startDate, LocalDateTime endDate, double discountPercentage) {
        if (startDate == null && endDate == null)
            throw new IllegalArgumentException("At least one of startDate or endDate must be defined.");
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date cannot be after end date.");
        if (discountPercentage < 0 || discountPercentage > 100)
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        this.startDate = startDate;
        this.endDate = endDate;
        this.discountPercentage = discountPercentage;
    }



    public boolean isMet(DiscountContext dc){
        LocalDateTime now = LocalDateTime.now();
        return !((startDate != null && now.isBefore(startDate)) || (endDate != null && now.isAfter(endDate)));
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext dc) {
        return isMet(dc) ? originalPrice * (1 - discountPercentage / 100) : originalPrice;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }


}