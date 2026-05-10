package com.group16b.DomainLayer.Policies;

public abstract class Discount {
    private double discountPercentage;
    private double discountAmount;

    protected Discount(double discountPercentage, double discountAmount){
        if(discountPercentage<0 || discountPercentage >100){
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        if(discountAmount<0){
            throw new IllegalArgumentException("Cannot have a negative discount.");
        }
        this.discountAmount = discountAmount;
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        if(discountAmount<0){
            throw new IllegalArgumentException("Cannot have a negative discount.");
        }
        this.discountAmount = discountAmount;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        if(discountPercentage<0 || discountPercentage >100){
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.discountPercentage = discountPercentage;
    }
    public abstract double calculateDiscount(double basePrice);
}
