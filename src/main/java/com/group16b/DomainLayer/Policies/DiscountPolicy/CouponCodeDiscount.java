package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public class CouponCodeDiscount implements DiscountPolicy {
    private double discountPercentage;
    private String code;
    private LocalDateTime expiryDate;
    private Integer maxUsages;
    private int currentUsages;

    public CouponCodeDiscount() {
    }

    public CouponCodeDiscount(double discountPercentage, String code, LocalDateTime expiryDate, Integer maxUsages) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty.");
        }
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Discount is expired.");
        }
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        if (maxUsages != null && maxUsages < 1) {
            throw new IllegalArgumentException("Max usages must be at least 1.");
        }

        this.discountPercentage = discountPercentage;
        this.code = code;
        this.expiryDate = expiryDate;
        this.maxUsages = maxUsages;
        this.currentUsages = 0;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public Integer getMaxUsages() {
        return maxUsages;
    }

    public int getCurrentUsages() {
        return currentUsages;
    }

    private boolean isCouponStillUsable() {
        return (expiryDate == null || !LocalDateTime.now().isAfter(expiryDate))
                && !(maxUsages != null && currentUsages >= maxUsages);
    }

    public boolean isMet() {
        return isCouponStillUsable();
    }

    @Override
    public boolean isMet(DiscountContext context) {
        return context != null
                && context.couponCode() != null
                && context.couponCode().equals(code)
                && isCouponStillUsable();
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext context) {
        if (!isMet(context)) {
            return originalPrice;
        }

        currentUsages++;
        return originalPrice * (1 - discountPercentage / 100);
    }

    @Override
    public double calculateDiscount(double originalPrice) {
        if (!isCouponStillUsable()) {
            return originalPrice;
        }

        currentUsages++;
        return originalPrice * (1 - discountPercentage / 100);
    }
}