package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.util.ArrayList;
import java.util.List;

public class DiscountPolicy {
    List<Discount> discounts = new ArrayList<>();


    public void addDiscount(Discount discount){discounts.add(discount);}
    public void removeDiscount(Discount discount){discounts.remove(discount);}
    // iterate over list of discounts and apply each one
    public double applyDiscount(double basePrice){
        double price = basePrice;
        for (Discount discount : discounts) {
            price = discount.calculateDiscount(price);
        }
        return Math.max(0, price); //if total discounts are greater than the original price you get the order for free
    }
}
