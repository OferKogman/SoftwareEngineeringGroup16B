package com.group16b.DomainLayer.ProductionCompany.membership;

import java.util.Set;

import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.RoleType;

public class HierarchyNodeData {
    private final int userID;
    private final int parentID;
    private final RoleType roleType;
    private final Set<ManagerPermissions> permissions;

    public HierarchyNodeData(int userID,int parentID,RoleType roleType,Set<ManagerPermissions> permissions) 
    {
        this.userID = userID;
        this.parentID = parentID;
        this.roleType = roleType;
        this.permissions = permissions;
    }

    public int getUserID() {
        return userID;
    }

    public int getParentID() {
        return parentID;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public Set<ManagerPermissions> getPermissions() {
        return permissions;
    }
}
