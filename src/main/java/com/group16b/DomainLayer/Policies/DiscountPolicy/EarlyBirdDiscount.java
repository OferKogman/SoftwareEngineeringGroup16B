package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public class EarlyBirdDiscount extends VisibleDiscount {
    public EarlyBirdDiscount(double discountPercentage, double discountAmount, LocalDateTime expiryDate) {
        super(discountPercentage, discountAmount, expiryDate);
    }

    public double calculateDiscount(double basePrice){
        if(LocalDateTime.now().isAfter(getExpiryDate())){
            return basePrice; //discount expired.
        }
        return basePrice * (1-getDiscountPercentage()/100) - getDiscountAmount();
    }
}
