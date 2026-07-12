package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CouponCodeDiscount implements DiscountPolicy {

    @Column(name = "discount_percentage", nullable = false)
    private double discountPercentage;

    @Column(name = "coupon_code", nullable = false)
    private String code;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "max_usages")
    private Integer maxUsages;

    @Column(name = "current_usages", nullable = false)
    private int currentUsages;

    public CouponCodeDiscount() {
        // Required by JPA.
    }

    public CouponCodeDiscount(
            double discountPercentage,
            String code,
            LocalDateTime expiryDate,
            Integer maxUsages) {

        if (discountPercentage <= 0 || discountPercentage > 100) {
            throw new IllegalArgumentException(
                    "Discount percentage must be greater than 0 and at most 100.");
        }

        if (expiryDate != null
                && !expiryDate.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Expiration date must be in the future.");
        }

        if (maxUsages != null && maxUsages < 1) {
            throw new IllegalArgumentException(
                    "Max usages must be at least 1.");
        }

        this.discountPercentage = discountPercentage;
        this.code = normalizeCode(code);
        this.expiryDate = expiryDate;
        this.maxUsages = maxUsages;
        this.currentUsages = 0;
    }

    public CouponCodeDiscount(CouponCodeDiscount other) {
        if (other == null) {
            throw new IllegalArgumentException(
                    "Coupon cannot be null.");
        }

        this.discountPercentage = other.discountPercentage;
        this.code = other.code;
        this.expiryDate = other.expiryDate;
        this.maxUsages = other.maxUsages;
        this.currentUsages = other.currentUsages;
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

    public boolean matchesCode(String suppliedCode) {
        return code.equals(normalizeCode(suppliedCode));
    }

    public boolean isCouponStillUsable() {
        return (expiryDate == null
                || LocalDateTime.now().isBefore(expiryDate))
                && (maxUsages == null
                || currentUsages < maxUsages);
    }

    public double redeem(double currentOrderPrice) {
        if (currentOrderPrice < 0) {
            throw new IllegalArgumentException(
                    "Order price cannot be negative.");
        }

        if (expiryDate != null
                && !LocalDateTime.now().isBefore(expiryDate)) {

            throw new IllegalStateException(
                    "Coupon has expired.");
        }

        if (maxUsages != null
                && currentUsages >= maxUsages) {

            throw new IllegalStateException(
                    "Coupon has reached its maximum number of uses.");
        }

        double discountedPrice =
                currentOrderPrice
                        * (1.0 - discountPercentage / 100.0);

        currentUsages++;

        return Math.max(0.0, discountedPrice);
    }

    public boolean isMet() {
        return isCouponStillUsable();
    }

    @Override
    public boolean isMet(DiscountContext context) {
        return context != null
                && context.couponCode() != null
                && matchesCode(context.couponCode())
                && isCouponStillUsable();
    }

    @Override
    public double calculateDiscount(
            double originalPrice,
            DiscountContext context) {

        if (!isMet(context)) {
            return originalPrice;
        }

        return redeem(originalPrice);
    }

    @Override
    public double calculateDiscount(double originalPrice) {
        if (!isCouponStillUsable()) {
            return originalPrice;
        }

        return redeem(originalPrice);
    }

    private static String normalizeCode(String rawCode) {
        if (rawCode == null
                || rawCode.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Coupon code cannot be empty.");
        }

        return rawCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CouponCodeDiscount other)) {
            return false;
        }

        return Objects.equals(code, other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}