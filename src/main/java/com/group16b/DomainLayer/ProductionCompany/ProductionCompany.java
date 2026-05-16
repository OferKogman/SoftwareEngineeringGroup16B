package com.group16b.DomainLayer.ProductionCompany;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.User.User;

public class ProductionCompany {
    private int productionCompanyID;
    private double rating;
    private long version;
    private String name;

    private final HashMap<Integer, MembershipNode> membersNodes=new HashMap<>();
    private final HashMap<Integer, MembershipNode> invites= new HashMap<>();

    public ProductionCompany(ProductionCompany other)
    {
        this.productionCompanyID=other.productionCompanyID;
        this.rating=other.rating;
        this.version=other.version;
        this.name=other.name;
    }
    public ProductionCompany(int id, String name, double rating)
    {
        this.rating=rating;
        this.name=name;
        this.productionCompanyID=id;
        this.version=1;
    }


    public int getProductionCompanyID() {
        return productionCompanyID;
    }

    public double getRating() {
        return rating;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name=name;
    }
    public List<User> getAssociatedUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAssociatedUsers'");
    }

    public long getVersion()
    {
        return version;
    }
    public void setVersion(long version)
    {
        this.version=version;
    }

    public Set<DiscountPolicy> getDiscountPolicy()
    {
        return null;
    }

    public Set<PurchasePolicy> getPurchasePolicy()
    {
        return null;
    }
}
