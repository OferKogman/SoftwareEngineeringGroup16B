package com.group16b.ApplicationLayer.DTOs;

import java.util.Set;

import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;

public class ChangeManagerPermissionRequestDTO {
    private String targetID;
    private Set<ManagerPermissions> newPermissions;

    public String getTargetID() {
        return targetID;
    }
    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }
    public Set<ManagerPermissions> getNewPermissions() {
        return newPermissions;
    }
    public void setNewPermissions(Set<ManagerPermissions> newPermissions) {
        this.newPermissions = newPermissions;
    }
    
}
