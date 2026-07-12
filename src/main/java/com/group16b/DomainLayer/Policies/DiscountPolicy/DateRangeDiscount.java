package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public class DateRangeDiscount implements DiscountPolicy {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private double discountPercentage;

    public DateRangeDiscount() {
    }

    public DateRangeDiscount(LocalDateTime startDate, LocalDateTime endDate, double discountPercentage) {
        validate(startDate, endDate, discountPercentage);
        this.startDate = startDate;
        this.endDate = endDate;
        this.discountPercentage = discountPercentage;
    }

    private void validate(LocalDateTime startDate, LocalDateTime endDate, double discountPercentage) {
        if (startDate == null && endDate == null) {
            throw new IllegalArgumentException("At least one of startDate or endDate must be defined.");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public boolean isMet() {
        return isMet(new DiscountContext(0, 0, LocalDateTime.now(), null));
    }

    @Override
    public boolean isMet(DiscountContext context) {
        LocalDateTime now = context != null && context.date() != null
                ? context.date()
                : LocalDateTime.now();

        return !((startDate != null && now.isBefore(startDate))
                || (endDate != null && now.isAfter(endDate)));
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext context) {
        return isMet(context)
                ? originalPrice * (1 - discountPercentage / 100)
                : originalPrice;
    }
}