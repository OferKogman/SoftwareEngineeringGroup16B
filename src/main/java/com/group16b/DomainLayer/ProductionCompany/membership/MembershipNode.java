package com.group16b.DomainLayer.ProductionCompany.membership;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_nodes")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class MembershipNode implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbId;

    @Column(nullable = false)
    private String userID;

    @Column
    private String assignerID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType roleType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "membership_node_permissions", joinColumns = @JoinColumn(name = "membership_node_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<ManagerPermissions> permissions;

    protected MembershipNode() {}

    private MembershipNode(String userID, String assignerID, RoleType roleType, Set<ManagerPermissions> perms) {
        this.userID = userID;
        this.assignerID = assignerID;
        this.roleType = roleType;
        this.permissions = new HashSet<>(perms);
    }

    public MembershipNode(MembershipNode other) {
        this.userID = other.userID;
        this.assignerID = other.assignerID;
        this.roleType = other.roleType;
        this.permissions = new HashSet<>(other.permissions);
    }

    public static MembershipNode createManager(String userID, String assignerID, Set<ManagerPermissions> perms) {
        return new MembershipNode(userID, assignerID, RoleType.MANAGER, perms);
    }

    public static MembershipNode createOwner(String userID, String assignerID) {
        return new MembershipNode(userID, assignerID, RoleType.OWNER, EnumSet.allOf(ManagerPermissions.class));
    }

    public static MembershipNode createFounder(String userID) {
        return new MembershipNode(userID, null, RoleType.FOUNDER, EnumSet.allOf(ManagerPermissions.class));
    }

    public String getUserID() {
        return userID;
    }

    public String getAssignerID() {
        return assignerID;
    }

    public void setAssignerID(String newID) {
        if (this.roleType == RoleType.FOUNDER)
            throw new IllegalArgumentException("Can't update assignerID for founder of company!");
        this.assignerID = newID;
    }

    public Set<ManagerPermissions> getPermissions() {
        return new HashSet<>(permissions);
    }

    public void setPermissions(Set<ManagerPermissions> newPermissions) {
        if (this.roleType.isHigherOrEqual(RoleType.OWNER))
            throw new IllegalArgumentException("cant update permissions for owner and founder!");
        if (newPermissions == null || newPermissions.isEmpty())
            throw new IllegalArgumentException("permissions can't be null or empty!");
        this.permissions = new HashSet<>(newPermissions);
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public Long getDbId() {
        return dbId;
    }
}