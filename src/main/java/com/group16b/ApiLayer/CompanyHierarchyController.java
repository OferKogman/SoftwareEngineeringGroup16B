package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.CompanyHierarchyService;
import com.group16b.ApplicationLayer.DTOs.AssignManagerRequestDTO;
import com.group16b.ApplicationLayer.DTOs.AssignOwnerRequestDTO;
import com.group16b.ApplicationLayer.DTOs.ChangeManagerPermissionRequestDTO;
import com.group16b.ApplicationLayer.DTOs.InviteHandleRequestDTO;

@RestController
@RequestMapping("/api/production-companies/{companyId}")
public class CompanyHierarchyController extends BaseController {
    private final CompanyHierarchyService companyHierarchyService;

    public CompanyHierarchyController(CompanyHierarchyService companyHierarchyService) {
        this.companyHierarchyService = companyHierarchyService;
    }

    @PostMapping("/owners")
    public ResponseEntity<?> assignOwnerToCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody AssignOwnerRequestDTO requestDTO) 
    {
        return executeWithNoReturnData(() -> companyHierarchyService.assignOwnerToCompany(companyId, requestDTO.getTargetID(), authToken));
    }

    @PostMapping("/managers")
    public ResponseEntity<?> assignManagerToCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody AssignManagerRequestDTO requestDTO) 
    {
        return executeWithNoReturnData(() -> companyHierarchyService.assignManagerToCompany(companyId, requestDTO.getTargetID(),requestDTO.getPermissions(), authToken));
    }

    @PostMapping("/invites/accept")
    public ResponseEntity<?> acceptInviteToCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody InviteHandleRequestDTO requestDTO) 
    {
        return executeWithNoReturnData(() -> companyHierarchyService.acceptInviteToCompany(companyId, requestDTO.getAssignerID(), authToken));
    }

    @PostMapping("/invites/reject")
    public ResponseEntity<?> rejectInviteToCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody InviteHandleRequestDTO requestDTO) 
    {
        return executeWithNoReturnData(() -> companyHierarchyService.rejectInviteToCompany(companyId, requestDTO.getAssignerID(), authToken));
    }

    @DeleteMapping("/owners/me")
    public ResponseEntity<?> forfeitOwnership(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId) 
    {
        return executeWithNoReturnData(() -> companyHierarchyService.forfeitOwnership(companyId, authToken));
    }

    @DeleteMapping("/members/{targetId}")
    public ResponseEntity<?> removeOwnerManager(
            @RequestHeader("Authorization") String authToken,
            @PathVariable("companyId") int companyId,
            @PathVariable("targetId") String targetId)
    {
        return executeWithNoReturnData(() -> companyHierarchyService.removeOwnerManager(targetId,companyId, authToken));
    }

    @PatchMapping("/managers/permissions")
    public ResponseEntity<?> changeManagerPermission(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody ChangeManagerPermissionRequestDTO requestDTO) 
    {
        return executeWithNoReturnData(() -> companyHierarchyService.changeManagerPermission(requestDTO.getTargetID(),companyId, requestDTO.getNewPermissions(), authToken));
    }

    @GetMapping("/hierarchy-tree")
    public ResponseEntity<?> getCompanyHierarchyTree(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId) 
    {
        return executeWithReturnData(() -> companyHierarchyService.hierarchyTree(companyId, authToken));
    }

    @GetMapping("/me/permissions")
    public ResponseEntity<?> getCompanyPerms(@PathVariable("companyId") int companyId)
    {
        return executeWithReturnData(() -> companyHierarchyService.getComapanyPermissions(companyId));
    }

}
