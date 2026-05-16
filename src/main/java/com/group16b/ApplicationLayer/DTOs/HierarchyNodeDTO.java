package com.group16b.ApplicationLayer.DTOs;

import java.util.Set;

import com.group16b.DomainLayer.ProductionCompany.membership.HierarchyNodeData;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.RoleType;

public record HierarchyNodeDTO(
        int userID,
        int parentID,
        RoleType roleType,
        Set<ManagerPermissions> permissions
) {public HierarchyNodeDTO(HierarchyNodeData data) {
        this(
            data.getUserID(),
            data.getParentID(),
            data.getRoleType(),
            data.getPermissions()
        );
    }}
