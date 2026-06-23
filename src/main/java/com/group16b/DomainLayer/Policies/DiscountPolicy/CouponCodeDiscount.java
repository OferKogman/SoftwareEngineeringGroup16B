package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public class CouponCodeDiscount implements DiscountPolicy {
    private double discountPercentage;
    private String code;
    private LocalDateTime expiryDate;   // null = no expiry
    private Integer maxUsages;          // null = unlimited
    private int currentUsages;

    public CouponCodeDiscount(double discountPercentage, String code, LocalDateTime expiryDate, Integer maxUsages) {
        if (code == null || code.isEmpty())
            throw new IllegalArgumentException("Code cannot be null or empty.");
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Discount is expired.");
        if (discountPercentage < 0 || discountPercentage > 100)
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        if (maxUsages != null && maxUsages < 1)
            throw new IllegalArgumentException("Max usages must be at least 1.");
        this.discountPercentage = discountPercentage;
        this.code = code;
        this.expiryDate = expiryDate;
        this.maxUsages = maxUsages;
        this.currentUsages = 0;
    }


    public int getCurrentUsages() { return currentUsages; }

    public boolean isMet(DiscountContext dc){
        return dc.couponCode() != null && dc.couponCode().equals(code) && (expiryDate == null ||(expiryDate != null && !(LocalDateTime.now().isAfter(expiryDate)))) && !(maxUsages != null && currentUsages >= maxUsages);
    }
    @Override
    public double calculateDiscount(double originalPrice, DiscountContext dc) {
        boolean valid = isMet(dc);
        if(valid) currentUsages++; //made this one a bit longer because we have to update on successful uses.
        return valid ? originalPrice * (1 - discountPercentage / 100) : originalPrice;
    }

    public double getDiscountPercentage() { return this.discountPercentage;
    }

    public String getCode() { return this.code;
    }

    public LocalDateTime getExpiryDate() { return this.expiryDate;
    }
}