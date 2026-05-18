package com.group16b.DomainLayer.ProductionCompany;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.membership.HierarchyNodeData;
import com.group16b.DomainLayer.ProductionCompany.membership.MembershipNode;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;

public class ProductionCompany {
    private int productionCompanyID;
    private double rating;
    private long version;
    private String name;
    private String founderID;

    private final HashMap<String, MembershipNode> membersNodes=new HashMap<>();
    private final HashMap<InviteKey, MembershipNode> invites= new HashMap<>();

    public ProductionCompany(ProductionCompany other)
    {
        this.productionCompanyID=other.productionCompanyID;
        this.rating=other.rating;
        this.version=other.version;
        this.name=other.name;
        this.founderID=other.founderID;
        for (Entry<String, MembershipNode> entry : other.membersNodes.entrySet()) {
            this.membersNodes.put(
                    entry.getKey(),
                    new MembershipNode(entry.getValue())
            );
        }
        for (Map.Entry<InviteKey, MembershipNode> entry : other.invites.entrySet()) {
            this.invites.put(
                    new InviteKey(entry.getKey()),
                    new MembershipNode(entry.getValue())
            );
        }
    }
    public ProductionCompany(int id, String name, double rating, String founderID)
    {
        this.rating=rating;
        this.name=name;
        this.productionCompanyID=id;
        this.version=1;
        this.founderID= founderID;
        this.membersNodes.put(founderID,MembershipNode.createFounder(founderID));
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

    public boolean isFouder(String userID)
    {
        MembershipNode node=membersNodes.get(userID);
        if(node!=null && node.getRoleType()==RoleType.FOUNDER)
            return true;
        return false;
    }

    public boolean isOwner(String userID)
    {
        MembershipNode node=membersNodes.get(userID);
        if(node==null || node.getRoleType() == RoleType.MANAGER)
            return false;
        return true;
    }

    public boolean isManager(String userID)
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
    public void AssignOwner(String callerID, String targetID)
    {
        if(!isOwner(callerID))
        {
            throw new IllegalArgumentException("caller User is not owner in Assign Owner.");
        }
        if(isOwner(targetID))
        {
            throw new IllegalArgumentException("Target is already owner in Assign Owner.");
        }
        MembershipNode newOnwerInvite = MembershipNode.createOwner(targetID, callerID);
        invites.put(new InviteKey(targetID, callerID), newOnwerInvite);
    }

    public void AssignManager(String callerID, String targetID,Set<ManagerPermissions> perms)
    {
        if(!isOwner(callerID))
        {
            throw new IllegalArgumentException("caller User is not owner in Assign Manager.");
        }
        if(isManager(targetID))
        {
            throw new IllegalArgumentException("Target is already owner in Assign Manager.");
        }
        MembershipNode newManagerInvite = MembershipNode.createManager(targetID, callerID,perms);
        invites.put(new InviteKey(targetID, callerID), newManagerInvite);
    }

    //if invite is found then accept it and remove all invites with equivalent or lower role
    public void acceptInvite(String targetEmail, String assignerID) 
    {
        InviteKey key = new InviteKey(targetEmail, assignerID);
        MembershipNode invite = invites.get(key);

        if (invite == null) {
            throw new IllegalArgumentException("Invite not found.");
        }

        membersNodes.put(targetEmail, invite);

        RoleType acceptedRole = invite.getRoleType();

        // remove equivalent or lower roles only
        invites.entrySet().removeIf(e ->
            e.getKey().targetId == targetEmail &&
            e.getValue().getRoleType().isLowerOrEqual(acceptedRole)
        );
    }

    public void rejectInvite(String targetID, String assignerID) 
    {
        InviteKey key = new InviteKey(targetID, assignerID);

        if (!invites.containsKey(key)) {
            throw new IllegalArgumentException("Invite not found.");
        }

        invites.remove(key);
    }

    public void forfeitOwnership(String userID)
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

    public void removeMemberByOwner(String ownerID, String targetID)
    {
        canOwnerManageTarget(ownerID, targetID);
        removeMember(targetID);
    }

    public void updatePermissionsOfManager(String ownerID, String targetID, Set<ManagerPermissions> newPerms)
    {
        canOwnerManageTarget(ownerID, targetID);
        MembershipNode targetNode = membersNodes.get(targetID);
        targetNode.setPermissions(newPerms);
    }

    public List<HierarchyNodeData> getHierarchyTree(String requesterID)
    {
        if(!isOwner(requesterID)) {
            throw new IllegalArgumentException("Requester is not owner.");
        }
        List<HierarchyNodeData> result = new ArrayList<>();
        for (Map.Entry<String, MembershipNode> entry : membersNodes.entrySet()) {
            MembershipNode node = entry.getValue();
            result.add(new HierarchyNodeData(
                    entry.getKey(),
                    node.getAssignerID(),
                    node.getRoleType(),
                    node.getPermissions()
            ));
        }
        return result;
    }

    public void validateUserPermissions(String userID, RoleType type)
    {
        MembershipNode node=membersNodes.get(userID);
        if(node==null || node.getRoleType().isLowerOrEqual(type) && !(node.getRoleType().equals(type)))
        {
            throw new IllegalArgumentException("user "+userID+" dont have high enough permissions in company "+this.productionCompanyID);
        }
    }
    public void validateUserPermissions(String userID, ManagerPermissions perm)
    {
        MembershipNode node=membersNodes.get(userID);
        if(node==null || !(node.getPermissions().contains(perm)))
        {
            throw new IllegalArgumentException("user "+userID+" dont have high enough permissions in company "+this.productionCompanyID);
        }
    }

    public List<String> getDirectSubordinates(String userID)
    {
        List<String> result = new ArrayList<>();

        for (Map.Entry<String, MembershipNode> entry : membersNodes.entrySet())
        {
            MembershipNode node = entry.getValue();

            if (node.getAssignerID() == userID)
            {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    public List<String> getAllSubordinates(String userID)
    {
        List<String> result = new ArrayList<>();

        collectSubordinates(userID, result);

        return result;
    }

    private void collectSubordinates(String userID, List<String> result)
    {
        List<String> directSubs = getDirectSubordinates(userID);

        for (String subordinateID : directSubs)
        {
            result.add(subordinateID);

            collectSubordinates(subordinateID, result);
        }
    }

    private boolean isAssignedByOwner(String ownerID, String targetID)
    {
        MembershipNode current = membersNodes.get(targetID);

        while (current != null)
        {
            String assignerID = current.getAssignerID();

            if (assignerID == ownerID) {
                return true;
            }

            current = membersNodes.get(assignerID);
        }

        return false;
    }

    private void removeMember(String userID)
    {
        MembershipNode node = membersNodes.get(userID);

        if (node == null) return;

        String parentID = node.getAssignerID();

        // reparent children
        for (MembershipNode child : membersNodes.values()) {
            if (child.getAssignerID() == userID) {
                child.setAssignerID(parentID);
            }
        }

        // remove user
        membersNodes.remove(userID);

        //remove all retaled invites
        invites.entrySet().removeIf(e ->
            e.getKey().targetId == userID || e.getKey().assignerId==userID
        );
    }

    private void canOwnerManageTarget(String ownerID, String targetID)
    {
        if (!isOwner(ownerID)) {
            throw new IllegalArgumentException("Caller is not an owner.");
        }
        MembershipNode targetNode = membersNodes.get(targetID);

        if (targetNode == null) {
            throw new IllegalArgumentException("Target is not a member.");
        }

        if (!isAssignedByOwner(ownerID, targetID)) {
            throw new IllegalArgumentException("Target was not assigned by this owner (directly or transitively).");
        }
    }


    private static class InviteKey {
        private final String targetId;
        private final String assignerId;

        public InviteKey(String targetId, String assignerId) {
            this.targetId = targetId;
            this.assignerId = assignerId;
        }
        public InviteKey(InviteKey other) {
            this.targetId = other.targetId;
            this.assignerId = other.assignerId;
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

    public void adminRemoveUser(String userID)
    {
        removeMember(userID);
    }
}
