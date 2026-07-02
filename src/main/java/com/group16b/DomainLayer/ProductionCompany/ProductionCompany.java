package com.group16b.DomainLayer.ProductionCompany;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.group16b.ApplicationLayer.Records.CompanyInviteRecord;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.membership.HierarchyNodeData;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.ProductionCompany.membership.MembershipNode;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
@Table(name = "production_companies")
public class ProductionCompany {

    @Id
    private int productionCompanyID;

    @Column(nullable = false)
    private double rating;

    @Version
    private long version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String founderID;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "production_company_id")
    @MapKey(name = "userID")
    private final Map<String, MembershipNode> membersNodes = new HashMap<>();

    @Convert(converter = InvitesConverter.class)
    @Column(name = "invites", columnDefinition = "TEXT")
    private final Map<InviteKey, MembershipNode> invites = new HashMap<>();

    @Convert(converter = ChildrenByUserConverter.class)
    @Column(name = "children_by_user", columnDefinition = "TEXT")
    private final Map<String, Set<String>> childrenByUser = new HashMap<>();

    // TODO: purchasePolicies and discountPolicies need annotations once rewritten
    @Transient
    private final Set<PurchasePolicy> purchasePolicies = new HashSet<>();
    @Transient
    private final Set<DiscountPolicy> discountPolicies = new HashSet<>();

    protected ProductionCompany() {
    }

    public ProductionCompany(ProductionCompany other) {
        this.productionCompanyID = other.productionCompanyID;
        this.rating = other.rating;
        this.version = other.version;
        this.name = other.name;
        this.founderID = other.founderID;
        for (Entry<String, MembershipNode> entry : other.membersNodes.entrySet()) {
            this.membersNodes.put(
                    entry.getKey(),
                    new MembershipNode(entry.getValue()));
        }
        for (Map.Entry<InviteKey, MembershipNode> entry : other.invites.entrySet()) {
            this.invites.put(
                    new InviteKey(entry.getKey()),
                    new MembershipNode(entry.getValue()));
        }
        for (Map.Entry<String, Set<String>> entry : other.childrenByUser.entrySet()) {
            this.childrenByUser.put(
                    entry.getKey(),
                    new HashSet<>(entry.getValue()));
        }

        this.purchasePolicies.addAll(other.purchasePolicies);
        this.discountPolicies.addAll(other.discountPolicies);
    }

    public ProductionCompany(int Id, String name, double rating, String founderID) {
        this.name = normalizeCompanyName(name);
        this.rating = rating;
        this.founderID = founderID;
        this.productionCompanyID = Id;
        this.membersNodes.put(founderID, MembershipNode.createFounder(founderID));
    }

    private String normalizeCompanyName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Production company name cannot be empty.");
        }

        String trimmed = name.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Production company name cannot be empty.");
        }

        return trimmed;
    }

    public static ProductionCompany createNewCompany(String name, String founderID, int id) {
        return new ProductionCompany(id, name, 0.0, founderID);//we no longer need id generation, but will save  signature for mapImpl
    }

    public int getProductionCompanyID() {
        return productionCompanyID;
    }

    public double getRating() {
        return rating;
    }

    public String getName() {
        return name;
    }

    public String getFounderID() {
        return founderID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setProductionCompanyId(int id){//for the interception
        this.productionCompanyID = id;
    }

    public Set<DiscountPolicy> getDiscountPolicy() {
        return new HashSet<>(discountPolicies);
    }

    public Set<PurchasePolicy> getPurchasePolicy() {
        return new HashSet<>(purchasePolicies);
    }

    public boolean isFounder(String userID) {
        MembershipNode node = membersNodes.get(userID);
        if (node != null && node.getRoleType() == RoleType.FOUNDER)
            return true;
        return false;
    }

    public boolean isOwner(String userID) {
        MembershipNode node = membersNodes.get(userID);
        if (node == null || node.getRoleType() == RoleType.MANAGER)
            return false;
        return true;
    }

    public boolean isManager(String userID) {
        MembershipNode node = membersNodes.get(userID);
        if (node == null)
            return false;
        return true;
    }

    public void AssignOwner(String callerID, String targetID) {
        if (!isOwner(callerID)) {
            throw new IllegalArgumentException("caller User is not owner in Assign Owner.");
        }
        if (isOwner(targetID)) {
            throw new IllegalArgumentException("Target " + targetID + " is already an owner of the company.");
        }
        MembershipNode newOnwerInvite = MembershipNode.createOwner(targetID, callerID);
        invites.put(new InviteKey(targetID, callerID), newOnwerInvite);
    }

    public void AssignManager(String callerID, String targetID, Set<ManagerPermissions> perms) {
        if (!isOwner(callerID)) {
            throw new IllegalArgumentException("caller User is not owner in Assign Manager.");
        }
        if (isManager(targetID)) {
            throw new IllegalArgumentException("Target " + targetID + " is already a manager of the company.");
        }
        if (perms == null || perms.isEmpty()) {
            throw new IllegalArgumentException("Manager must have at least one permission.");
        }
        MembershipNode newManagerInvite = MembershipNode.createManager(targetID, callerID, perms);
        invites.put(new InviteKey(targetID, callerID), newManagerInvite);
    }

    public void acceptInvite(String targetID, String assignerID) {
        InviteKey key = new InviteKey(targetID, assignerID);
        MembershipNode invite = invites.get(key);

        if (invite == null) {
            throw new IllegalArgumentException("Invite not found.");
        }
        MembershipNode curRole = membersNodes.get(targetID);
        if (curRole != null) {
            String parentID = curRole.getAssignerID();
            if (parentID != null)
                childrenByUser.get(parentID).remove(targetID);
        }
        membersNodes.put(targetID, invite);
        addChild(assignerID, targetID);

        RoleType acceptedRole = invite.getRoleType();

        invites.entrySet().removeIf(e -> e.getKey().targetId.equals(targetID) &&
                e.getValue().getRoleType().isLowerOrEqual(acceptedRole));
    }

    public void rejectInvite(String targetID, String assignerID) {
        InviteKey key = new InviteKey(targetID, assignerID);

        if (!invites.containsKey(key)) {
            throw new IllegalArgumentException("Invite not found.");
        }

        invites.remove(key);
    }

    public void forfeitOwnership(String userID) {
        if (!isOwner(userID)) {
            throw new IllegalArgumentException("User " + userID + " is not owner in forfeit Ownership");
        }
        if (isFounder(userID)) {
            throw new IllegalArgumentException("Founder cannot forfeit ownership in his company.");
        }
        removeMember(userID);
    }

    public void removeMemberByOwner(String ownerID, String targetID) {
        canOwnerManageTarget(ownerID, targetID);
        removeMember(targetID);
    }

    public void updatePermissionsOfManager(String ownerID, String targetID, Set<ManagerPermissions> newPerms) {
        canOwnerManageTarget(ownerID, targetID);
        MembershipNode targetNode = membersNodes.get(targetID);
        targetNode.setPermissions(newPerms);
    }

    public List<HierarchyNodeData> getHierarchyTree(String requesterID) {
        if (!isOwner(requesterID)) {
            throw new IllegalArgumentException("Requester is not owner.");
        }
        List<HierarchyNodeData> result = new ArrayList<>();
        for (Map.Entry<String, MembershipNode> entry : membersNodes.entrySet()) {
            MembershipNode node = entry.getValue();
            result.add(new HierarchyNodeData(
                    entry.getKey(),
                    node.getAssignerID(),
                    node.getRoleType(),
                    node.getPermissions()));
        }
        return result;
    }

    public Set<ManagerPermissions> getUserPermissions(String userID) {
        MembershipNode node = membersNodes.get(userID);
        if (node == null)
            throw new IllegalArgumentException("User " + userID + " is not part of the company.");
        return node.getPermissions();
    }

    public void validateUserPermissions(String userID, RoleType type) {
        MembershipNode node = membersNodes.get(userID);
        if (node == null || node.getRoleType().isLowerOrEqual(type) && !(node.getRoleType().equals(type))) {
            throw new IllegalArgumentException(
                    "user " + userID + " dont have high enough permissions in company " + this.productionCompanyID);
        }
    }

    public void validateUserPermissions(String userID, ManagerPermissions perm) {
        MembershipNode node = membersNodes.get(userID);
        if (node == null || !(node.getPermissions().contains(perm))) {
            throw new IllegalArgumentException(
                    "user " + userID + " dont have correct permissions in company " + this.productionCompanyID);
        }
    }

    public List<String> getAllSubordinates(String userID) {
        List<String> result = new ArrayList<>();
        collectSubordinates(userID, result);
        return result;
    }

    private void collectSubordinates(String userID, List<String> result) {
        for (String child : childrenByUser.getOrDefault(userID, Set.of())) {
            result.add(child);
            collectSubordinates(child, result);
        }
    }

    public List<String> getOwnershipDescendants(String userID) {
        List<String> result = new ArrayList<>();
        collectOwnershipDescendants(userID, result);
        return result;
    }

    private void collectOwnershipDescendants(String userID, List<String> result) {
        for (String child : childrenByUser.getOrDefault(userID, Set.of())) {
            MembershipNode node = membersNodes.get(child);
            if (node != null && node.getRoleType() == RoleType.OWNER) {
                result.add(child);
                collectOwnershipDescendants(child, result);
            }
        }
    }

    private boolean isAssignedByOwner(String ownerID, String targetID) {
        MembershipNode current = membersNodes.get(targetID);

        while (current.getAssignerID() != null) {
            String assignerID = current.getAssignerID();

            if (assignerID.equals(ownerID)) {
                return true;
            }

            current = membersNodes.get(assignerID);
        }

        return false;
    }

    private void removeMember(String userID) {
        MembershipNode node = membersNodes.get(userID);

        if (node == null)
            return;

        String parentID = node.getAssignerID();
        childrenByUser.get(parentID).remove(userID);

        Set<String> children = childrenByUser.getOrDefault(userID, Set.of());
        for (String child : children) {
            MembershipNode childNode = membersNodes.get(child);
            if (childNode != null) {
                childNode.setAssignerID(parentID);
                addChild(parentID, child);
            }
        }

        childrenByUser.remove(userID);
        membersNodes.remove(userID);

        invites.entrySet().removeIf(e -> e.getKey().targetId.equals(userID) || e.getKey().assignerId.equals(userID));
    }

    private void canOwnerManageTarget(String ownerID, String targetID) {
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

    public void adminRemoveUser(String userID) {
        removeMember(userID);
    }

    private void addChild(String parent, String child) {
        childrenByUser.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
    }

    public void incrementVersion() {
		this.version++;
	}

    static class InviteKey implements java.io.Serializable {
        private final String targetId;
        private final String assignerId;

        public InviteKey(String targetId, String callerID) {
            this.targetId = targetId;
            this.assignerId = callerID;
        }

        public InviteKey(InviteKey other) {
            this.targetId = other.targetId;
            this.assignerId = other.assignerId;
        }

        public String getTargetId() {
            return targetId;
        }

        public String getAssignerId() {
            return assignerId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof InviteKey))
                return false;
            InviteKey other = (InviteKey) o;
            return targetId.equals(other.targetId) &&
                    assignerId.equals(other.assignerId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(targetId, assignerId);
        }
    }

    public boolean hasPendingInvite(String userID) {
        return invites.keySet()
                .stream()
                .anyMatch(k -> k.targetId.equals(userID));
    }

    public boolean hasPendingInvite(String targetID, String assignerID) {
        return invites.containsKey(
                new InviteKey(targetID, assignerID));
    }

    public boolean hasPendingOwnerInvite(String targetID, String assignerID) {
        MembershipNode node = invites.get(new InviteKey(targetID, assignerID));
        return node != null && node.getRoleType() == RoleType.OWNER;
    }

    public boolean hasPendingManagerInvite(String targetID, String assignerID, Set<ManagerPermissions> perms) {
        MembershipNode node = invites.get(new InviteKey(targetID, assignerID));
        return node != null && node.getRoleType() == RoleType.MANAGER && node.getPermissions().equals(perms);
    }

    public Set<String> getDirectSubordinates(String userID) {
        return childrenByUser.getOrDefault(userID, Set.of());
    }

    public boolean isDirectSubordinate(String parentID, String childID) {
        return childrenByUser.getOrDefault(parentID, Set.of()).contains(childID);
    }

    public boolean areDirectSubordinates(String parentID, Set<String> childIDs) {
        Set<String> directSubs = childrenByUser.getOrDefault(parentID, Set.of());
        return childIDs.stream().allMatch(directSubs::contains);
    }

    public boolean hasManagerWithPermissions(String userID, Set<ManagerPermissions> perms) {
        MembershipNode node = membersNodes.get(userID);
        if (node == null || node.getRoleType() != RoleType.MANAGER)
            return false;
        return node.getPermissions().containsAll(perms);
    }

    public boolean hasOutgoingInvites(String userID) {
        return invites.keySet().stream().anyMatch(k -> k.assignerId.equals(userID));
    }

    public void addPurchasePolicy(PurchasePolicy pp) {
        purchasePolicies.add(pp);
    }

    public void removePurchasePolicy(PurchasePolicy pp) {
        purchasePolicies.remove(pp);
    }

    public void addDiscountPolicy(DiscountPolicy dp) {
        discountPolicies.add(dp);
    }

    public void removeDiscountPolicy(DiscountPolicy dp) {
        discountPolicies.remove(dp);
    }

    public List<CompanyInviteRecord> getPendingUserInvites(String userID) {
        List<CompanyInviteRecord> result = new ArrayList<>();
        for (Map.Entry<InviteKey, MembershipNode> entry : invites.entrySet()) {
            InviteKey key = entry.getKey();
            MembershipNode node = entry.getValue();
            if (key.targetId.equals(userID)) {
                result.add(new CompanyInviteRecord(
                    productionCompanyID,
                    name,
                    key.assignerId,
                    key.targetId,
                    node.getRoleType(),
                    node.getPermissions()));
            }
        }
        return result;
    }
}