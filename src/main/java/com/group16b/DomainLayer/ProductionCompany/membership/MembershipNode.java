package com.group16b.DomainLayer.ProductionCompany.membership;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class MembershipNode {
    private String userID;
    private String assignerID;
    private RoleType roleType;
    private Set<ManagerPermissions> permissions;

    private MembershipNode(String userID, String assignerID, RoleType roleType, Set<ManagerPermissions> perms)
        {
        this.userID=userID;
        this.assignerID=assignerID;
        this.roleType=roleType;
        this.permissions=new HashSet<>(perms);
    }
    public MembershipNode(MembershipNode other)
    {
        this.userID=other.userID;
        this.assignerID=other.assignerID;
        this.roleType=other.roleType;
        this.permissions=new HashSet<>(other.permissions);
    }
    public static MembershipNode createManager(String userID, String assignerID,Set<ManagerPermissions> perms)
    {
        return new MembershipNode(userID, assignerID, RoleType.MANAGER, perms);
    }
    
    public static MembershipNode createOwner(String userID, String assignerID)
    {
        return new MembershipNode(userID, assignerID, RoleType.OWNER, EnumSet.allOf(ManagerPermissions.class));
    }
    public static MembershipNode createFounder(String userID)
    {
        return new MembershipNode(userID, null, RoleType.FOUNDER, EnumSet.allOf(ManagerPermissions.class));
    }

    public String getUserID()
    {
        return userID;
    }
    public String getAssignerID()
    {
        return assignerID;
    }
    public void setAssignerID(String newID)
    {   
        if(this.roleType==RoleType.FOUNDER)
            throw new IllegalArgumentException("Can't update assignerID for founder of company!");
        this.assignerID=newID;
    }
    public Set<ManagerPermissions> getPermissions()
    {
        return new HashSet<>(permissions);
    }
    public void setPermissions(Set<ManagerPermissions> newPermissions)
    {
        if(this.roleType.isHigherOrEqual(RoleType.OWNER))
            throw new IllegalArgumentException("cant update permissions for owner and founder!");
        this.permissions=new HashSet<>(newPermissions);
    }
    public RoleType getRoleType()
    {
        return roleType;
    }

}
