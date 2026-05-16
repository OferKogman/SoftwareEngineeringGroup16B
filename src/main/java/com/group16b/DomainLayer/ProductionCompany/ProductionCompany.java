package com.group16b.DomainLayer.ProductionCompany;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.membership.MembershipNode;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.RoleType;

public class ProductionCompany {
    private int productionCompanyID;
    private double rating;
    private long version;
    private String name;

    private final HashMap<Integer, MembershipNode> membersNodes=new HashMap<>();
    private final HashMap<InviteKey, MembershipNode> invites= new HashMap<>();

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

    private boolean isFouder(int userID)
    {
        MembershipNode node=membersNodes.get(userID);
        if(node!=null && node.getRoleType()==RoleType.FOUNDER)
            return true;
        return false;
    }

    private boolean isOwner(int userID)
    {
        MembershipNode node=membersNodes.get(userID);
        if(node==null || node.getRoleType() == RoleType.MANAGER)
            return false;
        return true;
    }

    private boolean isManager(int userID)
    {
        MembershipNode node=membersNodes.get(userID);
        if(node==null)
            return false;
        return true;
    }

    /*
    adds an invite to become owner
    assigner is set to be caller
    PRE CONDITIONS: target is not already owner, caller is owner
    */
    public void AssignOwner(int callerID, int targetID)
    {
        if(!isOwner(callerID))
        {
            throw new IllegalArgumentException("caller User is not owner in Assign Owner.");
        }
        if(isOwner(targetID))
        {
            throw new IllegalArgumentException("Target is already owner in Assign Owner.");
        }
        MembershipNode newOnwerInvite = MembershipNode.createOwner(callerID, targetID);
        invites.put(new InviteKey(targetID, callerID), newOnwerInvite);
    }

    public void AssignManager(int callerID, int targetID,Set<ManagerPermissions> perms)
    {
        if(!isOwner(callerID))
        {
            throw new IllegalArgumentException("caller User is not owner in Assign Manager.");
        }
        if(isManager(targetID))
        {
            throw new IllegalArgumentException("Target is already owner in Assign Manager.");
        }
        MembershipNode newManagerInvite = MembershipNode.createManager(callerID, targetID,perms);
        invites.put(new InviteKey(targetID, callerID), newManagerInvite);
    }

    //if invite is found then accept it and remove all invites with equivalent or lower role
    public void acceptInvite(int targetID, int assignerID) 
    {
        InviteKey key = new InviteKey(targetID, assignerID);
        MembershipNode invite = invites.get(key);

        if (invite == null) {
            throw new IllegalArgumentException("Invite not found.");
        }

        membersNodes.put(targetID, invite);

        RoleType acceptedRole = invite.getRoleType();

        // remove equivalent or lower roles only
        invites.entrySet().removeIf(e ->
            e.getKey().targetId == targetID &&
            e.getValue().getRoleType().isLowerOrEqual(acceptedRole)
        );
    }

    public void rejectInvite(int targetID, int assignerID) 
    {
        InviteKey key = new InviteKey(targetID, assignerID);

        if (!invites.containsKey(key)) {
            throw new IllegalArgumentException("Invite not found.");
        }

        invites.remove(key);
    }

    public void forfeitOwnership(int userID)
    {
        if(!isOwner(userID))
        {
            throw new IllegalArgumentException("User is not owner in forfeit Ownership");
        }
        if(isFouder(userID))
        {
            throw new IllegalArgumentException("Founder cannot forfeit ownership in his company.");
        }
        removeMember(userID);

    }
    
    private void removeMember(int userID)
    {
        MembershipNode node = membersNodes.get(userID);

        if (node == null) return;

        int parentID = node.getAssignerID();

        // reparent children
        for (MembershipNode child : membersNodes.values()) {
            if (child.getAssignerID() == userID) {
                child.setAssignerID(parentID);
            }
        }

        // remove user
        membersNodes.remove(userID);
    }


    private static class InviteKey {
        private final int targetId;
        private final int assignerId;

        public InviteKey(int targetId, int assignerId) {
            this.targetId = targetId;
            this.assignerId = assignerId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InviteKey)) return false;
            InviteKey other = (InviteKey) o;
            return targetId == other.targetId &&
                   assignerId == other.assignerId;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(targetId, assignerId);
        }
    }
}
