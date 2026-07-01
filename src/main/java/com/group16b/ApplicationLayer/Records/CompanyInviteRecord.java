package com.group16b.ApplicationLayer.Records;

import java.util.Set;

import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;

public record CompanyInviteRecord(
    int companyId,
    String companyName,
    String assignerId,
    String invitedId,
    RoleType roleType,
    Set<ManagerPermissions> managerPermissions) {
}
