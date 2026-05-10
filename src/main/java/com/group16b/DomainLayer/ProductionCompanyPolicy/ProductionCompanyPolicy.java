package com.group16b.DomainLayer.ProductionCompanyPolicy;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy;

import java.util.concurrent.atomic.AtomicBoolean;

public class ProductionCompanyPolicy {
    private int id;
    private String name;
    private AtomicBoolean isOpen = new AtomicBoolean(false);
    private PurchasePolicy purchasePolicy;
    private DiscountPolicy discountPolicy;

    ProductionCompanyPolicy(int id, String name, PurchasePolicy purchasePolicy,
            DiscountPolicy discountPolicy) {
        this.id = id;
        this.name = name;
        this.purchasePolicy = purchasePolicy;
        this.discountPolicy = discountPolicy;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isOpen() {
        return isOpen.get();
    }

    public PurchasePolicy getPurchasePolicy() {
        return this.purchasePolicy;
    }

    public DiscountPolicy getDiscountPolicy() {
        return this.discountPolicy;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void openCompany(){
        if(isOpen.getAndSet(true)){
            throw new IllegalStateException("Company is already open.");
        }
    }

    public void closeCompany(){
        if(!(isOpen.getAndSet(false))){
            throw new IllegalStateException("Company is already closed.");
        }
    }

    public void setPurchasePolicy(PurchasePolicy purchasePolicy) {
        this.purchasePolicy = purchasePolicy;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

}
