package com.group16b.DomainLayer.ProductionCompany.membership;

import java.util.Set;

import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;

public class HierarchyNodeData {
    private final String userID;
    private final String parentID;
    private final RoleType roleType;
    private final Set<ManagerPermissions> permissions;

    public HierarchyNodeData(String userID,String parentID,RoleType roleType,Set<ManagerPermissions> permissions) 
    {
        this.userID = userID;
        this.parentID = parentID;
        this.roleType = roleType;
        this.permissions = permissions;
    }

    public String getUserID() {
        return userID;
    }

    public String getParentID() {
        return parentID;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public Set<ManagerPermissions> getPermissions() {
        return permissions;
    }
}
