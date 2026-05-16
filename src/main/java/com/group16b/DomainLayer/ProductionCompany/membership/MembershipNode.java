package com.group16b.DomainLayer.ProductionCompany.membership;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Role;
import com.group16b.DomainLayer.User.Roles.RoleType;

public class MembershipNode {
    private int userID;
    private Integer assignerID;
    private RoleType roleType;
    private Set<ManagerPermissions> permissions;

    private MembershipNode(int userID, Integer assignerID, RoleType roleType, Set<ManagerPermissions> perms)
    {
        this.userID=userID;
        this.assignerID=Integer.valueOf(assignerID);
        this.roleType=roleType;
        this.permissions=new HashSet<>(perms);
    }

    public static MembershipNode createManager(int userID, Integer assignerID,Set<ManagerPermissions> perms)
    {
        return new MembershipNode(userID, assignerID, RoleType.MANAGER, perms);
    }
    
    public static MembershipNode createOwner(int userID, Integer assignerID)
    {
        return new MembershipNode(userID, assignerID, RoleType.OWNER, EnumSet.allOf(ManagerPermissions.class));
    }
    public static MembershipNode createFounder(int userID)
    {
        return new MembershipNode(userID, null, RoleType.OWNER, EnumSet.allOf(ManagerPermissions.class));
    }

    public int getUserID()
    {
        return userID;
    }
    public int getAssgnerID()
    {
        return assignerID;
    }
    public Set<ManagerPermissions> getPermissions()
    {
        return new HashSet<>(permissions);
    }
    public RoleType getRoleType()
    {
        return roleType;
    }

}
