package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public class EarlyBirdDiscount implements DiscountPolicy{
    private double discountPercentage;
    private double discountAmount;
    private LocalDateTime expiryDate;
    public EarlyBirdDiscount(double discountPercentage, double discountAmount, LocalDateTime expiryDate) {
        if(expiryDate == null) {this.expiryDate = LocalDateTime.MAX;}
        else {
            if (LocalDateTime.now().isAfter(expiryDate)) {
                throw new IllegalArgumentException("discount expired");
            }
            this.expiryDate = expiryDate;
        }

        if(discountPercentage<0 || discountPercentage >100){
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        if(discountAmount<0){
            throw new IllegalArgumentException("Cannot have a negative discount.");
        }
        this.discountAmount = discountAmount;
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double calculateDiscount(double originalPrice){
        if(LocalDateTime.now().isAfter(this.expiryDate)){
            return originalPrice; //discount expired.
        }
        return originalPrice * (1-this.discountPercentage/100) - this.discountAmount;
    }
}
