package com.group16b.DomainLayer.Policies.PurchasePolicy;

public class AgePolicy implements PurchasePolicy{
    private int minAge;
    public AgePolicy(int minAge){
        if(minAge<0){
            throw new IllegalArgumentException("Minimum age cannot be negative.");
        }
        this.minAge = minAge;
    }
    public int getMinAge(){
        return this.minAge;
    }
    public void setMinAge(int minAge){
        this.minAge = minAge;
    }
    public boolean validatePurchase(){
        return true; //TODO: implement
    }
}