package com.group16b.DomainLayer.ProductionCompanyPolicy;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy;

public class ProductionCompanyPolicy {
    private int id;
    private String name;
    private CompanyState companyState;
    private PurchasePolicy purchasePolicy;
    private DiscountPolicy discountPolicy;

    ProductionCompanyPolicy(int id, String name, CompanyState companyState, PurchasePolicy purchasePolicy,
            DiscountPolicy discountPolicy) {
        this.id = id;
        this.name = name;
        this.companyState = companyState;
        this.purchasePolicy = purchasePolicy;
        this.discountPolicy = discountPolicy;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public CompanyState getCompanyState() {
        return this.companyState;
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

    public void setCompanyState(CompanyState companyState) {
        this.companyState = companyState;
    }

    public void setPurchasePolicy(PurchasePolicy purchasePolicy) {
        this.purchasePolicy = purchasePolicy;
    }

    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

}
