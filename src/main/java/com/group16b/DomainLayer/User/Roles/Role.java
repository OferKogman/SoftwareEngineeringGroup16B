package com.group16b.DomainLayer.User.Roles;

public abstract class Role {
    
    private int parentID;
    
    public Role(int parentID){
        this.parentID = parentID;
    }
    public int getParentID() {
        return parentID;
    }
}
