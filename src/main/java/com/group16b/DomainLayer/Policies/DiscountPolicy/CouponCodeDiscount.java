package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public class CouponCodeDiscount implements DiscountPolicy{
    private double discountPercentage;
    private double discountAmount;
    private String code;
    private LocalDateTime expiryDate;
    public CouponCodeDiscount(double discountPercentage, double discountAmount, String code, LocalDateTime expiryDate) {
        if(expiryDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Discount is expired");
        }
        if(code == null){
            throw new IllegalArgumentException("Null code");
        }
        if(code.isEmpty()){
            throw new IllegalArgumentException("Empty code");
        }
        if(discountPercentage<0 || discountPercentage >100){
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        if(discountAmount<0){
            throw new IllegalArgumentException("Cannot have a negative discount.");
        }
        this.discountAmount = discountAmount;
        this.discountPercentage = discountPercentage;
        this.code = code;
        this.expiryDate = expiryDate;
    }

    public double calculateDiscount(double basePrice){
        if(LocalDateTime.now().isAfter(this.expiryDate)){
            return basePrice; //discount expired.
        }
        return basePrice * (1-this.discountPercentage/100) - this.discountAmount;
    }
}
