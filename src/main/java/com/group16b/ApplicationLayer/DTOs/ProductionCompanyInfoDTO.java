package com.group16b.ApplicationLayer.DTOs;

import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;

public class ProductionCompanyInfoDTO {
        private int id;
    private double rating;
    private String name;


    public ProductionCompanyInfoDTO(ProductionCompany company) {
        this.id = company.getProductionCompanyID();
        this.name = company.getName();
        this.rating = company.getRating();
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
}
