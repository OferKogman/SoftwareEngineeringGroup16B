package com.group16b.ApplicationLayer.DTOs;

import java.util.List;

import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.PurchasePolicyDTO;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;

public class ProductionCompanyDTO {

    private int id;
    private double rating;
    private String name;
    private String founderID;

    private List<HierarchyNodeDTO> members;
    private PurchasePolicyDTO purchasePolicy;

    public ProductionCompanyDTO(ProductionCompany company) {
        this.id = company.getProductionCompanyID();
        this.name = company.getName();
        this.rating = company.getRating();
        this.founderID = company.getFounderID();
        this.members = company.getHierarchyTree(founderID).stream().map(HierarchyNodeDTO::new).toList();
        this.purchasePolicy = null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public String getFounderID() {
        return founderID;
    }

    public List<HierarchyNodeDTO> getMembers() {
        return members;
    }

    public PurchasePolicyDTO getPurchasePolicy() {
        return purchasePolicy;
    }
}
