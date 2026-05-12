package com.group16b.DomainLayer.ProductionCompanyPolicy;

import java.util.List;

import com.group16b.DomainLayer.User.User;

public class ProductionCompanyPolicy {
    private int productionCompanyID;
    private double rating;
    
    public int getProductionCompanyID() {
        return productionCompanyID;
    }

    public double getRating() {
        return rating;
    }

    public List<User> getAssociatedUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAssociatedUsers'");
    }
}
