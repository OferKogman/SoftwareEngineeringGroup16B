package com.group16b.ApplicationLayer.DTOs;

import java.util.Set;

import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;

public class AssignManagerRequestDTO {
    private String targetID;
    private Set<ManagerPermissions> permissions;

    public String getTargetID() {
        return targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }
    public Set<ManagerPermissions> getPermissions() {
        return permissions;
    }
    public void setPermissions(Set<ManagerPermissions> permissions) {
        this.permissions = permissions;
    }
    
}
