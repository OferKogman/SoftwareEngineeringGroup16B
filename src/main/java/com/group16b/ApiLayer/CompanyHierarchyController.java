package com.group16b.ApiLayer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.CompanyHierarchyService;
import com.group16b.ApplicationLayer.DTOs.AssignManagerRequestDTO;
import com.group16b.ApplicationLayer.DTOs.AssignOwnerRequestDTO;
import com.group16b.ApplicationLayer.DTOs.ChangeManagerPermissionRequestDTO;
import com.group16b.ApplicationLayer.DTOs.InviteHandleRequestDTO;
import com.group16b.ApplicationLayer.DTOs.RemoveMemberRequestDTO;
import com.group16b.ApplicationLayer.Objects.Result;

@RestController
@RequestMapping("/production-companies/{companyId}")
public class CompanyHierarchyController {
    private final CompanyHierarchyService companyHierarchyService;

    public CompanyHierarchyController(CompanyHierarchyService companyHierarchyService) {
        this.companyHierarchyService = companyHierarchyService;
    }

    @GetMapping("/assign-owner")
    public ResponseEntity<?> assignOwnerToCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody AssignOwnerRequestDTO requestDTO) 
    {
        try {
            Result<Boolean> result = companyHierarchyService.assignOwnerToCompany(companyId, requestDTO.getTargetID(), authToken);
            if(result.isSuccess()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/assign-manager")
    public ResponseEntity<?> assignManagerToCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody AssignManagerRequestDTO requestDTO) 
    {
        try {
            Result<Boolean> result = companyHierarchyService.assignManagerToCompany(companyId, requestDTO.getTargetID(), requestDTO.getPermissions(), authToken);
            if(result.isSuccess()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/accept-invite")
    public ResponseEntity<?> acceptInviteToCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody InviteHandleRequestDTO requestDTO) 
    {
        try {
            Result<Boolean> result = companyHierarchyService.acceptInviteToCompany(companyId, requestDTO.getAssignerID(), authToken);
            if(result.isSuccess()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/reject-invite")
    public ResponseEntity<?> rejectInviteToCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody InviteHandleRequestDTO requestDTO) 
    {
        try {
            Result<Boolean> result = companyHierarchyService.rejectInviteToCompany(companyId, requestDTO.getAssignerID(), authToken);
            if(result.isSuccess()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/forfeit-ownership")
    public ResponseEntity<?> forfeitOwnership(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId) 
    {
        try {
            Result<Boolean> result = companyHierarchyService.forfeitOwnership(companyId, authToken);
            if(result.isSuccess()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/remove-owner-manager")
    public ResponseEntity<?> removeOwnerManager(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody RemoveMemberRequestDTO requestDTO) 
    {
        try {
            Result<Boolean> result = companyHierarchyService.removeOwnerManager(requestDTO.getTargetID(),companyId, authToken);
            if(result.isSuccess()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/change-manager-permissions")
    public ResponseEntity<?> changeManagerPermission(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId,
                    @RequestBody ChangeManagerPermissionRequestDTO requestDTO) 
    {
        try {
            Result<Boolean> result = companyHierarchyService.changeManagerPermission(requestDTO.getTargetID(), companyId, requestDTO.getNewPermissions(), authToken);
            if(result.isSuccess()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
