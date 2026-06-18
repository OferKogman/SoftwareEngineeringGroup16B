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

    public double getDiscountPercentage() { return discountPercentage; }
    public String getCode() { return code; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public Integer getMaxUsages() { return maxUsages; }
    public int getCurrentUsages() { return currentUsages; }

    public boolean isMet(){
        return false;
    }
    @Override
    public double calculateDiscount(double originalPrice) {
        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate))
            return originalPrice;
        if (maxUsages != null && currentUsages >= maxUsages)
            return originalPrice;
        currentUsages++;
        return originalPrice * (1 - discountPercentage / 100);
    }
}