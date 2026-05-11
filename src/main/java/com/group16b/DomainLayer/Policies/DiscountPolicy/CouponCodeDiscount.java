package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public class CouponCodeDiscount extends CouponDiscount{

    public CouponCodeDiscount(double discountPercentage, double discountAmount, String code, LocalDateTime expiryDate) {
        super(discountPercentage, discountAmount, code, expiryDate);
    }

    public double calculateDiscount(double basePrice){
        if(LocalDateTime.now().isAfter(getExpiryDate())){
            return basePrice; //discount expired.
        }
        return basePrice * (1-getDiscountPercentage()/100) - getDiscountAmount();
    }
}
