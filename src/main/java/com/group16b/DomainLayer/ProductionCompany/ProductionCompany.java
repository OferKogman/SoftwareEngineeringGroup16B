package com.group16b.DomainLayer.ProductionCompany;

import java.util.List;

import com.group16b.DomainLayer.User.User;

public class ProductionCompany {
    private int productionCompanyID;
    private double rating;
    private long version;
    
    public ProductionCompany(ProductionCompany other)
    {
        this.productionCompanyID=other.productionCompanyID;
        this.rating=other.rating;
        this.version=other.version;
    }



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

    public long getVersion()
    {
        return version;
    }
    public void setVerson(long version)
    {
        this.version=version;
    }
}
